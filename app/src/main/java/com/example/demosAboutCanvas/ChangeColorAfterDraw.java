package com.example.demosAboutCanvas;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**演示已绘制到Canvas的内容如何进行后期变色**/
public class ChangeColorAfterDraw extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new TestView(this));
    }

    private class TestView extends View {

        public TestView(Context context) {
            super(context);
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        public TestView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        public TestView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            //创建SRC图层，实际上是红色的字来的:
            Bitmap srcBmp = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas cvsFromText = new Canvas(srcBmp);
            Paint textPaint = new Paint();
            textPaint.setAntiAlias(true);
            textPaint.setColor(Color.RED);
            textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            textPaint.setTextSize(100);
            cvsFromText.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            cvsFromText.drawText("Test", 0, 300, textPaint);
            //创建DST图层:
            Bitmap dstBmp = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas cvsFromText2 = new Canvas(dstBmp);
            cvsFromText2.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            cvsFromText2.drawColor(Color.GREEN);
            //先绘制换色用的DST图层（违背直觉，但这规则就是如此）
            canvas.drawBitmap(dstBmp, 0, 0, null);
            //再绘制实际内容SRC图层
            Paint blendPaint = new Paint();
            blendPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN)); //使用混合规则DST_IN， [Sa * Da, Sa * Dc]，这样SRC图层不透明的地方就可以按透明度系数换成DST图层的颜色了。
            canvas.drawBitmap(srcBmp, 0, 0, blendPaint);
        }
    }
}
