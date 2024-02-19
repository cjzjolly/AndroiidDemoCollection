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
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void init() throws Exception {
        Bitmap bitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        ImageView iv = new ImageView(this);
        Path basePath = new Path();
//        basePath.moveTo(50, 50);
//        basePath.addRoundRect(50, 50, 500, 500, 20, 20, Path.Direction.CCW);
        basePath.addOval(30, 30, 300, 300, Path.Direction.CCW);
        PathMeasure pathMeasure = new PathMeasure(basePath, false);
        float lineWidth = 52f;
        Path outsiderPath = new Path();
        List<float[]> outsiderList = new LinkedList<>();
        for (float i = 0; i < pathMeasure.getLength(); i++) {
            float[] pos = new float[2];
            float[] tan = new float[2];
            pathMeasure.getPosTan(i, pos, tan);
            //计算方位角
            float degrees = (float) (Math.atan2(tan[1], tan[0]) * 180.0 / Math.PI);
            Log.i("cjztest", "degree:" + degrees);
            float width = lineWidth * 1f; //todo
            float newVec[] = new float[4];
            float rotatedVec0[] = new float[] {-width / 2f, 0};
            float rotatedVec1[] = new float[] {width / 2f, 0};

            Matrix matrix = new Matrix();
            matrix.setRotate(degrees + 90);
            matrix.mapPoints(rotatedVec0);
            matrix.mapPoints(rotatedVec1);
            //偏移到对应位置
            newVec[0] = rotatedVec0[0] + pos[0];
            newVec[1] = rotatedVec0[1] + pos[1];
            newVec[2] = rotatedVec1[0] + pos[0];
            newVec[3] = rotatedVec1[1] + pos[1];
            outsiderList.add(newVec);
        }
        for (int i = 0; i < outsiderList.size(); i++) {
            float[] pos = outsiderList.get(i);
            if (i == 0) {
                outsiderPath.moveTo(pos[0], pos[1]);
            } else if (i % 2 == 0) {
                outsiderPath.lineTo(pos[0], pos[1]);
            }
        }
        for (int i = 0; i < outsiderList.size(); i++) {
            float[] pos = outsiderList.get(i);
            if (i % 2 != 0) {
                outsiderPath.lineTo(pos[2], pos[3]);
            }
        }
        outsiderPath.setFillType(Path.FillType.EVEN_ODD);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
//        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1f);
        paint.setColor(Color.RED);

        canvas.drawColor(Color.GRAY);

        canvas.drawPath(outsiderPath, paint);

        iv.setImageBitmap(bitmap);
        setContentView(iv);
    }
}
