package com.example.whiteboard;

import android.app.Activity;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DrawView drawView = new DrawView(this);
        drawView.setBitmapScale(Whiteboard.MAX_SCALE);
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.addView(new Whiteboard(this));
        frameLayout.addView(drawView);
        setContentView(frameLayout);
    }
}
