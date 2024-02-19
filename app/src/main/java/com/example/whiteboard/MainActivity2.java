package com.example.whiteboard;

import android.Manifest;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class MainActivity2 extends Activity {
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }


    private void init() {
        Bitmap bitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        ImageView iv = new ImageView(this);
        Path basePath = new Path();

//        basePath.addRoundRect(50, 50, 500, 500, 20, 20, Path.Direction.CCW);
        basePath.addOval(30, 30, 500, 500, Path.Direction.CCW);
//        basePath.moveTo(50, 50);
//        basePath.lineTo(500, 500);
        PathMeasure pathMeasure = new PathMeasure(basePath, false);
        float lineWidth = 52f;
        int step = 1;
        Path totalPath = new Path();
        Path outsiderPath = new Path();
        Path innerPath = new Path();
        List<float[]> outsiderList = new LinkedList<>();
        for (float i = 0; i < pathMeasure.getLength(); i += step) {
            float[] pos = new float[2];
            float[] tan = new float[2];
            pathMeasure.getPosTan(i, pos, tan);
            //计算方位角
            float degrees = (float) (Math.atan2(tan[1], tan[0]) * 180.0 / Math.PI);
            float width = (float) (lineWidth * Math.random()); //todo 根据书写压力更改width
            float newVec[] = new float[4];
            float rotatedVec[] = new float[] {-width / 2f, 0, width / 2f, 0};

            Matrix matrix = new Matrix();
            matrix.setRotate(degrees + 90);
            matrix.mapPoints(rotatedVec);
            //偏移到对应位置
            newVec[0] = rotatedVec[0] + pos[0];
            newVec[1] = rotatedVec[1] + pos[1];
            newVec[2] = rotatedVec[2] + pos[0];
            newVec[3] = rotatedVec[3] + pos[1];
            outsiderList.add(newVec);
        }
        //勾勒外边框
        for (int i = 0; i < outsiderList.size(); i++) {
            float[] pos = outsiderList.get(i);
            if (i == 0) {
                //使得outsidePath开头和innerPath开头连起来
                outsiderPath.moveTo(pos[2], pos[3]);
                outsiderPath.lineTo(pos[0], pos[1]);
            } else if (i % 2 == 0) {
                outsiderPath.lineTo(pos[0], pos[1]);
            }
        }
        //勾勒内边框
        for (int i = 0; i < outsiderList.size(); i++) {
            float[] pos = outsiderList.get(i);
            if (i == 0) {
                innerPath.moveTo(pos[2], pos[3]);
            } else if (i % 2 != 0) {
                innerPath.lineTo(pos[2], pos[3]);
            }
        }
        //使得outsidePath结尾和innerPath结尾连起来
        float lastPoint[] = outsiderList.get(outsiderList.size() - 1);
        innerPath.lineTo(lastPoint[0], lastPoint[1]);

        totalPath.addPath(outsiderPath);
        totalPath.addPath(innerPath);
        totalPath.setFillType(Path.FillType.EVEN_ODD);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
//        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1f);
        paint.setColor(Color.RED);

        canvas.drawColor(Color.GRAY);
        canvas.drawPath(totalPath, paint);

        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(basePath, paint);


        iv.setImageBitmap(bitmap);
        setContentView(iv);

        mHandler.postDelayed(()-> init(), 30);
    }
}
