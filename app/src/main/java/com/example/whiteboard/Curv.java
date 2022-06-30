package com.example.whiteboard;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

/**
 *  曲线容器，一个容器的曲线>=1
 * Created by cjz on 2018/9/17.
 */

public class Curv extends BaseCurv {
    public Paint paint;
    private List<PointF> touchPointList = new ArrayList<>();
    private List<Path> segPathList = new ArrayList<>(); //用于加速画布的一小段一小段的path
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


    public Curv(Paint paint) {
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
        if(!isStart()) {
            setCurrentRaw(x, y, action);

            mTotalPath.moveTo(x, y);
//            if(!isBuildPathAllDoing)
            touchPointList.add(new PointF(x, y));
            segPathList.add(new Path());
            canvas.drawPath(segPathList.get(segPathList.size() - 1), paint);

        } else {
            if (action == MotionEvent.ACTION_UP)
                System.out.println("setCurrent end " + x + " , " + y);
            touchPointList.add(new PointF(x, y));
            drawPath = new Path();
            segPathList.add(drawPath);
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
            canvas.drawPath(segPathList.get(segPathList.size() - 1), paint);

        }
        if(action == MotionEvent.ACTION_UP) {
            if(mTotalPath != null && paint != null){
                PathMeasure pathMeasure = new PathMeasure(mTotalPath, false);
                if(pathMeasure.getLength() < 2f){
                    paint.setStyle(Paint.Style.FILL);
                    mTotalPath = new Path();
                    mTotalPath.addCircle(x + paint.getStrokeWidth() / 2f, y + paint.getStrokeWidth() / 2f, paint.getStrokeWidth() / 2f, Path.Direction.CCW);
                    canvas.drawPath(mTotalPath, paint);
                }
            }
        }
    }

    public Path getTotalPath() {
        return mTotalPath;
    }
}