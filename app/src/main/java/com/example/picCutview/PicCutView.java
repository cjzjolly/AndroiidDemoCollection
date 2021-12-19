package com.example.picCutview;

/**@author 陈杰柱
 * 2021.6月**/

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class PicCutView extends View {
    private PointF mPrevCurrentCenter = null;
    private float mPrevDistance = Float.MIN_VALUE;
    private float mTotalScale = 1f;
    /**
     * resetView初始化图片时的大小，不变
     **/
    private float mInitScale = 1f;
    /**
     * 触摸点点距队列
     **/
    private Queue<Float> mTouchDistanceQueue = new LinkedBlockingQueue<>();
    private Bitmap mBmp;
    private Matrix mMatrix;
    private float mAvergeX = 0, mAvergeY = 0;
    private int mPrevPointCount = 0;

    /**
     * setPic的方式
     **/
    private int mSetPicWay = 0;
    /**
     * setPicWay = 1时依靠居中选择框
     **/
    private int mCutW, mCutH;
    private float mdX = 0f, mdY = 0f;
    private int mWidth, mHeight;
    private Rect mCutRect = null;
    private Rect mCutRectInitedClone = null;
    /**
     * 默认缩放最小比例为初始化时的100%，如果低于1，则会出现留白
     **/
    private float mScaleMin = 1f;
    /**
     * 默认缩放最大比例为初始化时的2倍
     **/
    private float mScaleMax = 2.0f;
    /**
     * 横向移动不得小于x轴长度的百分比
     **/
    private float mBorderXMin = 0.2f;
    /**
     * 横向移动不得大于x轴长度的百分比
     **/
    private float mBorderXMax = 0.8f;
    /**
     * 横向移动不得小于y轴长度的百分比
     **/
    private float mBorderYMin = 0.2f;
    /**
     * 横向移动不得大于y轴长度的百分比
     **/
    private float mBorderYMax = 0.8f;
    private boolean mDebug = false;
    /**
     * 是否是有4比3框框
     **/
    private boolean mUse4div3rect = true;

    /**
     * 是否可以修改缩放框
     **/
    private boolean mCanChangeRect = true;

    /**
     * 是否可以移动缩放
     **/
    private boolean mCanMoveAndScale = true;

    /**
     * 裁剪框修改后抬起动画
     **/
    private ValueAnimator mTransFinishAnim;

    /**
     * 当前正在拖拽哪个裁剪框角
     **/
    private boolean mCutRectLeftTopIsDragging = false;
    private boolean mCutRectLeftBottomIsDragging = false;
    private boolean mCutRectRightTopIsDragging = false;
    private boolean mCutRectRightBottomIsDragging = false;

    private void init() {
        if (mMatrix == null) {
            mMatrix = new Matrix();
        }
    }

    public PicCutView(Context context) {
        super(context);
        init();
    }

    public PicCutView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PicCutView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setPic(Bitmap bmp) { //使用默认3：4裁剪框
        mBmp = bmp;
        mSetPicWay = 0;
        resetView();
    }

    public void setPic(Bitmap bmp, Rect rect) { //定制选择框大小和位置
        mBmp = bmp;
        setCutPicRect(rect);
        mSetPicWay = 1;
        resetView();
    }

    public void setPic(Bitmap bmp, int cutW, int cutH) { //定制选择框的宽和高但剧中
        mBmp = bmp;
        this.mCutW = cutW;
        this.mCutH = cutH;
        if (mWidth > 0 && mHeight > 0) {
            mCutRect = new Rect((mWidth - mCutW) / 2, (mHeight - mCutH) / 2, (mWidth - mCutW) / 2 + mCutW, (mHeight - mCutH) / 2 + mCutH);
            if (mCanChangeRect) {
                mCutRectInitedClone = new Rect(mCutRect);
            }
        }
        //等onMeasuer时设置Rect
        mSetPicWay = 2;
        resetView();
    }

    public void setPic(Bitmap bmp, boolean use4div3rect) { //使用3：4裁剪框与否，默认值为true
        mBmp = bmp;
        mUse4div3rect = use4div3rect;
        mSetPicWay = 3;
        resetView();
    }


    /**
     * 设置最小能缩放到初始化比例的多少比例
     *
     * @param scaleMin 最小能缩放到初始化比例的多少
     **/
    public void setScaleMin(float scaleMin) {
        this.mScaleMin = scaleMin;
    }

    /**
     * 设置最大能缩放到初始化比例的多少比例
     *
     * @param scaleMax 最大能缩放到初始化比例的多少
     **/
    public void setScaleMax(float scaleMax) {
        this.mScaleMax = scaleMax;
    }

    /**
     * 设置横向移动不得小于x轴长度的百分比
     *
     * @param borderXMin 横向移动不得小于x轴长度的百分比
     **/
    public void setBorderXMin(float borderXMin) {
        this.mBorderXMin = borderXMin;
    }

    /**
     * 设置横向移动不得大于x轴长度的百分比
     *
     * @param borderXMax 横向移动不得大于x轴长度的百分比
     **/
    public void setBorderXMax(float borderXMax) {
        this.mBorderXMax = borderXMax;
    }

    /**
     * 设置横向移动不得小于y轴长度的百分比
     *
     * @param borderYMin 横向移动不得小于y轴长度的百分比
     **/
    public void setBorderYMin(float borderYMin) {
        this.mBorderYMin = borderYMin;
    }

    /**
     * 设置横向移动不得大于y轴长度的百分比
     *
     * @param borderYMax 横向移动不得大于y轴长度的百分比
     **/
    public void setBorderYMax(float borderYMax) {
        this.mBorderYMax = borderYMax;
    }

    /**
     * 每加载一张图片都要重初始化缩放等所有参数，因为每张图片的长宽参数都不一样
     **/
    private void resetView() {
        if (mBmp != null && !mBmp.isRecycled() && mWidth > 0 && mHeight > 0 && mCutRect != null) {
            int bmpH = mBmp.getHeight();
            int bmpW = mBmp.getWidth();
            //复原参数
            mMatrix = new Matrix();
            mTotalScale = 1f;
            mdX = mdY = 0;
            //图片中间部分放在控件上
            float scale;
            //图片缩放到view的宽高可以容纳的程度
            if (bmpW >= bmpH) {//bmpH是短边时
                scale = (float) mCutRect.height() / bmpH;
                mdX -= (bmpW * scale - mWidth) / 2; //移动到view的中间位置
                mdY = mCutRect.top; //移动到Rect上边界
            } else {//bmpW是短边时
                scale = (float) mCutRect.width() / bmpW;
                mdX = mCutRect.left; //移动到Rect左边界
                mdY -= (bmpH * scale - mHeight) / 2;
            }
            //先以左上角0，0点缩放到目标比例
            mMatrix.postScale(scale, scale, 0, 0);
            //再放到中线位置
            mMatrix.postTranslate(mdX, mdY); //等效于mMatrix.postScale(scale, scale, mHeight / 2, mWidth / 2)（以中线为缩放中心缩放）
            mTotalScale = scale;
            mInitScale = scale;
            invalidate();
        }
    }

    /**
     * 缩放函数
     *
     * @param scale 本次缩放量
     * @param px    缩放中心x坐标
     * @param py    缩放中心y坐标
     **/
    public void scale(float scale, float px, float py) {
        if (mCutRect == null) {
            return;
        }
        float relatedTotalScale = mTotalScale / mInitScale; //因为resetView时为了让图片刚好可以被屏幕包裹，本身就做过缩放，所以mTotalScale不为1，但为了方便接下来缩放比例换算，把初始化后的比例看作1。
        if (scale < 1f && relatedTotalScale * scale < mScaleMin) { //如果正在试图缩小，但计算出缩小后的比例值会小于最小限制比例，则取消缩放
            return;
        }
        if (scale >= 1f && relatedTotalScale * scale > mScaleMax && !mCanChangeRect) {
            return;
        }

        if (mMatrix != null && mBmp != null && !mBmp.isRecycled()) {
            mMatrix.postScale(scale, scale, px, py);
            mTotalScale *= scale;
            int bmpW = mBmp.getWidth();
            int bmpH = mBmp.getHeight();
            //不允许缩放时出框
            float matrix[] = new float[9];
            mMatrix.getValues(matrix);
            if (matrix[2] < mCutRect.left && matrix[2] + bmpW * mTotalScale < mCutRect.right) {
                matrix[2] += mCutRect.right - (matrix[2] + bmpW * mTotalScale);
            }
            if (matrix[2] > mCutRect.left && matrix[2] + bmpW * mTotalScale > mCutRect.right) {
                matrix[2] = mCutRect.left;
            }
            if (matrix[5] < mCutRect.top && matrix[5] + bmpH * mTotalScale < mCutRect.bottom) {
                matrix[5] += mCutRect.bottom - (matrix[5] + bmpH * mTotalScale);
            }
            if (matrix[5] > mCutRect.top && matrix[5] + bmpH * mTotalScale > mCutRect.bottom) {
                matrix[5] = mCutRect.top;
            }
            mMatrix.setValues(matrix);

            invalidate();
        }
        Log.i("缩放", String.format("百分比：%f", mTotalScale));
    }

    /**
     * 移动函数
     *
     * @param distanceX 本次移动距离x分量
     * @param distanceY 本次移动距离y分量
     **/
    private void translate(float distanceX, float distanceY) {
        if (mMatrix != null && mBmp != null && !mBmp.isRecycled() && mCutRect != null) {
            //不允许用户把图片完全推出框框外:
            float matrix[] = new float[9];
            int bmpW = mBmp.getWidth();
            int bmpH = mBmp.getHeight();
            mMatrix.getValues(matrix);
            float currentX = matrix[2];
            float currentY = matrix[5];
            //如果本次的distance值会让图片超出指定范围，则去除传入值的数学意义，即归0
            if (currentX + bmpW * mTotalScale + distanceX < mCutRect.left + mCutRect.width() || currentX + distanceX > mCutRect.left) {
                distanceX = 0;
            }
            if (currentY + bmpH * mTotalScale + distanceY < mCutRect.top + mCutRect.height() || currentY + distanceY > mCutRect.top) {
                distanceY = 0;
            }
            mdX += distanceX;
            mdY += distanceY;
            mMatrix.postTranslate(distanceX, distanceY);
            invalidate();
        }
        Log.i("移动", String.format("x位移：%f， y位移：%f", distanceX, distanceY));
    }

    /**
     * 裁剪图片
     **/
    public Bitmap cutPic() {
        if (mBmp != null && !mBmp.isRecycled() && mCutRect != null) {
            int width = mCutRect.width();
            int height = mCutRect.height();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Matrix tempMatrix = new Matrix(mMatrix);
            tempMatrix.postTranslate(-mCutRect.left, -mCutRect.top);  //例如我要截取图片可见部分的右半部分，等同于我选择框不动，图片向左移动相应距离。所以left这个对左边的距离等同于图片要左移的距离
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            canvas.drawBitmap(mBmp, tempMatrix, paint);
            Bitmap scaledBmp = Bitmap.createScaledBitmap(bitmap, (int) (width / mTotalScale), (int) (height / mTotalScale), true);
            bitmap.recycle();
            return scaledBmp;
        }
        return null;
    }

    /**
     * 设置图片裁剪范围
     **/
    private void setCutPicRect(Rect rect) {
        this.mCutRect = new Rect(rect);
        if (mCanChangeRect) {
            this.mCutRectInitedClone = new Rect(mCutRect);
        }
    }

    /* 获取测量大小*/
    private int getMySize(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        result = specSize;//确切大小,所以将得到的尺寸给view
        return result;
    }

    private float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mBmp == null || mBmp.isRecycled()) {
            return true;
        }
        int offset = (int) convertDpToPixel(50, getContext()); //边框4角4个点在多大的范围内可以接受触摸事件实现拖动缩放
        int smallestEdgeSize = (int) convertDpToPixel(100, getContext());  //裁剪框最小能有多小
        Rect leftTop = new Rect(mCutRect.left - offset, mCutRect.top - offset, mCutRect.left + offset, mCutRect.top + offset);
        Rect rightTop = new Rect(mCutRect.right - offset, mCutRect.top - offset, mCutRect.right + offset, mCutRect.top + offset);
        Rect leftBottom = new Rect(mCutRect.left - offset, mCutRect.bottom - offset, mCutRect.left + offset, mCutRect.bottom + offset);
        Rect rightBottom = new Rect(mCutRect.right - offset, mCutRect.bottom - offset, mCutRect.right + offset, mCutRect.bottom + offset);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mCutRectLeftTopIsDragging = false;
                mCutRectLeftBottomIsDragging = false;
                mCutRectRightTopIsDragging = false;
                mCutRectRightBottomIsDragging = false;
                mPrevDistance = 0;
                mPrevPointCount = event.getPointerCount();
                //算出移动中心坐标、点间距离
                for (int i = 0; i < event.getPointerCount(); i++) {
                    mAvergeX += event.getX(i);
                    mAvergeY += event.getY(i);
                    if (i + 1 < event.getPointerCount()) {
                        mPrevDistance += Math.sqrt(Math.pow(event.getX(i + 1) - event.getX(i), 2) + Math.pow(event.getY(i + 1) - event.getY(i), 2));
                    }
                }
                mAvergeX /= event.getPointerCount();
                mAvergeY /= event.getPointerCount();
                if (mPrevCurrentCenter == null) {
                    mPrevCurrentCenter = new PointF(mAvergeX, mAvergeY);
                } else {
                    mPrevCurrentCenter.set(mAvergeX, mAvergeY);
                }
                if (mCanChangeRect && event.getPointerCount() == 1) { //如果允许拖动裁剪框而且手指数只有1
                    //判断是否是选择rect的4个角度附近
                    if (leftTop.contains((int) event.getX(), (int) event.getY())) {
                        mCanMoveAndScale = false;
                        mCutRectLeftTopIsDragging = true;
                    } else if (rightTop.contains((int) event.getX(), (int) event.getY())) {
                        mCanMoveAndScale = false;
                        mCutRectRightTopIsDragging = true;
                    } else if (leftBottom.contains((int) event.getX(), (int) event.getY())) {
                        mCanMoveAndScale = false;
                        mCutRectLeftBottomIsDragging = true;
                    } else if (rightBottom.contains((int) event.getX(), (int) event.getY())) {
                        mCanMoveAndScale = false;
                        mCutRectRightBottomIsDragging = true;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                mAvergeX = 0;
                mAvergeY = 0;
                float nowDistance = 0;
                //算出移动中心坐标、点间距离
                for (int i = 0; i < event.getPointerCount(); i++) {
                    mAvergeX += event.getX(i);
                    mAvergeY += event.getY(i);
                    if (i + 1 < event.getPointerCount()) {
                        nowDistance += Math.sqrt(Math.pow(event.getX(i + 1) - event.getX(i), 2) + Math.pow(event.getY(i + 1) - event.getY(i), 2));
                    }
                }
                //现在的点间距离 除以 上次点间距离 这次得到缩放比例
                mAvergeX /= event.getPointerCount();
                mAvergeY /= event.getPointerCount();
                if ((mPrevPointCount != event.getPointerCount()) || event.getPointerCount() <= 1 || mPrevPointCount <= 1) { //触摸点数突然改变 或者 触摸点不超过2，不允许缩放
                    mPrevDistance = nowDistance = 0;
                }
                //检测上次手指之间的距离mPrevDistance和这次的nowDistance之间的长度差，以这次/上次所谓缩放比例。如果缩放数据有效，则进行平均平滑化并且进行缩放
                if (mPrevDistance > 0 && nowDistance > 0) {
                    mTouchDistanceQueue.add(nowDistance / mPrevDistance);
                    if (mTouchDistanceQueue.size() >= 6) {
                        Float point[] = new Float[mTouchDistanceQueue.size()];
                        mTouchDistanceQueue.toArray(point);
                        float avergDistance = 0;
                        for (int i = 0; i < point.length; i++) {
                            avergDistance += point[i];
                        }
                        avergDistance /= point.length;
                        if (mCanMoveAndScale) {
                            scale((float) Math.sqrt(avergDistance), mAvergeX, mAvergeY);
                        }
                        while (mTouchDistanceQueue.size() > 6) {
                            mTouchDistanceQueue.poll();
                        }
                    }
                }
                mPrevPointCount = event.getPointerCount();
                mPrevDistance = nowDistance;
                //当前坐标 - 上次坐标 = 偏移值，然后进行位置偏移
                if (mPrevCurrentCenter == null) {
                    mPrevCurrentCenter = new PointF(mAvergeX, mAvergeY);
                } else if (event.getPointerCount() > 0) { //至少要保证真正地有一个点按着移动
                    if (mCanChangeRect && event.getPointerCount() == 1 && !mCanMoveAndScale) { //如果允许拖动裁剪框而且手指数只有1
                        Rect mCutRectBeforeChange = new Rect(mCutRect);
                        //判断是否是选择rect的4个角度附近，是就跟随手指移动修改
                        float wTohRatioBeforeChange = (float) mCutRectInitedClone.width() / mCutRectInitedClone.height();
                        float hTowRatioBeforeChange = 1 / wTohRatioBeforeChange;
                        if (leftTop.contains((int) event.getX(), (int) event.getY()) || mCutRectLeftTopIsDragging) {
                            mCutRect.left += event.getX(0) - mPrevCurrentCenter.x;
                            mCutRect.top += event.getY(0) - mPrevCurrentCenter.y;
                            mCutRect.bottom = (int) (mCutRect.top + mCutRect.width() * hTowRatioBeforeChange);  //通过重新设定符合初始化时裁剪框的比例的高，来保持比例
                        } else if (rightTop.contains((int) event.getX(), (int) event.getY()) || mCutRectRightTopIsDragging) {
                            mCutRect.right += event.getX(0) - mPrevCurrentCenter.x;
                            mCutRect.top += event.getY(0) - mPrevCurrentCenter.y;
                            mCutRect.bottom = (int) (mCutRect.top + mCutRect.width() * hTowRatioBeforeChange);  //通过重新设定符合初始化时裁剪框的比例的高，来保持比例
                        } else if (leftBottom.contains((int) event.getX(), (int) event.getY()) || mCutRectLeftBottomIsDragging) {
                            mCutRect.left += event.getX(0) - mPrevCurrentCenter.x;
                            mCutRect.bottom += event.getY(0) - mPrevCurrentCenter.y;
                            mCutRect.top = (int) (mCutRect.bottom - mCutRect.width() * hTowRatioBeforeChange);  //通过重新设定符合初始化时裁剪框的比例的高，来保持比例
                        } else if (rightBottom.contains((int) event.getX(), (int) event.getY()) || mCutRectRightBottomIsDragging) {
                            mCutRect.right += event.getX(0) - mPrevCurrentCenter.x;
                            mCutRect.bottom += event.getY(0) - mPrevCurrentCenter.y;
                            mCutRect.top = (int) (mCutRect.bottom - mCutRect.width() * hTowRatioBeforeChange);  //通过重新设定符合初始化时裁剪框的比例的高，来保持比例
                        }
                        //如果宽或者高已经到最小面积或最大面积，将修改无效化
                        if (mCutRect.width() > mCutRectInitedClone.width() || mCutRect.width() < smallestEdgeSize
                                || mCutRect.height() > mCutRectInitedClone.height() || mCutRect.height() < smallestEdgeSize) {
                            mCutRect = new Rect(mCutRectBeforeChange);
                        }
                        //不允许把框推出画面之外:
                        if (mMatrix != null && mBmp != null && !mBmp.isRecycled()) {
                            int bmpW = mBmp.getWidth();
                            int bmpH = mBmp.getHeight();
                            float matrix[] = new float[9];
                            mMatrix.getValues(matrix);
                            float currentX = matrix[2];
                            float currentY = matrix[5];
                            float scale = matrix[0];
                            if (mCutRect.left < currentX || mCutRect.top < currentY || mCutRect.right > currentX + bmpW * scale
                                    || mCutRect.bottom > currentY + bmpH * scale) {
                                mCutRect = mCutRectBeforeChange;
                            }
                        }
                        invalidate();
                    }
                    //如果没有选中控件4个角度则直接拖动图片
                    if (mCanMoveAndScale) {
                        translate(event.getX(0) - mPrevCurrentCenter.x, event.getY(0) - mPrevCurrentCenter.y);
                    }
                    mPrevCurrentCenter.set(event.getX(0), event.getY(0));
                }
                break;
            case MotionEvent.ACTION_UP:
                //抬起，清理干净数据
                mAvergeX = 0;
                mAvergeY = 0;
                mTouchDistanceQueue.clear();
                if (mCanChangeRect) { //抬手之后按照缩放rect缩放大小
                    int scalePx = mCutRect.centerX();
                    int scalePy = mCutRect.centerY();
                    float scale = (float) mCutRectInitedClone.width() / mCutRect.width();
                    scale(scale, scalePx, scalePy);
                    translate((scalePx - mCutRect.centerX()) * scale, (scalePy - mCutRect.centerY()) * scale);
                    mCutRect = new Rect(mCutRectInitedClone);
                    invalidate();
                }
                mCanMoveAndScale = true;
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mCutRect != null) {
            if (mBmp != null && !mBmp.isRecycled()) {
                Paint p = new Paint();
                p.setAlpha(128);
                canvas.drawBitmap(mBmp, mMatrix, p);

                p.setAlpha(255);
                canvas.save();
                canvas.clipRect(mCutRect); //只选择框内进行不透明渲染
                canvas.drawBitmap(mBmp, mMatrix, p);
                canvas.restore(); //还原绘制区域
            }
            Paint p = new Paint();
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(3);
            p.setColor(Color.WHITE);
            canvas.drawRect(mCutRect, p);

            p.setStrokeWidth(9);
            p.setStrokeCap(Paint.Cap.SQUARE);
            float cornerLength = mCutRect.width() > mCutRect.height() ? mCutRect.width() * 0.05f : mCutRect.height() * 0.05f;
            //左上角
            canvas.drawLine((float) mCutRect.left, (float) mCutRect.top, mCutRect.left + cornerLength, mCutRect.top, p);
            canvas.drawLine((float) mCutRect.left, (float) mCutRect.top, mCutRect.left, mCutRect.top + cornerLength, p);
            //左下角
            canvas.drawLine((float) mCutRect.left, (float) mCutRect.bottom, mCutRect.left + cornerLength, mCutRect.bottom, p);
            canvas.drawLine((float) mCutRect.left, (float) mCutRect.bottom, mCutRect.left, mCutRect.bottom - cornerLength, p);
            //右上角
            canvas.drawLine((float) mCutRect.right, (float) mCutRect.top, mCutRect.right - cornerLength, mCutRect.top, p);
            canvas.drawLine((float) mCutRect.right, (float) mCutRect.top, mCutRect.right, mCutRect.top + cornerLength, p);
            //右下角
            canvas.drawLine((float) mCutRect.right, (float) mCutRect.bottom, mCutRect.right - cornerLength, mCutRect.bottom, p);
            canvas.drawLine((float) mCutRect.right, (float) mCutRect.bottom, mCutRect.right, mCutRect.bottom - cornerLength, p);
            //如果正在拉伸移动裁剪框，则显示九宫格：
            if (!mCanMoveAndScale) {
                Paint nineRectPaint = new Paint(p);
                nineRectPaint.setAlpha(100);
                nineRectPaint.setStrokeWidth(3);
                canvas.drawLine((float) mCutRect.left, (float) mCutRect.top + mCutRect.height() * 1 / 3, (float) mCutRect.left + mCutRect.width(), (float) mCutRect.top + mCutRect.height() * 1 / 3, nineRectPaint);
                canvas.drawLine((float) mCutRect.left, (float) mCutRect.top + mCutRect.height() * 2 / 3, (float) mCutRect.left + mCutRect.width(), (float) mCutRect.top + mCutRect.height() * 2 / 3, nineRectPaint);
                canvas.drawLine((float) mCutRect.left + mCutRect.width() * 1 / 3, (float) mCutRect.top, (float) mCutRect.left + mCutRect.width() * 1 / 3, (float) mCutRect.top + mCutRect.height(), nineRectPaint);
                canvas.drawLine((float) mCutRect.left + mCutRect.width() * 2 / 3, (float) mCutRect.top, (float) mCutRect.left + mCutRect.width() * 2 / 3, (float) mCutRect.top + mCutRect.height(), nineRectPaint);
            }

            //绘制提示语:
            String tipsStr = String.format("已截取分辨率:%d * %d", (int) (mCutRect.width() / mTotalScale), (int) (mCutRect.height() / mTotalScale));
            String tipsStr2 = "建议尺寸≥600*800";
            if (mCutRect.width() == mCutRect.height()) {
                tipsStr2 = "建议尺寸≥600*600";
            }
            Paint tipsPaint = new Paint();
            tipsPaint.setTextSize(convertDpToPixel(12, getContext()));
            tipsPaint.setColor(Color.WHITE);
            tipsPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            tipsPaint.setStrokeWidth(1);
            float textLen = tipsPaint.measureText(tipsStr);
            canvas.drawText(tipsStr, (mWidth - textLen) / 2, mCutRect.bottom + tipsPaint.measureText("1") * 2, tipsPaint);
            tipsPaint.setColor(0xFFCCCCCC);
            textLen = tipsPaint.measureText(tipsStr2);
            canvas.drawText(tipsStr2, (mWidth - textLen) / 2, mCutRect.bottom + tipsPaint.measureText("1") * 6, tipsPaint);

            if (mDebug) {
                p.setColor(Color.RED);
                float matrix[] = new float[9];
                mMatrix.getValues(matrix);
                canvas.drawRect(new Rect((int) matrix[2], (int) matrix[5], (int) (matrix[2] + mBmp.getWidth() * mTotalScale), (int) (matrix[5] + mBmp.getHeight() * mTotalScale)), p);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w = getMySize(widthMeasureSpec);
        int h = getMySize(heightMeasureSpec);
        if (w != mWidth || h != mHeight) { //已经onMeasuer过一次，除非界面大小改动否则不重新初始化view
            mWidth = getMySize(widthMeasureSpec);
            mHeight = getMySize(heightMeasureSpec);
            switch (mSetPicWay) {
                case 0: {//根据图片选择裁剪框大小
                    float wRatio = 0.9f;
                    int cutW = (int) (mWidth * wRatio);
                    int cutH = (int) (mWidth * wRatio * 4 / 3); //默认H:W = 4:3
                    if (mBmp != null && !mBmp.isRecycled()) {
//                        float bmpRatio = mBmp.getWidth() >= mBmp.getHeight() ? (float) mBmp.getWidth() / mBmp.getHeight() : (float) mBmp.getHeight() / mBmp.getWidth();
                        float bmpRatio = (float) mBmp.getWidth() / mBmp.getHeight();
                        if (bmpRatio > 1.3f) { //如果图片比较长，使用正方形选择框
                            if (cutW >= cutH) {
                                cutW = cutH;
                            } else {
                                cutH = cutW;
                            }
                        } //否则用长条形选择框
                    }
                    mCutRect = new Rect((mWidth - cutW) / 2, (mHeight - cutH) / 2, (mWidth - cutW) / 2 + cutW, (mHeight - cutH) / 2 + cutH);
                    if (mCanChangeRect) {
                        mCutRectInitedClone = new Rect(mCutRect);
                    }
                    break;
                }
                case 2:
                    mCutRect = new Rect((mWidth - mCutW) / 2, (mHeight - mCutH) / 2, (mWidth - mCutW) / 2 + mCutW, (mHeight - mCutH) / 2 + mCutH);
                    if (mCanChangeRect) {
                        mCutRectInitedClone = new Rect(mCutRect);
                    }
                    break;
                case 3: {
                    float wRatio = 0.9f;
                    int cutW = (int) (mWidth * wRatio);
                    int cutH;
                    if (mUse4div3rect) {
                        cutH = (int) (mWidth * wRatio * 4 / 3); //默认H:W = 4:3
                    } else {
                        cutH = cutW;
                    }
                    mCutRect = new Rect((mWidth - cutW) / 2, (mHeight - cutH) / 2, (mWidth - cutW) / 2 + cutW, (mHeight - cutH) / 2 + cutH);
                    if (mCanChangeRect) {
                        mCutRectInitedClone = new Rect(mCutRect);
                    }
                    break;
                }
            }
            resetView();
        }
    }


}
