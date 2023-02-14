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
import com.example.demosAboutCanvas.ChangeColorAfterDraw;
import com.example.effectByScrollInOut.EffectDemoActivity;
import com.example.eldestBmpOutCache.TestActivity;
import com.example.pdfReader.PdfReaderDemo;
import com.example.photoCutter.CutterActivity;
import com.example.photoSelector.PhotoSelectorPageActivity;
import com.example.photoSelectorByRecycler.PicSelectorByRV;
import com.example.picCutview.PicCutActivity;
import com.example.sidingMenuRecyclerView.SlideRecyclerViewActivity;
import com.example.singleChoiceRecyclerView.SingleOrMultiChoiceRecyclerActivity;

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
        //照片拖动Demo2：
        Button photoSelectorBtm2 = new Button(this);
        photoSelectorBtm2.setText("照片拖动Demo2");
        photoSelectorBtm2.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, PicSelectorByRV.class));
        });
        linearLayoutContainer.addView(photoSelectorBtm2);
        //图片不规则裁剪demo：
        Button photoCutterBtn = new Button(this);
        photoCutterBtn.setText("图片不规则裁剪demo");
        photoCutterBtn.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CutterActivity.class));
        });
        linearLayoutContainer.addView(photoCutterBtn);
        //侧滑菜单recyclerView：
        Button sidingMenuBtn = new Button(this);
        sidingMenuBtn.setText("侧滑菜单recyclerView");
        sidingMenuBtn.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SlideRecyclerViewActivity.class));
        });
        linearLayoutContainer.addView(sidingMenuBtn);
        //可以放大缩放的白板：
        Button whiteboardBtn = new Button(this);
        whiteboardBtn.setText("可以放大缩放的白板");
        whiteboardBtn.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, com.example.whiteboard.MainActivity.class));
        });
        linearLayoutContainer.addView(whiteboardBtn);
        //最旧条目去除算法测试：
        Button eldestOutTest = new Button(this);
        eldestOutTest.setText("最旧条目去除算法测试");
        eldestOutTest.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, TestActivity.class));
        });
        linearLayoutContainer.addView(eldestOutTest);
        //在Canvas绘制后更换颜色测试：
        Button btnChangeColorAfterDrawTest = new Button(this);
        btnChangeColorAfterDrawTest.setText("在Canvas绘制后更换颜色测试");
        btnChangeColorAfterDrawTest.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ChangeColorAfterDraw.class));
        });
        //单选recyclerView：
        Button btnSingleChoiceRV = new Button(this);
        btnSingleChoiceRV.setText("单选recycler view");
        btnSingleChoiceRV.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SingleOrMultiChoiceRecyclerActivity.class));
        });
        linearLayoutContainer.addView(btnSingleChoiceRV);
    }
}
