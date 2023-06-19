package com.example.photoCutter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.example.piccut.R;

public class CutterActivity extends Activity {

    private int mRotate = 90;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cutter_example);
        PhotoCutter photoCutter = findViewById(R.id.pc_cutter);
        photoCutter.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.test_pic_2));
        findViewById(R.id.btn_photo_cutter_rotate).setOnClickListener(v -> {
            try {
                photoCutter.rotate(mRotate);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mRotate += 90;
        });
        findViewById(R.id.btn_photo_cutter_cut).setOnClickListener(v -> {
            Bitmap bitmap = photoCutter.cutPhoto();
            ImageView imageView = findViewById(R.id.iv_cutter_result);
            imageView.setImageBitmap(bitmap);
        });
    }
}
