package com.example.whiteboard;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by cjz on 2019/4/30.
 */

public class Whiteboard extends FrameLayout {

    private PointF currentCenter = new PointF();
    private PointF prevCurrentCenter = null;
    private float prevDistance = Float.MIN_VALUE;
    private float totalScale = 1f;
    private float dx = 0, dy = 0;
    /**缩放比例上限**/
    private final float MAX_SCALE = 2f;
    /**缩放比例下限**/
    private final float MIN_SCALE = 0.5f;
    /**单元格矩阵长宽均有多少个单元**/
    private final int MATRIX_LENGTH = 4;
    /**单元格表**/
    private MapUnit mapUnitMatrix[][] = new MapUnit[MATRIX_LENGTH][MATRIX_LENGTH];
    private boolean initFinished = false;

    /*** 触摸点点距队列**/
    private Queue<Float> touchDistanceQueue = new LinkedBlockingQueue<>();

    public Whiteboard(Context context) {
        super(context);
    }

    public Whiteboard(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public Whiteboard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(!initFinished){
            createView();
            initFinished = true;
        }
    }

    private void createView() {
        for(int yPos = 0; yPos < MATRIX_LENGTH; yPos++) {
            for (int xPos = 0; xPos < MATRIX_LENGTH; xPos++) {
                MapUnit mapUnit = new MapUnit(getContext());
//                mapUnit.setImageResource(R.mipmap.ic_launcher);
                mapUnit.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                mapUnit.setTag(new int[]{xPos, yPos});
                addView(mapUnit);
                mapUnitMatrix[xPos][yPos] = mapUnit;
            }
        }
        for(int yPos = 0; yPos < MATRIX_LENGTH; yPos++) {
            for (int xPos = 0; xPos < MATRIX_LENGTH; xPos++) {
                mapUnitMatrix[xPos][yPos].setX(xPos * getMeasuredWidth());
                mapUnitMatrix[xPos][yPos].setY(yPos * getMeasuredHeight());
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
                    if(i + 1 < event.getPointerCount()){
                        nowDistance += Math.sqrt(Math.pow(event.getX(i + 1) - event.getX(i), 2) + Math.pow(event.getY(i + 1) - event.getY(i), 2));
                    }
                }
                //现在的点间距离 除以 上次点间距离 这次得到缩放比例
                avergeX /= event.getPointerCount();
                avergeY /= event.getPointerCount();
                if((prevPointCount != event.getPointerCount()) || event.getPointerCount() <= 1 || prevPointCount <= 1){ //触摸点数突然改变 或者 触摸点不超过2，不允许缩放
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
                    translate(avergeX - prevCurrentCenter.x, avergeY - prevCurrentCenter.y);
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
                View view = mapUnitMatrix[xPos][yPos];
                //以本View中心点为缩放中心缩放
                view.setScaleX(view.getScaleX() * scale);
                view.setScaleY(view.getScaleY() * scale);
                //求本view中心点在屏幕中的坐标
                float centerX = view.getX() + view.getWidth() / 2;
                float centerY = view.getY() + view.getHeight() / 2;
                /**向缩放中心靠拢，例如缩放为原来的80%，那么缩放中心x到view中心x的距离则为0.8*(缩放中心x - view中心x),
                 * 那么view的x距离屏幕左边框的距离则 为   view中心x + (1 - 0.8) * (缩放x - view中心x)  ****/
                float centerXAfterScale = centerX + (px - centerX) * (1 - scale); //view中心向缩放中心聚拢或扩散
                float centerYAfterScale = centerY + (py - centerY) * (1 - scale);
                view.setX(centerXAfterScale - view.getWidth() / 2); //setXY是set左上角的x,y，所以view中心点要减去宽度/高度的一般来重新得到应该去的左上角坐标
                view.setY(centerYAfterScale - view.getHeight() / 2);
//            viewFind(view, this.scale);
                Log.i("View" + view.hashCode() + "的信息", String.format("长度:%d, 宽度:%d, 坐标x:%f, 坐标y:%f", view.getWidth(), view.getHeight(), view.getX(), view.getY()));
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
        for(int yPos = 0; yPos < MATRIX_LENGTH; yPos++) {
            for (int xPos = 0; xPos < MATRIX_LENGTH; xPos++) {
                View view = mapUnitMatrix[xPos][yPos];
                view.setX(view.getX() + (distanceX));
                view.setY(view.getY() + (distanceY));
            }
        }
        //x轴,y轴要分开两个循环处理，否则会引发混乱
        for(int yPos = 0; yPos < MATRIX_LENGTH; yPos++) {
            for (int xPos = 0; xPos < MATRIX_LENGTH; xPos++) {
                View view = mapUnitMatrix[xPos][yPos];
                //移除去的部分添加到未显示的部分的末尾
                if(view.getX()  + (1 - view.getScaleX()) / 2 * view.getWidth() + view.getWidth() * view.getScaleX() < 0  && getWidth() > 0) { //单元格溢出到了屏幕左边，移动到当前对应行最右边
                    if(xPos == 0) {
                        //重设位置
                        view.setX(mapUnitMatrix[MATRIX_LENGTH - 1][yPos].getX() + mapUnitMatrix[MATRIX_LENGTH - 1][yPos].getWidth() * mapUnitMatrix[MATRIX_LENGTH - 1][yPos].getScaleX());
                        for (int i = xPos; i < MATRIX_LENGTH - 1; i++) {
                            mapUnitMatrix[i][yPos] = mapUnitMatrix[i + 1][yPos];
                        }
                        mapUnitMatrix[MATRIX_LENGTH - 1][yPos] = (MapUnit) view;
                    }
                }
                else if (view.getX() + (1 - view.getScaleX()) / 2 * view.getWidth() > getWidth() && getWidth() > 0) {
                    if(xPos == MATRIX_LENGTH - 1){ //因为初始化时显示的Unit是最左上角的Unit，有可能导致非最后一列的内容被平移，这违反自动补充的逻辑，会出bug，所以要加判断
                        //重设位置(设置和最后一个的左上角坐标直接重合（setx用于设定左上角坐标），再减去控件宽度*缩放量使得目标控件右上角和最后一个控件左上角对齐)
                        view.setX(mapUnitMatrix[0][yPos].getX() - mapUnitMatrix[0][yPos].getWidth() * mapUnitMatrix[0][yPos].getScaleX());
                        MapUnit temp = mapUnitMatrix[MATRIX_LENGTH - 1][yPos];
                        for(int i = MATRIX_LENGTH - 1; i > 0 ; i--){
                            mapUnitMatrix[i][yPos] = mapUnitMatrix[i - 1][yPos];
                        }
                        mapUnitMatrix[0][yPos] = temp;
                    }
                }
            }
        }
        for(int yPos = 0; yPos < MATRIX_LENGTH; yPos++) {
            for (int xPos = 0; xPos < MATRIX_LENGTH; xPos++) {
                View view = mapUnitMatrix[xPos][yPos];
                if (view.getY() + (1 - view.getScaleY()) / 2 * view.getHeight() + view.getHeight() * view.getScaleY() < 0 && getHeight() > 0) {
                    if (yPos == 0) {
                        //重设位置
                        view.setY(mapUnitMatrix[xPos][MATRIX_LENGTH - 1].getY() + mapUnitMatrix[xPos][MATRIX_LENGTH - 1].getHeight() * mapUnitMatrix[xPos][MATRIX_LENGTH - 1].getScaleY());
                        for (int i = yPos; i < MATRIX_LENGTH - 1; i++) {
                            mapUnitMatrix[xPos][i] = mapUnitMatrix[xPos][i + 1];
                        }
                        mapUnitMatrix[xPos][MATRIX_LENGTH - 1] = (MapUnit) view;
                    }
                }
                else if (view.getY() + (1 - view.getScaleY()) / 2 * view.getHeight() > getHeight() && getHeight() > 0) {
                    if (yPos == MATRIX_LENGTH - 1) {
                        //Log.i("越位", "到了屏幕下边界");
                        //重设位置(设置和最后一个的左上角坐标直接重合（setx用于设定左上角坐标），再减去控件宽度*缩放量使得目标控件右上角和最后一个控件左上角对齐)
                        view.setY(mapUnitMatrix[xPos][0].getY() - view.getHeight() * view.getScaleY());
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
//        invalidate();
    }

}