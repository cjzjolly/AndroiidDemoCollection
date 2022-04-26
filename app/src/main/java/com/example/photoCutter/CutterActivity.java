package com.example.photoCutter;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.example.piccut.R;

public class CutterActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PhotoCutter photoCutter = new PhotoCutter(this);
        photoCutter.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.tree));
        setContentView(photoCutter);
    }
}
