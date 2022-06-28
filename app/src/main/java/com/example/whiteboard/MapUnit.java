package com.example.whiteboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import java.util.Random;

/**
 * Created by cjz on 2019/4/30.
 */

public class MapUnit extends View {

    public MapUnit(Context context) {
        super(context);
    }

    public MapUnit(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MapUnit(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        Random random = new Random();
        Log.i("onDraw", hashCode() + "");
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setStrokeWidth(8f);
        paint.setColor((0xFF000000 | (random.nextInt(255) & 0xFF) << 16 | (random.nextInt(255) & 0xFF) << 8 | (random.nextInt(255) & 0xFF)));
        paint.setStyle(Paint.Style.FILL);
        //绘制随机色背景
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        Paint paintPen = new Paint();
        paintPen.setStrokeWidth(8f);
        paintPen.setStyle(Paint.Style.FILL);
        paintPen.setColor(Color.BLACK);
        paintPen.setTextSize(120f);
        paintPen.setAntiAlias(true);
        //绘制自己是第几列第几行的单元
        if(getTag() != null) {
            int position[] = (int[]) getTag();
            canvas.drawText(String.format("UnitX: %d, UnitY: %d", position[0], position[1]),  getWidth() / 2 - 100, getHeight() / 2, paintPen);
        }
    }
}