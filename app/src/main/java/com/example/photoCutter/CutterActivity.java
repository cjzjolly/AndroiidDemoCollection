package com.example.photoCutter;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

public class CutterActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PhotoCutter photoCutter = new PhotoCutter(this);
        setContentView(photoCutter);
    }
}
