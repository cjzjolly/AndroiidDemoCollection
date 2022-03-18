package com.example.dctDemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

public class BaseView extends View {
    private double mInputSignal[][] = new double[8][8];
    private int mU;
    private int mV;

    public BaseView(Context context) {
        super(context);
    }

    public BaseView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**传入实际信号**/
    public void setInputSignal(int u, int v, double signal) {
        mInputSignal[u][v] = signal;
    }


    @Override
    protected void onDraw(Canvas canvas) {
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
                int val = (int) (mInputSignal[col][line] * 255f);
                paint.setColor(0xFF000000 | val << 8 * 2 | val << 8 | val);
                canvas.drawRect(rectF, paint);
                col++;
            }
            line ++;
        }
    }
}
