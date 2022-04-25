package com.example.photoCutter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.piccut.R;

/**图片裁剪框
 * @author chenjiezhu
 * **/
public class PhotoCutter extends View {
    private final String TAG = getClass().getName();
    private int mWidth, mHeight;
    /**多边形选择框的顶点数据**/
    private PointF mVectorPoint[] = new PointF[4];
    /**多边形选择框的可触摸范围定义**/
    private RectF mTouchArea[] = new RectF[4];
    /**要被处理的图片**/
    private Bitmap mBitmap = null;
    /**图片的缩放绿**/
    private float mScale = 1f;
    /**选择器端点画笔**/
    private Paint mSelectorRectPointPaint = null;
    /**被选择的端点**/
    private int mSelectedPoint = -1;
    /**上一次点击的位置**/
    private PointF mPrevTouchPos = null;
    /**端点感知范围**/
    private int areaWidth = 200;

    public PhotoCutter(Context context) {
        super(context);
    }

    public PhotoCutter(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PhotoCutter(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private double linesAngle(PointF first, PointF middle, PointF end) {
        double x1 = first.x - middle.x;
        double x2 = end.x - middle.x;
        double y1 = first.y - middle.y;
        double y2 = end.y - middle.y;
        double dot = x1 * x2 + y1 * y2;
        double det = x1 * y2 - y1 * x2;
        double angle = Math.atan2(det, dot) / Math.PI * 180f;
        return angle;
    }

    /**检查如果角度更改，是否依然符合内角不超过180的要求
     * @param dx 控制点将要偏移的值
     * @param dy 控制点将要偏移的值
     * @return 是否依然符合要求
     * **/
    private boolean willAngleFit(float dx, float dy) {
        PointF vectorPoint[] = new PointF[mVectorPoint.length];
        for (int i = 0; i < vectorPoint.length; i++) {
            vectorPoint[i] = new PointF();
            vectorPoint[i].x = mVectorPoint[i].x + (mSelectedPoint == i ? dx : 0);
            vectorPoint[i].y = mVectorPoint[i].y + (mSelectedPoint == i ? dy : 0);
        }
        //不允许形成超过180度的角
        double angle = linesAngle(vectorPoint[1],
                new PointF(vectorPoint[0].x, vectorPoint[0].y),
                vectorPoint[3]);
        if (angle < 0f) {
            return false;
        }
        //不允许形成超过180度的角
        angle = linesAngle(vectorPoint[0],
                new PointF(vectorPoint[1].x, vectorPoint[1].y),
                vectorPoint[2]);
        if (angle > 0f) {
            return false;
        }
        angle = linesAngle(vectorPoint[1],
                new PointF(vectorPoint[2].x, vectorPoint[2].y),
                vectorPoint[3]);
        if (angle > 0f) {
            return false;
        }
        angle = linesAngle(vectorPoint[2],
                new PointF(vectorPoint[3].x, vectorPoint[3].y),
                vectorPoint[0]);
        if (angle > 0f) {
            return false;
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /**1、ACTION_DOWN：判断是否和端点的外接矩形相交
         * 2、ACTION_MOVE：端点值加上偏移值
         * **/
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getPointerCount() > 1) {
                    return true;
                }
                for (int i = 0; i < mTouchArea.length; i++) {
                    RectF area = mTouchArea[i];
                    if (area.contains(event.getX(), event.getY())) {
                        //在up之前都绑定这个area和端点
                        mSelectedPoint = i;
                        mPrevTouchPos = new PointF(event.getX(), event.getY());
                        break;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (null != mPrevTouchPos && mSelectedPoint != -1) {
                    float dx = event.getX() - mPrevTouchPos.x;
                    float dy = event.getY() - mPrevTouchPos.y;
                    //禁止超出图片范围：
                    RectF limitedArea = new RectF(mWidth / 2 - mBitmap.getWidth() / 2 * mScale,
                            mHeight / 2 - mBitmap.getHeight() / 2 * mScale,
                            mWidth / 2 + mBitmap.getWidth() / 2 * mScale,
                            mHeight / 2 + mBitmap.getHeight() / 2 * mScale);
                    if (!limitedArea.contains(mVectorPoint[mSelectedPoint].x + dx, mVectorPoint[mSelectedPoint].y + dy)) {
                        break;
                    }
                    //禁止越界导致线条交叉：
                    switch (mSelectedPoint) {
                        case 0:
                            if (!(mVectorPoint[0].x + dx < mVectorPoint[1].x && mVectorPoint[0].y + dy < mVectorPoint[2].y
                                    && mVectorPoint[0].y + dy < mVectorPoint[3].y)) {
                                return true;
                            }
                            break;
                        case 1:
                            if (!(mVectorPoint[1].x + dx > mVectorPoint[0].x && mVectorPoint[1].y + dy < mVectorPoint[2].y
                                    && mVectorPoint[1].y + dy < mVectorPoint[3].y)) {
                                return true;
                            }
                            break;
                        case 2:
                            if (!(mVectorPoint[2].x + dx > mVectorPoint[3].x && mVectorPoint[2].y + dy > mVectorPoint[0].y
                                    && mVectorPoint[2].y + dy > mVectorPoint[1].y)) {
                                return true;
                            }
                            break;
                        case 3:
                            if (!(mVectorPoint[3].x + dx < mVectorPoint[2].x && mVectorPoint[3].y + dy > mVectorPoint[0].y
                                    && mVectorPoint[3].y + dy > mVectorPoint[1].y)) {
                                return true;
                            }
                            break;
                    }
                    //检查角度是否符合要求
                    if(!willAngleFit(dx, dy)) {
                        return true;
                    }
                    mTouchArea[mSelectedPoint].offset(dx, dy);
                    mVectorPoint[mSelectedPoint].offset(dx, dy);
                    invalidate();
                }
                mPrevTouchPos = new PointF(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                mSelectedPoint = -1;
                mPrevTouchPos = null;
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制图片:
        if (null != mBitmap && !mBitmap.isRecycled()) {
            //移动到图片到控件中间并贴图
            Matrix matrix = new Matrix();
            matrix.setTranslate(mWidth / 2f - mBitmap.getWidth() / 2f,
                    mHeight / 2f - mBitmap.getHeight() / 2f);
            matrix.postScale(mScale, mScale, mWidth / 2f, mHeight / 2f);
            mSelectorRectPointPaint.setAlpha(100);
            canvas.drawBitmap(mBitmap, matrix, mSelectorRectPointPaint);
            mSelectorRectPointPaint.setAlpha(255);

            //绘制选择框：
            if (null != mSelectorRectPointPaint) {
                mSelectorRectPointPaint.setStyle(Paint.Style.FILL);
                Path path = new Path();
                for (int i = 0; i < mVectorPoint.length; i++) {
                    PointF pointF = mVectorPoint[i];
                    if (i == 0) {
                        path.moveTo(pointF.x, pointF.y);
                    } else {
                        path.lineTo(pointF.x, pointF.y);
                    }
                }
                path.lineTo(mVectorPoint[0].x, mVectorPoint[0].y);
                //选择框内的图片透明度为0：
                canvas.save();
                canvas.clipPath(path);
                canvas.drawBitmap(mBitmap, matrix, mSelectorRectPointPaint);
                canvas.restore();
                //绘制选择器边框:
                mSelectorRectPointPaint.setStyle(Paint.Style.STROKE);
                canvas.drawPath(path, mSelectorRectPointPaint);
                mSelectorRectPointPaint.setStyle(Paint.Style.FILL);
                for (PointF pointF : mVectorPoint) {
                    //绘制选择器端点
                    canvas.drawCircle(pointF.x, pointF.y, 30, mSelectorRectPointPaint);
                }
            }
        } else {
            Log.e(TAG, "图片为空");
        }
    }

    private void init() {
        //todo cjztest 随便找个图测试一下
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tree);
        areaWidth = (int) MeasurelUtils.convertDpToPixel(100, getContext());
        resetView();
    }

    /**初始化**/
    private void resetView() {
        //根据照片大小覆盖在画布上
        if (null != mBitmap && !mBitmap.isRecycled()) {
            int bmpW = mBitmap.getWidth();
            int bmpH = mBitmap.getHeight();
            float bmpRatio = (float) bmpW / bmpH;
            //计算缩放率
            float ratioW = (float) bmpW / mWidth;
            float ratioH = (float) bmpH / mHeight;
            if (ratioW > ratioH) {
                mScale = 1f / ratioW;
            } else {
                mScale = 1f / ratioH;
            }
        } else {
            Log.e(TAG, "初始化失败");
        }
        //初始化选择框:
        mSelectorRectPointPaint = new Paint();
        mSelectorRectPointPaint.setAntiAlias(true);
        mSelectorRectPointPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mSelectorRectPointPaint.setStrokeWidth(6);
        mSelectorRectPointPaint.setColor(0xFF5555FF);
        if (null != mBitmap && !mBitmap.isRecycled()) {
            //顺时政赋值一圈
            mVectorPoint[0] = new PointF(mWidth / 2 - mBitmap.getWidth() / 2 * mScale,
                    mHeight / 2 - mBitmap.getHeight() / 2 * mScale);
            mVectorPoint[1] = new PointF(mWidth / 2 + mBitmap.getWidth() / 2 * mScale,
                    mHeight / 2 - mBitmap.getHeight() / 2 * mScale);
            mVectorPoint[2] = new PointF(mWidth / 2 + mBitmap.getWidth() / 2 * mScale,
                    mHeight / 2 + mBitmap.getHeight() / 2 * mScale);
            mVectorPoint[3] = new PointF(mWidth / 2 - mBitmap.getWidth() / 2 * mScale,
                    mHeight / 2 + mBitmap.getHeight() / 2 * mScale);
            mTouchArea[0] = new RectF(mVectorPoint[0].x - areaWidth / 2, mVectorPoint[0].y - areaWidth / 2,
                    mVectorPoint[0].x + areaWidth / 2, mVectorPoint[0].y + areaWidth / 2);
            mTouchArea[1] = new RectF(mVectorPoint[1].x - areaWidth / 2, mVectorPoint[1].y - areaWidth / 2,
                    mVectorPoint[1].x + areaWidth / 2, mVectorPoint[1].y + areaWidth / 2);
            mTouchArea[2] = new RectF(mVectorPoint[2].x - areaWidth / 2, mVectorPoint[2].y - areaWidth / 2,
                    mVectorPoint[2].x + areaWidth / 2, mVectorPoint[2].y + areaWidth / 2);
            mTouchArea[3] = new RectF(mVectorPoint[3].x - areaWidth / 2, mVectorPoint[3].y - areaWidth / 2,
                    mVectorPoint[3].x + areaWidth / 2, mVectorPoint[3].y + areaWidth / 2);
        } else {
            Log.e(TAG, "init:图片为空");
        }
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        if (w != mWidth || h != mHeight) { //已经onMeasuer过一次，除非界面大小改动否则不重新初始化view
            mWidth = w;
            mHeight = h;
            init();
        }
        invalidate();
    }
}
