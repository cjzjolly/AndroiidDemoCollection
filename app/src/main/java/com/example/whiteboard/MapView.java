package com.example.whiteboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by cjz on 2019/4/30.
 */

public class MapView extends View {
    private PointF currentCenter = new PointF();
    private PointF prevCurrentCenter = null;
    private float prevDistance = Float.MIN_VALUE;
    private float totalScale = 1f;
    private int mWidth = -1;
    private int mHeight = -1;
    private float dx = 0, dy = 0;
    /**缩放比例上限**/
    public static final float MAX_SCALE = 4f;
    /**缩放比例下限**/
    private final float MIN_SCALE = 0.5f;
    private final int MATRIX_LENGTH = 8 * 4;
    /**单元格表**/
    private MapUnit mapUnitMatrix[][] = new MapUnit[MATRIX_LENGTH][MATRIX_LENGTH];
    private ExecutorService mFixedThreadPool = Executors.newFixedThreadPool(8); //使用了多线程速度优化也并不大
    private Object mLock = new Object();

    /*** 触摸点点距队列**/
    private Queue<Float> touchDistanceQueue = new LinkedBlockingQueue<>();

    public MapView(Context context) {
        super(context);
    }

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public MapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        if (w != mWidth || h != mHeight) { //已经onMeasuer过一次，除非界面大小改动否则不重新初始化view
            mWidth = w;
            mHeight = h;
            createUnits();
        }
    }

    private void createUnits() {
        int longEdge = mWidth;
        if (mHeight > mWidth) {
            longEdge = mHeight;
        }
        int unitWidth = (int) (longEdge / MATRIX_LENGTH / MIN_SCALE);
        int unitHeight = (int) (longEdge / MATRIX_LENGTH / MIN_SCALE);
        unitWidth += unitWidth / 2;
        unitHeight += unitHeight / 2;
        for(int yPos = 0; yPos < MATRIX_LENGTH; yPos++) {
            for (int xPos = 0; xPos < MATRIX_LENGTH; xPos++) {
                RectF unitRange = new RectF(0, 0, unitWidth, unitHeight);
                unitRange.offset(xPos * unitWidth, yPos * unitHeight);
                MapUnit mapUnit = new MapUnit(new int[]{xPos, yPos}, unitRange, MAX_SCALE);
                mapUnit.setMapViewSize(mWidth, mHeight);
//                mapUnit.setTag(new int[]{xPos, yPos});
                mapUnitMatrix[xPos][yPos] = mapUnit;
            }
        }
    }

    /*将触摸点的坐标平均化*/
    private float avergeX = 0, avergeY = 0;
    private int prevPointCount = 0;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                prevDistance = 0;
                prevPointCount = event.getPointerCount();
                //算出移动中心坐标、点间距离
                for(int i = 0; i < event.getPointerCount(); i++){
                    avergeX += event.getX(i);
                    avergeY += event.getY(i);
                    if(i + 1 < event.getPointerCount()){
                        prevDistance += Math.sqrt(Math.pow(event.getX(i + 1) - event.getX(i), 2) + Math.pow(event.getY(i + 1) - event.getY(i), 2));
                    }
                }
                avergeX /= event.getPointerCount();
                avergeY /= event.getPointerCount();
                if(prevCurrentCenter == null){
                    prevCurrentCenter = new PointF(avergeX, avergeY);
                } else {
                    prevCurrentCenter.set(avergeX, avergeY);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                avergeX = 0;
                avergeY = 0;
                float nowDistance = 0;
                //算出移动中心坐标、点间距离
                for(int i = 0; i < event.getPointerCount(); i++){
                    avergeX += event.getX(i);
                    avergeY += event.getY(i);
                    if (i + 1 < event.getPointerCount()) {
                        nowDistance += Math.sqrt(Math.pow(event.getX(i + 1) - event.getX(i), 2) + Math.pow(event.getY(i + 1) - event.getY(i), 2));
                    }
                }
                //现在的点间距离 除以 上次点间距离 这次得到缩放比例
                avergeX /= event.getPointerCount();
                avergeY /= event.getPointerCount();
                if ((prevPointCount != event.getPointerCount()) || event.getPointerCount() <= 1 || prevPointCount <= 1) { //触摸点数突然改变 或者 触摸点不超过2，不允许缩放
                    prevDistance = nowDistance = 0;
                }
                //如果缩放数据有效，则进行平均平滑化并且进行缩放
                if(prevDistance > 0 && nowDistance > 0){
                    touchDistanceQueue.add(nowDistance / prevDistance);
                    if(touchDistanceQueue.size() >= 6) {
                        Float point[] = new Float[touchDistanceQueue.size()];
                        touchDistanceQueue.toArray(point);
                        float avergDistance = 0;
                        for(int i = 0; i < point.length; i++){
                            avergDistance += point[i];
                        }
                        avergDistance /= point.length;
                        scale((float) Math.sqrt(avergDistance), avergeX, avergeY);
                        while(touchDistanceQueue.size() > 6){
                            touchDistanceQueue.poll();
                        }
                    }
                }
                prevPointCount = event.getPointerCount();
                prevDistance = nowDistance;
                //当前坐标 - 上次坐标 = 偏移值，然后进行位置偏移
                if(prevCurrentCenter == null) {
                    prevCurrentCenter = new PointF(avergeX, avergeY);
                } else {
                    float dx = avergeX - prevCurrentCenter.x;
                    float dy = avergeY - prevCurrentCenter.y;
                    if (Math.abs(dx) < 1f) {
                        dx = 0f;
                    }
                    if (Math.abs(dy) < 1f) {
                        dy = 0f;
                    }
                    translate(dx, dy);
                    prevCurrentCenter.set(avergeX, avergeY);
                }
                break;
            case MotionEvent.ACTION_UP:
                //抬起，清理干净数据
                avergeX = 0;
                avergeY = 0;
                touchDistanceQueue.clear();
                break;
        }
        invalidate();
        return true;
    }

    /**
     * 缩放函数
     **/
    public void scale(float scale, float px, float py) {
        if(totalScale * scale < MIN_SCALE || totalScale * scale > MAX_SCALE){
            return;
        }
        totalScale *= scale;
        for(int yPos = 0; yPos < MATRIX_LENGTH; yPos++) {
            for (int xPos = 0; xPos < MATRIX_LENGTH; xPos++) {
                MapUnit mapUnit = mapUnitMatrix[xPos][yPos];
                mapUnit.scale(scale);
            }
        }
        Log.i("缩放", String.format("百分比：%f", totalScale));
    }

    /**
     * 移动函数 (效率有点问题，但暂时不管，反正以后要用OpenGL重写的，自定义View的显示效率不是最终追求的最优选择)
     **/
    private void translate(float distanceX, float distanceY) {
        dx += distanceX;
        dy += distanceY;
        for (int yPos = 0; yPos < MATRIX_LENGTH; yPos++) {
            for (int xPos = 0; xPos < MATRIX_LENGTH; xPos++) {
                MapUnit view = mapUnitMatrix[xPos][yPos];
                view.offset((int) distanceX, (int) distanceY);
            }
        }
        //以单元格组成的总范围作为判断，这个范围要和视口对齐:
        int leftSide = mWidth / 2 - (int) (mapUnitMatrix[0][0].getWidth() * MATRIX_LENGTH / 2);
        int rightSide = mWidth / 2 + (int) (mapUnitMatrix[0][0].getWidth() * MATRIX_LENGTH / 2);
        int topSide = mHeight / 2 - (int) (mapUnitMatrix[0][0].getHeight() * MATRIX_LENGTH / 2);
        int bottomSide = mHeight / 2 + (int) (mapUnitMatrix[0][0].getHeight() * MATRIX_LENGTH / 2);
        //x轴,y轴要分开两个循环处理，否则会引发混乱
        for (int yPos = 0; yPos < MATRIX_LENGTH; yPos++) {
            for (int xPos = 0; xPos < MATRIX_LENGTH; xPos++) {
                MapUnit view = mapUnitMatrix[xPos][yPos];
                //移除去的部分添加到未显示的部分的末尾
                if (view.getXY().x + view.getWidth() < leftSide && mWidth > 0) { //单元格溢出到了屏幕左边，移动到当前对应行最右边
                    if (xPos == 0) {
                        //重设位置
                        view.setX(mapUnitMatrix[MATRIX_LENGTH - 1][yPos].getXY().x + mapUnitMatrix[MATRIX_LENGTH - 1][yPos].getWidth());
                        view.setTag(new int[] {mapUnitMatrix[MATRIX_LENGTH - 1][yPos].getTag()[0] + 1, view.getTag()[1]});  //todo 使用线程池执行该方法
                        for (int i = xPos; i < MATRIX_LENGTH - 1; i++) {
                            mapUnitMatrix[i][yPos] = mapUnitMatrix[i + 1][yPos];
                        }
                        mapUnitMatrix[MATRIX_LENGTH - 1][yPos] = (MapUnit) view;
                    }
                }
                else if (view.getXY().x > rightSide && mWidth > 0) {
                    if (xPos == MATRIX_LENGTH - 1) { //因为初始化时显示的Unit是最左上角的Unit，有可能导致非最后一列的内容被平移，这违反自动补充的逻辑，会出bug，所以要加判断
                        //重设位置(设置和最后一个的左上角坐标直接重合（setx用于设定左上角坐标），再减去控件宽度*缩放量使得目标控件右上角和最后一个控件左上角对齐)
                        view.setX(mapUnitMatrix[0][yPos].getXY().x - mapUnitMatrix[0][yPos].getWidth());
                        view.setTag(new int[] {mapUnitMatrix[0][yPos].getTag()[0] - 1, view.getTag()[1]});
                        MapUnit temp = mapUnitMatrix[MATRIX_LENGTH - 1][yPos];
                        for (int i = MATRIX_LENGTH - 1; i > 0 ; i--) {
                            mapUnitMatrix[i][yPos] = mapUnitMatrix[i - 1][yPos];
                        }
                        mapUnitMatrix[0][yPos] = temp;
                    }
                }
            }
        }
        for (int yPos = 0; yPos < MATRIX_LENGTH; yPos++) {
            for (int xPos = 0; xPos < MATRIX_LENGTH; xPos++) {
                MapUnit view = mapUnitMatrix[xPos][yPos];
                if (view.getXY().y + view.getHeight() < topSide && mHeight > 0) {
                    if (yPos == 0) {
                        //重设位置
                        view.setY(mapUnitMatrix[xPos][MATRIX_LENGTH - 1].getXY().y + mapUnitMatrix[xPos][MATRIX_LENGTH - 1].getHeight());
                        view.setTag(new int[] {view.getTag()[0], mapUnitMatrix[xPos][MATRIX_LENGTH - 1].getTag()[1] + 1});
                        for (int i = yPos; i < MATRIX_LENGTH - 1; i++) {
                            mapUnitMatrix[xPos][i] = mapUnitMatrix[xPos][i + 1];
                        }
                        mapUnitMatrix[xPos][MATRIX_LENGTH - 1] = (MapUnit) view;
                    }
                }
                else if (view.getXY().y > bottomSide && mHeight > 0) {
                    if (yPos == MATRIX_LENGTH - 1) {
                        //Log.i("越位", "到了屏幕下边界");
                        //重设位置(设置和最后一个的左上角坐标直接重合（setx用于设定左上角坐标），再减去控件宽度*缩放量使得目标控件右上角和最后一个控件左上角对齐)
                        view.setY(mapUnitMatrix[xPos][0].getXY().y - mapUnitMatrix[xPos][0].getHeight());
                        view.setTag(new int[] {view.getTag()[0], mapUnitMatrix[xPos][0].getTag()[1] - 1});
                        MapUnit temp = mapUnitMatrix[xPos][MATRIX_LENGTH - 1];
                        for (int i = MATRIX_LENGTH - 1; i > 0; i--) {
                            mapUnitMatrix[xPos][i] = mapUnitMatrix[xPos][i - 1];
                        }
                        mapUnitMatrix[xPos][0] = temp;
                    }
                }
            }
        }
        Log.i("移动", String.format("x位移：%f， y位移：%f", distanceX, distanceY));
        invalidate();
    }

    private void refreshCanvas(Canvas canvas) {
        if (null == mapUnitMatrix) {
            return;
        }
        if (canvas == null) {
            return;
        }
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        RectF mapViewRange = new RectF(0, 0, mWidth, mHeight);
        /**遍历所有瓦片并进行绘制**/
        int createThreadCount = 0;
        List<MapUnit> needRefreshUnit = new LinkedList<>();
        //确定需要渲染的图块
        for (int yPos = 0; yPos < MATRIX_LENGTH; yPos++) {
            for (int xPos = 0; xPos < MATRIX_LENGTH; xPos++) {
                MapUnit mapUnit = mapUnitMatrix[xPos][yPos];
                if (mapUnit == null) {
                    continue;
                }
                //如果和可见区域不相交，就不显示了
                if (null == mapUnit.getRange() ||
                        !mapViewRange.intersects(mapUnit.getRange().left, mapUnit.getRange().top, mapUnit.getRange().right, mapUnit.getRange().bottom)) {
                    continue;
                }
                createThreadCount++;
                needRefreshUnit.add(mapUnit);
            }
        }
        //多线程从外存中读入图块
        CountDownLatch countDownLatch = new CountDownLatch(createThreadCount);
        for (MapUnit mapUnit : needRefreshUnit) {
            mFixedThreadPool.execute(new Thread(() -> { //证明了可以在子线程中执行
                mapUnit.loadTileBmp();
                countDownLatch.countDown();
            }));
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //图块绘制
        for (MapUnit mapUnit : needRefreshUnit) {
            mapUnit.onDraw(canvas); //canvas不能同时被两个unit使用
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        refreshCanvas(canvas);
    }

    /**把当前的绘制内容分割并叠加到瓦片中去**/
    public void drawBmp(Bitmap contentBmp) {
        RectF mapViewRange = new RectF(0, 0, mWidth, mHeight);
        List<MapUnit> needRefreshUnit = new LinkedList<>();
        //确定需要保存的图块
        for (int yPos = 0; yPos < MATRIX_LENGTH; yPos++) {
            for (int xPos = 0; xPos < MATRIX_LENGTH; xPos++) {
                MapUnit mapUnit = mapUnitMatrix[xPos][yPos];
                if (mapUnit == null) {
                    continue;
                }
                //如果和可见区域不相交，就不保存了
                if (null == mapUnit.getRange() ||
                        !mapViewRange.intersects(mapUnit.getRange().left, mapUnit.getRange().top, mapUnit.getRange().right, mapUnit.getRange().bottom)) {
                    continue;
                }
                needRefreshUnit.add(mapUnit);
            }
        }
        //多线程保存
        CountDownLatch countDownLatch = new CountDownLatch(needRefreshUnit.size());
        for (MapUnit mapUnit : needRefreshUnit) {
            mFixedThreadPool.execute(new Thread(() -> { //证明了可以在子线程中执行
                mapUnit.drawBmp(contentBmp);
                countDownLatch.countDown();
            }));
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        invalidate();
    }

    /**图块合成大图**/
    public void readBMP(Bitmap bmp) {
        RectF mapViewRange = new RectF(0, 0, mWidth, mHeight);
        Canvas canvas = new Canvas(bmp);
        canvas.scale(canvas.getWidth() / (float) mWidth, canvas.getHeight() / (float) mHeight); //让任意放大倍数的载体图片都能正常绘制
        for (int yPos = 0; yPos < MATRIX_LENGTH; yPos++) {
            for (int xPos = 0; xPos < MATRIX_LENGTH; xPos++) {
                MapUnit mapUnit = mapUnitMatrix[xPos][yPos];
                if (mapUnit == null) {
                    continue;
                }
                //如果和可见区域不相交，就不绘制了
                if (null == mapUnit.getRange() ||
                        !mapViewRange.intersects(mapUnit.getRange().left, mapUnit.getRange().top, mapUnit.getRange().right, mapUnit.getRange().bottom)) {
                    continue;
                }
                mapUnit.drawTo(canvas);
            }
        }
    }
}