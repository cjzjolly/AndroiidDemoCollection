package com.cjz.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.Nullable;

import com.example.cameraXDemo.CameraXDemoActivity_1;
import com.example.dctDemo.DCTTestDemo;
import com.example.effectByScrollInOut.EffectDemoActivity;
import com.example.pdfReader.PdfReaderDemo;
import com.example.photoSelector.PhotoSelectorPageActivity;
import com.example.picCutview.PicCutActivity;

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
        //图片裁剪控件：
        Button btnDemoPicCut = new Button(this);
        btnDemoPicCut.setText("图片裁剪控件");
        btnDemoPicCut.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, PicCutActivity.class));
        });
        linearLayoutContainer.addView(btnDemoPicCut);
        //DCT算法展示Demo：
        Button btnDemoDCTCut = new Button(this);
        btnDemoDCTCut.setText("DCT算法Demo");
        btnDemoDCTCut.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, DCTTestDemo.class));
        });
        linearLayoutContainer.addView(btnDemoDCTCut);
        //pdf阅读展示Demo：
        Button pdfDemoBtn = new Button(this);
        pdfDemoBtn.setText("pdf阅读展示Demo");
        pdfDemoBtn.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, PdfReaderDemo.class));
        });
        linearLayoutContainer.addView(pdfDemoBtn);
        //CameraX API Demo：
        Button cameraXDemoBtn = new Button(this);
        cameraXDemoBtn.setText("CameraX API Demo");
        cameraXDemoBtn.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CameraXDemoActivity_1.class));
        });
        linearLayoutContainer.addView(cameraXDemoBtn);
        //照片拖动Demo：
        Button photoSelectorBtm = new Button(this);
        photoSelectorBtm.setText("照片拖动Demo");
        photoSelectorBtm.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, PhotoSelectorPageActivity.class));
        });
        linearLayoutContainer.addView(photoSelectorBtm);
    }
}
