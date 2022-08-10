package com.example.whiteboard;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import java.util.Random;

/**
 * Created by cjz on 2019/4/30.
 * todo ：1、不根据缩放率分级绘制，在比较小的缩放率中，拖动缩放速率相当慢。因为图块全是高分辨率图 2、降采样好像还是没有按照实际大小渲染来的好看
 */

public class MapUnit {
    private Bitmap mTileBitmap;
    private Bitmap mFastCacheBmp;
    private float mMaxScale = 1f;
    private Paint mPaint;
    private RectF mMapRange;
    private int mUnitXY[] = new int[] {Integer.MIN_VALUE, Integer.MIN_VALUE};
    private float mScale = 1f;
    private final boolean mIsDebug = true;
    private int mMapViewWidth = 0;
    private int mMapViewHeight = 0;

    public MapUnit(int tag[], RectF range, float maxScale) {
        mMapRange = new RectF(range);
        Random random = new Random();
        mPaint = new Paint();
        mPaint.setStrokeWidth(2f);
        mPaint.setColor((0xFF000000 | (random.nextInt(255) & 0xFF) << 16 | (random.nextInt(255) & 0xFF) << 8 | (random.nextInt(255) & 0xFF)));
        mPaint.setStyle(Paint.Style.STROKE);
        this.mMaxScale = maxScale;
        setTag(tag);
    }

    /**设置图块容器的宽高数据**/
    public void setMapViewSize(int width, int height) {
        mMapViewWidth = width;
        mMapViewHeight = height;
    }

    public void offset(int dx, int dy) {
        if (null == mMapRange) {
            return;
        }
        mMapRange.offset(dx, dy);
    }

    public void scale(float scale) {
        if (null == mMapRange) {
            return;
        }
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        matrix.mapRect(mMapRange);
        mScale *= scale;
        //缩放率变化了，销毁预览提速缩略图
        clearFastCacheBmp();

    }

    public float getScale() {
        return mScale;
    }

    public PointF getCenter() {
        if (null == mMapRange) {
            return null;
        }
        return new PointF(mMapRange.centerX(), mMapRange.centerY());
    }

    public PointF getXY() {
        if (null == mMapRange) {
            return null;
        }
        return new PointF(mMapRange.left, mMapRange.top);
    }

    public void setX(float x) {
        if (null == mMapRange) {
            return;
        }
        mMapRange.offset(x - mMapRange.left, 0);
    }

    public void setY(float y) {
        if (null == mMapRange) {
            return;
        }
        mMapRange.offset(0, y - mMapRange.top);
    }

    public float getWidth() {
        if (null == mMapRange) {
            return 0;
        }
        return mMapRange.width();
    }

    public float getHeight() {
        if (null == mMapRange) {
            return 0;
        }
        return mMapRange.height();
    }

    /**todo 绘制的时候根据缩放率做一些小图提升绘制效率，缩放/更换标记的时候销毁，绘制的时候创建一次**/
    private void clearFastCacheBmp() {
        if (mFastCacheBmp != null && !mFastCacheBmp.isRecycled()) {
            mFastCacheBmp.recycle();
            mFastCacheBmp = null;
        }
    }

    /**设置标记并更新图块**/
    public void setTag(int unitXY[]) {
        if (unitXY == null) { //同样标号的图块就不重复从外存读图与更新了
            return;
        }
        if (unitXY[0] == mUnitXY[0] && unitXY[1] == mUnitXY[1]) {
            return;
        }
        mUnitXY[0] = unitXY[0];
        mUnitXY[1] = unitXY[1];
        if (mTileBitmap != null) {
            mTileBitmap.recycle();
        }
        mTileBitmap = null;
//        if (mTileBitmap == null) {
//            mTileBitmap = Bitmap.createBitmap((int) (mMapRange.width() * mMaxScale), (int) (mMapRange.height() * mMaxScale), Bitmap.Config.ARGB_8888);
//        }
        //标记变化了，销毁预览提速缩略图
        clearFastCacheBmp();
    }

    public int[] getTag() {
        return mUnitXY;
    }

    public RectF getRange() {
        return mMapRange;
    }


    public void loadTileBmp() {
        if (mTileBitmap == null) {
            mTileBitmap = MapImageManager.getTileImage(getTag(), mMaxScale);
        }
    }

    /**使用前先触发一次loadTileBmp加载图块**/
    protected void onDraw(Canvas canvas) {
        if (null == mMapRange || null == mTileBitmap) {
            return;
        }
        Log.i("onDraw", hashCode() + "");
        //绘制边界框
        if (mIsDebug) {
            canvas.drawRect(mMapRange, mPaint);
        }
        //按照缩放率，减少要绘制的像素量
        if (mFastCacheBmp == null && mTileBitmap != null && mScale < 2f) { //缩放率足够大的时候，显示瓦片数很少，没必要做缩略图了
            int w = (int) mMapRange.width();
            int h = (int) mMapRange.height();
            //使用2的倍数提升缩略图质量
//            mFastCacheBmp = Bitmap.createScaledBitmap(mTileBitmap,  w + (w % 2 == 0 ? 0 : 1), h + (h % 2 == 0 ? 0 : 1), true);
            //这样处理速度快一些
            mFastCacheBmp = Bitmap.createBitmap(w + (w % 2 == 0 ? 0 : 1), h + (h % 2 == 0 ? 0 : 1), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(mFastCacheBmp);
            c.drawBitmap(mTileBitmap, new Rect(0, 0, mTileBitmap.getWidth(), mTileBitmap.getHeight()),
                    new Rect(0, 0, mFastCacheBmp.getWidth(), mFastCacheBmp.getHeight()), null);
        }
        //绘制内容中应放到瓦片的部分
        if (mFastCacheBmp != null && mScale < 2f) {
            canvas.drawBitmap(mFastCacheBmp, new Rect(0, 0, mFastCacheBmp.getWidth(), mFastCacheBmp.getHeight()),
                    mMapRange, null);
        } else {
            canvas.drawBitmap(mTileBitmap, new Rect(0, 0, mTileBitmap.getWidth(), mTileBitmap.getHeight()),
                    mMapRange, null);
        }
        //测试代码:
        if (mIsDebug) {
            Paint paintPen = new Paint();
            paintPen.setStrokeWidth(8f);
            paintPen.setStyle(Paint.Style.FILL);
            paintPen.setColor(Color.RED);
            paintPen.setTextSize(20f);
            paintPen.setAntiAlias(true);
            //绘制自己是第几列第几行的单元
            if (mUnitXY != null) {
                int position[] = mUnitXY;
                canvas.drawText(String.format("UnitX: %d, UnitY: %d", position[0], position[1]), mMapRange.centerX() - 40, mMapRange.centerY(), paintPen);
            }
        }
    }

    public void drawTo(Canvas canvas) {
        if (null == mMapRange || null == mTileBitmap) {
            return;
        }
        canvas.drawBitmap(mTileBitmap, new Rect(0, 0, mTileBitmap.getWidth(), mTileBitmap.getHeight()),
                mMapRange, null);
    }

    /**把白板的内容绘制到瓦片中 **/
    public void drawBmp(Bitmap contentBmp) {
        //没有图块载体，创建一个
        if (mTileBitmap == null) { //cjzmark todo
            mTileBitmap = Bitmap.createBitmap((int) (mMapRange.width() / mScale * mMaxScale),
                    (int) (mMapRange.height() / mScale * mMaxScale), Bitmap.Config.ARGB_8888);
        }
        //已有的话就进行内容替换
        Canvas canvas = new Canvas(mTileBitmap);
        Rect rectSrc = new Rect((int) (mMapRange.left * mMaxScale), (int) (mMapRange.top * mMaxScale),
                (int) (mMapRange.right * mMaxScale), (int) (mMapRange.bottom * mMaxScale));  //大图取材的范围
        Rect rectDst = new Rect(0, 0, mTileBitmap.getWidth(), mTileBitmap.getHeight()); //小图覆盖的范围
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        //如果有些图块与mapview边沿重合
        if (getRange().left < 0 || getRange().right >= mMapViewWidth || getRange().top < 0 || getRange().bottom >= mMapViewHeight) {
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));


//            Rect src = new Rect(rectSrc);
//            Rect dst = new Rect(rectDst);
//            src.offset(-(int) (mMapRange.left * mMaxScale), -(int) (mMapRange.top * mMaxScale));
//            //求相交矩形,src只剩下和dst相交的范围：  //todo 边界部分还是被清除了，可能要考虑clipPath进行处理
//            src.intersect(dst);
            //todo 求出当前mapUnit被显示的范围，设定为裁剪范围：
            RectF rectF = new RectF();
            rectF.left = (0 - getRange().left) / (float) getRange().width() * mTileBitmap.getWidth();
            rectF.right = (getRange().right - mMapViewWidth) / (float) getRange().width() * mTileBitmap.getWidth();
            rectF.top = (0 - getRange().top) / (float) getRange().width() * mTileBitmap.getHeight();
            rectF.bottom = (getRange().bottom - mMapViewHeight) / (float) getRange().width() * mTileBitmap.getHeight();

            Path path = new Path();
//            path.addRect(new RectF(src.left, src.top, src.right, src.bottom), Path.Direction.CCW);
            path.addRect(rectF, Path.Direction.CCW);
            canvas.save();
            canvas.clipPath(path);
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); //避免相同内容有重复叠加绘制的问题
            canvas.drawBitmap(contentBmp, rectSrc, rectDst, null);
            canvas.restore();
//            canvas.drawRect(src, paint);


//            canvas.save();
//            canvas.clipRect(src, paint)
//            canvas.restore();
        }
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));  //todo 和可视区域相交，但是有一部分不相交的，会导致不相交的部分被空像素覆盖，导致画面不连续
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); //避免相同内容有重复叠加绘制的问题
        canvas.drawBitmap(contentBmp, rectSrc, rectDst, null);

        MapImageManager.saveTileImage(getTag(), mTileBitmap, mMaxScale);
        clearFastCacheBmp();
    }
}