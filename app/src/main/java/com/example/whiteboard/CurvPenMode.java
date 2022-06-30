package com.example.whiteboard;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *  曲线容器，一个容器的曲线>=1
 * Created by cjz on 2018/9/17.
 */

public class CurvPenMode extends BaseCurv {
    public Paint paint;
    private List<PointF> touchPointList = new ArrayList<>();
    private List<Path> mSegPathList = new ArrayList<>(); //用于加速画布的一小段一小段的path
    private List<Path> pathList = new ArrayList<>(); //用于保存该容器里面有多少个path
    /**
     * 点信息
     */
    private PointF start;
    private PointF last;
    private PointF current;
    private PointF mid;
    private PointF end;
    private float cx;
    private float cy;
    private float midX;
    private float midY;
    private float startX;
    private float startY;
    /**
     * 绘制范围
     */
    public RectF range;
    private float width = 2 / 3.3f;

    private boolean isStart = false;
    public Path mTotalPath;
    private Path drawPath;

    private class TouchInfo {
        float mX;
        float mY;
        int mAction;
    }
    /**触摸事件历史记录**/
    private List<TouchInfo> mTouchHistory = new LinkedList<>();

    private void init() {
        start = new PointF();
        last = new PointF();
        current = new PointF();
        mid = new PointF();
        end = new PointF();
        range = new RectF();
        mTotalPath = new Path();
        pathList.add(mTotalPath);
    }

    public boolean isStart() {
        return isStart;
    }


    public CurvPenMode(Paint paint) {
        this.paint = new Paint(paint);
        init();
    }

    /**
     * 处理范围
     *
     * @param x 判断点x
     * @param y 判断点y
     */
    private void handleRange(float x, float y) {
        if (x <= range.left)
            range.left = x;
        if (y <= range.top)
            range.top = y;
        if (x >= range.right)
            range.right = x;
        if (y >= range.bottom)
            range.bottom = y;
    }

    public Rect getRect() {
        int padding = (int) (paint.getStrokeWidth() / 2 + 5);
        RectF rectF = new RectF();
        drawPath.computeBounds(rectF, true);
        return new Rect((int) rectF.left - padding, (int) rectF.top - padding, (int) rectF.right + padding, (int) rectF.bottom + padding);
    }

    public void setCurrentRaw(float x, float y, int action) {
        //记录起始点
        if (!isStart) {
            start.set(x, y);
            mid.set(x,y);
            end.set(x,y);
            isStart = true;
        }


        //记录上一个点
        last.set(current.x, current.y);

        //记录当前点
        current.set(x, y);

        //处理范围
        handleRange(x, y);

    }
    /**
     * 落闸放点（狗），贝塞尔曲线化
     *
     * @param x
     * @param y
     * @param action
     */
    @Override
    public void draw(float x, float y, int action, Canvas canvas) {
        TouchInfo touchInfo = new TouchInfo();
        touchInfo.mAction = action;
        touchInfo.mX = x;
        touchInfo.mY = y;
        mTouchHistory.add(touchInfo);

        if(!isStart()) {
            setCurrentRaw(x, y, action);

            mTotalPath.moveTo(x, y);
//            if(!isBuildPathAllDoing)
            touchPointList.add(new PointF(x, y));
            mSegPathList.add(new Path());
//            canvas.drawPath(segPathList.get(segPathList.size() - 1), paint);

        } else {
            if (action == MotionEvent.ACTION_UP)
                System.out.println("setCurrent end " + x + " , " + y);
            touchPointList.add(new PointF(x, y));
            drawPath = new Path();
            mSegPathList.add(drawPath);
            setCurrentRaw(x, y, action);

            double distance = Math.sqrt(Math.pow(Math.abs(x - last.x), 2) + Math.pow(Math.abs(y - last.y), 2));
            /**如果两次点击之间的距离过大，就判断为该点报废，Current点回退到last点**/
            if (distance > 400) {  //如果距离突变过长，判断为无效点，直接current回退到上一次纪录的last的点，并且用UP时间结束这次path draw
                Log.i("NewCurv.SetCurrent", "超长" + distance);
//                super.setCurrent(getLast().x, getLast().y, MotionEvent.ACTION_UP);
                System.out.println("超长?");
                return;
            }
            cx = last.x;
            cy = last.y;

            midX = (x + cx) / 2;
            midY = (y + cy) / 2;

            startX = mid.x;
            startY = mid.y;

            mid.x = midX;
            mid.y = midY;

            drawPath.moveTo(startX, startY);

            double s = Math.sqrt(Math.pow(x - cx, 2) + Math.pow(y - cy, 2));
            if (action == MotionEvent.ACTION_UP){
                drawPath.lineTo(x,y);
                mTotalPath.lineTo(x, y);
            } else {
                if (s < 200) {
                    if (s < 2) {//1.10 //2.12 //3.15
                        drawPath.cubicTo(cx, cy, midX, midY, x, y);
                        mTotalPath.cubicTo(cx, cy, midX, midY, x, y);
                        System.out.println("cubicTo");
                    } else {
                        drawPath.quadTo(cx, cy, midX, midY);
                        mTotalPath.quadTo(cx, cy, midX, midY);
//                    System.out.println("quadTo");
                    }
                } else {
                    drawPath.quadTo(cx, cy, midX, midY);
                    mTotalPath.quadTo(cx, cy, midX, midY);
                }
            }
//            canvas.drawPath(segPathList.get(segPathList.size() - 1), paint);

        }
        if (action == MotionEvent.ACTION_UP) {
            if (mTotalPath != null && paint != null) {
                PathMeasure pathMeasure = new PathMeasure(mTotalPath, false);
                if (pathMeasure.getLength() < 2f) {
                    paint.setStyle(Paint.Style.FILL);
                    mTotalPath = new Path();
                    mTotalPath.addCircle(x + paint.getStrokeWidth() / 2f, y + paint.getStrokeWidth() / 2f, paint.getStrokeWidth() / 2f, Path.Direction.CCW);
//                    canvas.drawPath(mTotalPath, paint);
                }
            }
        }
        drawTo(canvas);
    }

    float mRatio = 1f;
    public void drawTo(Canvas canvas) {
        Paint changedPaint = new Paint(paint);
        changedPaint.setStrokeCap(Paint.Cap.ROUND);//结束的笔画为圆心
        changedPaint.setStrokeJoin(Paint.Join.ROUND);//连接处元
        changedPaint.setStrokeMiter(1.0f);

        if(mSegPathList.size() >= 2) {
            Path prevPath = mSegPathList.get(mSegPathList.size() - 2);
            PathMeasure pathMeasure = new PathMeasure(prevPath, false);
            float beforeRatio = mRatio; //从之前的曲率渐变到现在的曲率，产生细分效果

            if (paint.getStrokeWidth() > 5) { //粗线条的粗细率变化
                if (pathMeasure.getLength() > 15f) { //速度快
                    if (mRatio > 0.3f) {
                        mRatio *= 0.8f;
                    }
                } else if (pathMeasure.getLength() < 5f) { //速度慢
                    if (mRatio < 1.5f) {
                        mRatio *= 1.15f;
                    }
                } else { //不快不慢，回到原大小
                    if (mRatio > 1f) {
                        mRatio *= 0.95f;
                    } else {
                        mRatio *= 1.15f;
                    }
                }
            } else { //细线条的粗细率变化
                if (pathMeasure.getLength() > 5f) { //速度快
                    if (mRatio > 0.3f) {
                        mRatio *= 0.5f;
                    }
                } else if (pathMeasure.getLength() < 2f) { //速度慢
                    if (mRatio < 1.5f) {
                        mRatio *= 1.4f;
                    }
                } else { //不快不慢，回到原大小
                    if (mRatio > 1f) {
                        mRatio *= 0.8f;
                    } else {
                        mRatio *= 1.2f;
                    }
                }
            }
            changedPaint.setStrokeWidth(changedPaint.getStrokeWidth() * mRatio);
            //分母，将当成小线段drawPath细分成多少份，份数越大曲面细分越细腻
            float denominator = 60f;
            Paint smallPaint = new Paint(paint);
            smallPaint.setStrokeWidth(2f);
            smallPaint.setStyle(Paint.Style.FILL);
//            smallPaint.setMaskFilter(new BlurMaskFilter(0.8f, BlurMaskFilter.Blur.SOLID)); //模糊效果
            //todo j起始值太小会有毛刺，太大会显得不连贯
            /** 按百分比遍历的循环
             *  i ---> 粗细比率的遍历用变量
             *  j ---> 遍历细分的PathMeasure的分子的存储变量
             *  denominator   ----->   分母，将当成小线段drawPath细分成多少份
             * (ratio - beforeRatio) / denominator ----> 将百分率的渐变细分成像线条那么多份，然后每份有多大
             *
             * **/
            if (beforeRatio < mRatio) {
                for (float i = beforeRatio, j = 0.4f; i < mRatio && j < denominator; i += (mRatio - beforeRatio) / denominator, j++) {
                    drawCurvSubDivision(paint, smallPaint, pathMeasure, canvas, i, j, denominator);
                }
            } else {
                for (float i = beforeRatio, j = 0.4f; i >= mRatio && j < denominator; i += (mRatio - beforeRatio) / denominator, j++) {
                    drawCurvSubDivision(paint, smallPaint, pathMeasure, canvas, i, j, denominator);
                }
            }
            DrawPathAndPaint drawPathAndPaint = new DrawPathAndPaint();
            drawPathAndPaint.inLength = new PathMeasure(mTotalPath, false).getLength() - new PathMeasure(drawPath, false).getLength();
            drawPathAndPaint.paint = changedPaint;
            drawPathAndPaint.ratio = mRatio;
            drawPathAndPaintList.add(drawPathAndPaint);
        }
    }

    /**绘制到瓦片中(把所有记录过的操作重做一遍)**/
    public void drawToTileCanvas(Canvas canvas) {
        CurvPenMode curvPenMode = new CurvPenMode(paint);
        for (int i = 0; i < mTouchHistory.size(); i++) {
            TouchInfo touchInfo = mTouchHistory.get(i);
            curvPenMode.draw(touchInfo.mX, touchInfo.mY, touchInfo.mAction, canvas);
        }
    }

    private class DrawPathAndPaint {
        public float inLength;
        public Paint paint;
        public float ratio;
    }

    public List<DrawPathAndPaint> drawPathAndPaintList = new LinkedList<>();


    /** 传入当前绘制线段的PathMeasure，用菱形曲面细分使得笔划细腻而好看，避免粗细的突变感
     *  @param  paint  传入原始画笔，用于通过画笔宽度确定菱形长度和高度
     *  @param  borderPaint 传入菱形的边界画笔，用于确定每个菱形单元用多粗的线条进行绘制
     *  @param  drawPathMeasure 需要被曲面细分的线条的PathMeasure，用于遍历该线条
     *  @param  divisionRatio 传入PathMeausre指定目标刻度应该用多少原本画笔粗细的比率来绘制一个菱形，来形成渐变过度
     *  @param  pathMeausreNumerator 传入要进行细分绘制的PathMeasure的第几个刻度(用分子表示)
     *  @param  pathMeasureDenominator  传入要进行细分绘制的PathMeasure分成了几份，即分母，分母越大，则细分分数越多，线条则越细腻
     * **/
    private void drawCurvSubDivision(Paint paint, Paint borderPaint, PathMeasure drawPathMeasure, Canvas canvas, float divisionRatio, float pathMeausreNumerator, float pathMeasureDenominator){
        Path pathArrow = new Path();
        float w = paint.getStrokeWidth() * divisionRatio > 1f ? paint.getStrokeWidth() * divisionRatio : 1f;
        pathArrow.moveTo(0, w / 2);
        pathArrow.lineTo(w, 0 );
        pathArrow.lineTo(2 * w, w / 2);
        pathArrow.lineTo(w, w);
        pathArrow.lineTo(0, w / 2);
        float[] pos = new float[2];
        float[] tan = new float[2];
        drawPathMeasure.getPosTan(pathMeausreNumerator / pathMeasureDenominator * drawPathMeasure.getLength(), pos, tan);
//                        canvas.drawCircle(pos[0], pos[1], paint.getStrokeWidth() / 2f * smallRatio, changedPaint);
        Matrix matrix = new Matrix();
        //计算方位角
        float degrees = (float) (Math.atan2(tan[1], tan[0]) * 180.0 / Math.PI);
        RectF rect = new RectF();
        pathArrow.computeBounds(rect, false);
        matrix.postRotate(degrees, rect.width() / 2, rect.height() / 2);   // 旋转图片
        matrix.postTranslate(pos[0] - rect.width() / 2, pos[1] - rect.height() / 2);   // 将图片绘制中心调整到与当前点重合
        pathArrow.transform(matrix);
        if (canvas != null) {
            canvas.drawPath(pathArrow, borderPaint);
        }
    }


    public Path getTotalPath() {
        return mTotalPath;
    }
}