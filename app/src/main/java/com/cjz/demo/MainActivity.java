package com.cjz.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.Nullable;

import com.example.effectByScrollInOut.EffectDemoActivity;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScrollView scrollView = new ScrollView(this);
        LinearLayout linearLayoutContainer = new LinearLayout(this);
        linearLayoutContainer.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(linearLayoutContainer);
        setContentView(scrollView);
        //仿B站竖屏视频拉伸：
        Button btnDemoBigHeightVideoScale = new Button(this);
        btnDemoBigHeightVideoScale.setText("仿B站竖屏视频拉伸");
        btnDemoBigHeightVideoScale.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, EffectDemoActivity.class));
        });
        linearLayoutContainer.addView(btnDemoBigHeightVideoScale);
    }
}
