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
    private float mMaxScale = 1f;
    private Paint mPaint;
    private RectF mMapRange;
    private int mUnitXY[] = new int[2];
    private float mScale = 1f;

    public MapUnit(RectF range, float maxScale) {
        mMapRange = new RectF(range);
        Random random = new Random();
        mPaint = new Paint();
        mPaint.setStrokeWidth(8f);
        mPaint.setColor((0xFF000000 | (random.nextInt(255) & 0xFF) << 16 | (random.nextInt(255) & 0xFF) << 8 | (random.nextInt(255) & 0xFF)));
        mPaint.setStyle(Paint.Style.FILL);
        this.mMaxScale = maxScale;
        //todo 创建载体位图，以后从外存中读写，暂时先用内存里面建立的作为测试:
        this.mTileBitmap = Bitmap.createBitmap((int) (mMapRange.width() * mMaxScale), (int) (mMapRange.height() * mMaxScale), Bitmap.Config.ARGB_8888);



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


    public void setTag(int unitXY[]) {
        mUnitXY[0] = unitXY[0];
        mUnitXY[1] = unitXY[1];
    }

    public int[] getTag() {
        return mUnitXY;
    }

    public RectF getRange() {
        return mMapRange;
    }

    protected void onDraw(Canvas canvas) {
        if (null == mMapRange) {
            return;
        }
        Log.i("onDraw", hashCode() + "");
        //绘制随机色背景
//        canvas.drawRect(mMapRange, mPaint);


        canvas.drawBitmap(mTileBitmap, new Rect(0, 0, mTileBitmap.getWidth(), mTileBitmap.getHeight()),
                mMapRange, null);



        Paint paintPen = new Paint();
        paintPen.setStrokeWidth(8f);
        paintPen.setStyle(Paint.Style.FILL);
        paintPen.setColor(Color.BLACK);
        paintPen.setTextSize(20f);
        paintPen.setAntiAlias(true);
        //绘制自己是第几列第几行的单元
        if(mUnitXY != null) {
            int position[] = mUnitXY;
            canvas.drawText(String.format("UnitX: %d, UnitY: %d", position[0], position[1]),  mMapRange.centerX() - 40, mMapRange.centerY(), paintPen);
        }
    }

    public void drawBmp(Bitmap contentBmp) {
        if (null == mTileBitmap) {
            return;
        }
        Canvas canvas = new Canvas(mTileBitmap);
        Rect rectSrc = new Rect((int) (mMapRange.left * mMaxScale), (int) (mMapRange.top * mMaxScale),
                (int) (mMapRange.right * mMaxScale), (int) (mMapRange.bottom * mMaxScale));
        Rect rectDst = new Rect(0, 0, mTileBitmap.getWidth(), mTileBitmap.getHeight());
        canvas.drawBitmap(contentBmp, rectSrc, rectDst, null);
    }
}