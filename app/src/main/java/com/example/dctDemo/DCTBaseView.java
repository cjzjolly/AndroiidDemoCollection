package com.example.dctDemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

public class DCTBaseView extends View {
    private double mDCTBaseMatrix[][] = new double[8][8];
    private double mInputSignal[][] = new double[8][8];
    private int mU;
    private int mV;

    public DCTBaseView(Context context) {
        super(context);
        init();
    }

    public DCTBaseView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DCTBaseView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        for (int y = 0; y < 8; y ++) {
            for (int x = 0; x < 8; x ++) {
                mInputSignal[x][y] = 1;
//                mInputSignal[x][y] = 0;
            }
        }
    }

    public void setInputSignalVal(int x, int y, double val) {
        mInputSignal[x][y] = val;
    }

    public double getSignalDCTBaseVal(int x, int y) {
        return mDCTBaseMatrix[x][y];
    }

    public void calcDCTBase(int u, int v) {
        this.mU = u;
        this.mV = v;
        double c_u = 1;
        double c_v = 1;
        if (u == 0 && v == 0) {
            c_u = c_v = 1f / Math.sqrt(2);
        }
        for (int y = 0; y < 8; y ++) {
            for (int x = 0; x < 8; x ++) {
//                double base = c_u * c_v * mInputSignal[x][y] * Math.cos(Math.toDegrees((2 * x + 1) * u * Math.PI / 16f)) * Math.cos(Math.toDegrees((2 * y + 1) * v * Math.PI / 16f));
//                double base = c_u * c_v * mInputSignal[x][y] * Math.cos(Math.toRadians((2 * x + 1) * u * Math.PI / 16f)) * Math.cos(Math.toRadianss((2 * y + 1) * v * Math.PI / 16f));
                double base = c_u * c_v * mInputSignal[x][y] * Math.cos(((2 * x + 1) * u * Math.PI / 16f)) * Math.cos(((2 * y + 1) * v * Math.PI / 16f));
                //todo 明天把f(x, y)作为系数的部分也实现
                mDCTBaseMatrix[x][y] = base;
                Log.i("cjztest", String.format("mDCTBaseMatrix[%d][%d] == %f", x, y, base));
            }
        }
        invalidate();
    }



    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        int w = canvas.getWidth();
        int h = canvas.getHeight();
        //JPEG的DCT为8*8的基
        float wStep = w / 8f;
        float hStep = h / 8f;
        //上色Paint
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        int line = 0;
        int col = 0;
        for (float y = 0; y < h; y += hStep) {
            col = 0;
            for (float x = 0; x < w; x += wStep) {
                RectF rectF = new RectF(x, y, x + wStep, y + hStep);
//                int val = (int) (mDCTBaseMatrix[col][line]);
                int val = (int) (((mDCTBaseMatrix[col][line] + 1f) / 2f) * 255f);
//                paint.setColor(0xFF000000 | (val < 0 ? 0 : val) << 8 * 2);
                paint.setColor(0xFF000000 | val << 8 * 2 | val << 8 | val);
                canvas.drawRect(rectF, paint);
                col++;
            }
            line ++;
        }
        //debug
//        paint.setStrokeWidth(3);
//        paint.setTextSize(20);
//        paint.setColor(Color.GREEN);
//        paint.setStyle(Paint.Style.STROKE);
//        canvas.drawText(String.format("%d, %d", mU, mV), 10, 20, paint);
    }
}
