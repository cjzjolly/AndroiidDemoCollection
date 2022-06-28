package com.example.whiteboard;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import java.util.Random;

/**
 * Created by cjz on 2019/4/30.
 */

public class MapUnit {
    private Paint mPaint;
    private RectF mMapRange;
    private int mUnitXY[] = new int[2];
    private float mScale = 1f;

    public MapUnit(RectF range) {
        mMapRange = new RectF(range);
        Random random = new Random();
        mPaint = new Paint();
        mPaint.setStrokeWidth(8f);
        mPaint.setColor((0xFF000000 | (random.nextInt(255) & 0xFF) << 16 | (random.nextInt(255) & 0xFF) << 8 | (random.nextInt(255) & 0xFF)));
        mPaint.setStyle(Paint.Style.FILL);
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

    protected void onDraw(Canvas canvas) {
        if (null == mMapRange) {
            return;
        }
        Log.i("onDraw", hashCode() + "");
        //绘制随机色背景
        canvas.drawRect(mMapRange, mPaint);
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
}