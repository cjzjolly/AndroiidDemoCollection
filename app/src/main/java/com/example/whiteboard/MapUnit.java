package com.example.whiteboard;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
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
    private int mUnitXY[] = new int[2];
    private float mScale = 1f;

    public MapUnit(int tag[], RectF range, float maxScale) {
        mMapRange = new RectF(range);
        Random random = new Random();
        mPaint = new Paint();
        mPaint.setStrokeWidth(2f);
        mPaint.setColor((0xFF000000 | (random.nextInt(255) & 0xFF) << 16 | (random.nextInt(255) & 0xFF) << 8 | (random.nextInt(255) & 0xFF)));
        mPaint.setStyle(Paint.Style.STROKE);
        this.mMaxScale = maxScale;
        setTag(tag);
        //todo 创建载体位图，以后从外存中读写，暂时先用内存里面建立的作为测试:


//        Canvas canvas = new Canvas(mTileBitmap);
//        Paint paint = new Paint();
//        paint.setStrokeWidth(12f);
//        paint.setColor(Color.BLACK);
//        paint.setStyle(Paint.Style.STROKE);
//        canvas.drawCircle(mTileBitmap.getWidth() / 2, mTileBitmap.getHeight() / 2, mTileBitmap.getHeight() / 3, paint);
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


    public void setTag(int unitXY[]) {
        if (unitXY == null) { //同样标号的图块就不重复从外存读图与更新了
            return;
        }
        if (unitXY[0] == mUnitXY[0] && unitXY[1] == mUnitXY[1]) {
            return;
        }
        mUnitXY[0] = unitXY[0];
        mUnitXY[1] = unitXY[1];
        mTileBitmap = MapImageManager.getTileImage(unitXY, mMaxScale);
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

    protected void onDraw(Canvas canvas) {
        if (null == mMapRange || null == mTileBitmap) {
            return;
        }
        Log.i("onDraw", hashCode() + "");
        //绘制边界框
        canvas.drawRect(mMapRange, mPaint);
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
        Paint paintPen = new Paint();
        paintPen.setStrokeWidth(8f);
        paintPen.setStyle(Paint.Style.FILL);
        paintPen.setColor(Color.RED);
        paintPen.setTextSize(20f);
        paintPen.setAntiAlias(true);
        //绘制自己是第几列第几行的单元
        if (mUnitXY != null) {
            int position[] = mUnitXY;
            canvas.drawText(String.format("UnitX: %d, UnitY: %d", position[0], position[1]),  mMapRange.centerX() - 40, mMapRange.centerY(), paintPen);
        }
    }

    /**把白板的内容绘制到瓦片中**/
    public void drawBmp(Bitmap contentBmp) {
        //没有图块载体，创建一个
        if (mTileBitmap == null) {
            mTileBitmap = Bitmap.createBitmap((int) (mMapRange.width() / mScale * mMaxScale),
                    (int) (mMapRange.height() / mScale * mMaxScale), Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(mTileBitmap);
        Rect rectSrc = new Rect((int) (mMapRange.left * mMaxScale), (int) (mMapRange.top * mMaxScale),
                (int) (mMapRange.right * mMaxScale), (int) (mMapRange.bottom * mMaxScale));
        Rect rectDst = new Rect(0, 0, mTileBitmap.getWidth(), mTileBitmap.getHeight());
        canvas.drawBitmap(contentBmp, rectSrc, rectDst, null);
        MapImageManager.saveTileImage(getTag(), mTileBitmap, mMaxScale);
        clearFastCacheBmp();
    }
}