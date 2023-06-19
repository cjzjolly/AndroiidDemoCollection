package com.example.photoCutter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

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
    /**用户设置的端点**/
    private PointF mVectorPointByUserSet[] = null;
    /**用户裁剪的范围**/
    private Path mCutterClipPath = null;
    /**当前触摸事件**/
    private int mCurrentAction = MotionEvent.ACTION_UP;

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
        mCurrentAction = event.getAction();
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
//                    switch (mSelectedPoint) {
//                        case 0:
//                            if (!(mVectorPoint[0].x + dx < mVectorPoint[1].x && mVectorPoint[0].y + dy < mVectorPoint[2].y
//                                    && mVectorPoint[0].y + dy < mVectorPoint[3].y)) {
//                                return true;
//                            }
//                            break;
//                        case 1:
//                            if (!(mVectorPoint[1].x + dx > mVectorPoint[0].x && mVectorPoint[1].y + dy < mVectorPoint[2].y
//                                    && mVectorPoint[1].y + dy < mVectorPoint[3].y)) {
//                                return true;
//                            }
//                            break;
//                        case 2:
//                            if (!(mVectorPoint[2].x + dx > mVectorPoint[3].x && mVectorPoint[2].y + dy > mVectorPoint[0].y
//                                    && mVectorPoint[2].y + dy > mVectorPoint[1].y)) {
//                                return true;
//                            }
//                            break;
//                        case 3:
//                            if (!(mVectorPoint[3].x + dx < mVectorPoint[2].x && mVectorPoint[3].y + dy > mVectorPoint[0].y
//                                    && mVectorPoint[3].y + dy > mVectorPoint[1].y)) {
//                                return true;
//                            }
//                            break;
//                    }
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
                invalidate();
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //canvas.drawColor(Color.GREEN); //for debug
        super.onDraw(canvas);
        //绘制图片:
        if (null != mBitmap && !mBitmap.isRecycled()) {
            //移动到图片到控件中间并贴图
            Matrix matrix = new Matrix();
            matrix.setTranslate(mWidth / 2f - mBitmap.getWidth() / 2f,
                    mHeight / 2f - mBitmap.getHeight() / 2f);
            matrix.postScale(mScale, mScale, mWidth / 2f, mHeight / 2f);
            mSelectorRectPointPaint.setAlpha(150);
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
                //记录这次的path
                mCutterClipPath = new Path(path);
                //绘制局部放大图
                if (null == mVectorPoint || mSelectedPoint < 0 || mSelectedPoint >= mVectorPoint.length) {
                    return;
                }
                PointF selectPoint = mVectorPoint[mSelectedPoint];
                if (null != selectPoint && MotionEvent.ACTION_UP != mCurrentAction) {
                    Path scopeArea = new Path();
                    int w = (int) MeasurelUtils.convertDpToPixel(100, getContext());
                    int h = (int) MeasurelUtils.convertDpToPixel(100, getContext());
                    //手指碰到绘制区域要避开
                    RectF rect = new RectF(0, 0, w, h);
                    if (rect.contains(selectPoint.x, selectPoint.y)) {
                        rect = new RectF(mWidth - w, 0, mWidth, h);
                    }
                    scopeArea.addRoundRect(rect, 10f, 10f,
                            Path.Direction.CCW);
                    canvas.save();
                    canvas.clipPath(scopeArea);
                    float offsetX = ((mWidth - mBitmap.getWidth() * mScale) / 2f); //去除计算中纳入的控件黑边，以防图像在控件缩放后两边有黑边时，导致裁剪时把偏移量多算了控件上的黑边范围导致严重误差
                    Point point = new Point((int) ((selectPoint.x - offsetX) * (1f / mScale)),
                            (int) ((selectPoint.y - (mHeight / 2f - mBitmap.getHeight() / 2f * mScale)) * (1f / mScale)));
                    Matrix scopeMatrix = new Matrix();
                    scopeMatrix.postScale(mScale * 2f, mScale * 2f, (point.x + w / 2), (point.y + h / 2));
                    scopeMatrix.setTranslate((-point.x + w / 2), (-point.y + h / 2));
                    canvas.drawColor(Color.BLACK);
                    canvas.drawBitmap(mBitmap, scopeMatrix, null);
                    mSelectorRectPointPaint.setStyle(Paint.Style.STROKE);
                    canvas.drawPath(scopeArea, mSelectorRectPointPaint);
                    canvas.drawLine(rect.centerX() - w / 10, rect.centerY(), rect.centerX() + w / 10, rect.centerY(), mSelectorRectPointPaint);
                    canvas.drawLine(rect.centerX(), rect.centerY() - w / 10, rect.centerX(), rect.centerY() + w / 10, mSelectorRectPointPaint);
                    canvas.restore();
                }
            }
        } else {
            Log.e(TAG, "图片为空");
        }
    }

    private void init() {
        setLayerType(LAYER_TYPE_SOFTWARE, null); //防止部分设备出现显示bug，禁止硬件加速
        areaWidth = (int) MeasurelUtils.convertDpToPixel(100, getContext());
        resetView();
    }

    /**设置要显示的图片，并重置控件**/
    public void setBitmap(Bitmap bitmap) {
        if (null == bitmap || bitmap.isRecycled()) {
            Log.e(TAG, "图片载入错误");
            return;
        }
        mBitmap = bitmap;
        resetView();
    }

    /**设置旋转量**/
    public void rotate(int rotate) throws Exception {
        if (rotate % 90 != 0) {
            throw new Exception("不允许使用非90度倍率的旋转角度");
        }
        setRotation(rotate); //整个控件进行旋转
        if (null == mBitmap) {
            return;
        }
        float ratioW = (float) mBitmap.getHeight() / mWidth;
        float ratioH = (float) mBitmap.getWidth() / mHeight;
        float scale = 1f;
        if (ratioW < ratioH) {
            scale = 1f / ratioW;
        } else {
            scale = 1f / ratioH;
        }
        if (getRotation() % 180 == 0) {
            setScaleX(1f);
            setScaleY(1f);
        } else {
            setScaleX(scale);
            setScaleY(scale);
        }
        invalidate();
    }

    /**设置选择点**/
    public void setSelectorPointOnPhoto(PointF leftTop, PointF rightTop,
                                        PointF leftBottom, PointF rightBottom) {
        //算出用户指定的在图片中点，在控件中已缩放后的相对位置
        if (null == mBitmap || mBitmap.isRecycled()) {
            Log.e(TAG, "setSelectorPointOnPhoto：图片空错误");
            return;
        }
        mVectorPointByUserSet = new PointF[4];
        mVectorPointByUserSet[0] = leftTop;
        mVectorPointByUserSet[1] = rightTop;
        mVectorPointByUserSet[2] = rightBottom;
        mVectorPointByUserSet[3] = leftBottom;
        resetView();
    }

    /**初始化**/
    private void resetView() {
        //根据照片大小覆盖在画布上
        if (null != mBitmap && !mBitmap.isRecycled()) {
            int bmpW = mBitmap.getWidth();
            int bmpH = mBitmap.getHeight();
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
        mSelectorRectPointPaint.setColor(0xFF5588FF);
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
            //如果有用户设置的触摸点，就根据设置改一下默认位置
            if (null != mVectorPointByUserSet) {
                PointF newLeftTop = new PointF();
                PointF newRightTop = new PointF();
                PointF newRightBottom = new PointF();
                PointF newLeftBottom = new PointF();
                newLeftTop.x = mVectorPoint[0].x + mVectorPointByUserSet[0].x * mScale;
                newLeftTop.y = mVectorPoint[0].y + mVectorPointByUserSet[0].y * mScale;
                newRightTop.x = mVectorPoint[1].x + (mVectorPointByUserSet[1].x - mBitmap.getWidth()) * mScale;
                newRightTop.y = mVectorPoint[1].y + mVectorPointByUserSet[1].y * mScale;
                newRightBottom.x = mVectorPoint[2].x + (mVectorPointByUserSet[2].x - mBitmap.getWidth()) * mScale;
                newRightBottom.y = mVectorPoint[2].y + (mVectorPointByUserSet[2].y - mBitmap.getHeight()) * mScale;
                newLeftBottom.x = mVectorPoint[3].x + mVectorPointByUserSet[3].x * mScale;
                newLeftBottom.y = mVectorPoint[3].y + (mVectorPointByUserSet[3].y - mBitmap.getHeight()) * mScale;
                //添加一下位置检查，否则如果传入的值不对会干扰代码逻辑
                if (!(newLeftTop.x < mVectorPoint[0].x || newLeftTop.y < mVectorPoint[0].y)) {
                    mVectorPoint[0] = newLeftTop;
                }
                if (!(newRightTop.x > mVectorPoint[1].x || newRightTop.y < mVectorPoint[1].y)) {
                    mVectorPoint[1] = newRightTop;
                }
                if (!(newRightBottom.x > mVectorPoint[2].x || newRightBottom.y > mVectorPoint[2].y)) {
                    mVectorPoint[2] = newRightBottom;
                }
                if (!(newLeftBottom.x < mVectorPoint[3].x || newLeftBottom.y > mVectorPoint[3].y)) {
                    mVectorPoint[3] = newLeftBottom;
                }
            }
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

    /**对向量继续进行旋转**/
    private float[] rotate2d(float vec[], double angle) throws Exception {
        if (null == vec) {
            return null;
        }
        if (vec.length > 2) {
            throw new Exception("不接受超过2D的坐标");
        }
        double angleRad = Math.toRadians(angle);
        float rotatedVec[] = new float[2];
        rotatedVec[0] = (float) (Math.cos(angleRad) * vec[0] - Math.sin(angleRad) * vec[1]);
        rotatedVec[1] = (float) (Math.sin(angleRad) * vec[0] + Math.cos(angleRad) * vec[1]);
        return rotatedVec;
    }

    public Bitmap cutPhoto() {
        if (null == mBitmap || mBitmap.isRecycled()) {
            Log.e(TAG, "当前没有图片");
            return null;
        }
        if (null == mVectorPoint) {
            Log.e(TAG, "当前没有初始化选择点");
            return null;
        }
        if (null == mCutterClipPath) {
            Log.e(TAG, "当前没有用户裁剪path");
            return null;
        }
        //算出裁剪框的裁剪范围的外接矩形:
        float offsetX = ((mWidth - mBitmap.getWidth() * mScale) / 2f); //去除计算中纳入的控件黑边，以防图像在控件缩放后两边有黑边时，导致裁剪时把偏移量多算了控件上的黑边范围导致严重误差
        float offsetY = ((mHeight - mBitmap.getHeight() * mScale) / 2f); //去除计算中纳入的控件黑边，以防图像在控件缩放后两边有黑边时，导致裁剪时把偏移量多算了控件上的黑边范围导致严重误差

        float left = Math.min(mVectorPoint[0].x, mVectorPoint[3].x);
        float top = Math.min(mVectorPoint[0].y, mVectorPoint[1].y);
        float right = Math.max(mVectorPoint[1].x, mVectorPoint[2].x);
        float bottom = Math.max(mVectorPoint[2].y, mVectorPoint[3].y);
        Rect rect = new Rect((int) left, (int) top, (int) right, (int) bottom);
        Bitmap bitmap = Bitmap.createBitmap((int) ((float) rect.width() * (1f / mScale)), (int) ((float) rect.height() * (1f / mScale)), Bitmap.Config.ARGB_8888);  //大小不定， 只看外接矩形的大小
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.TRANSPARENT);

        //将裁剪范围应用于画布
        Path cutterPathClone = new Path(mCutterClipPath);
        Matrix pathMatrix = new Matrix();
        pathMatrix.postTranslate(-rect.left, -rect.top); //因为left和top是应用于view的canvas之上的，还要转换为当前bmp画布的坐标，抹去view的canvas留下的间隙偏移量
        pathMatrix.postScale(1f / mScale, 1f / mScale);
        cutterPathClone.transform(pathMatrix);
        canvas.clipPath(cutterPathClone);
        //canvas.drawColor(Color.RED); //for debug

        Matrix bmpMatrix = new Matrix(); //没啥bug了
        bmpMatrix.setTranslate(-(rect.left - offsetX) / mScale, -(rect.top - offsetY) / mScale);
        canvas.drawBitmap(mBitmap, bmpMatrix, null);

        return bitmap;
    }
}
