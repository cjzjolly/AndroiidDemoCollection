package com.example.effectByScrollInOut;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.piccut.R;

public class EffectDemoActivity extends Activity {
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scrollView = new ScrollView(this);
        //part1
        LinearLayout linearLayoutContainer = new LinearLayout(this);
        linearLayoutContainer.setOrientation(LinearLayout.VERTICAL);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2300);
        TopPullPushEffectView topScaleView = new TopPullPushEffectView(this);
        topScaleView.setLayoutParams(lp);
        ImageView ivForTestScale = new ImageView(this);
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.tree);
        ivForTestScale.setImageBitmap(b);
        ivForTestScale.setBackgroundColor(Color.GREEN);
        ivForTestScale.setLayoutParams(lp);
        topScaleView.setVideoView(ivForTestScale, b.getWidth(), b.getHeight());
        View vTest2 = new View(this);
        vTest2.setLayoutParams(lp);
        vTest2.setBackgroundColor(Color.GRAY);
        linearLayoutContainer.addView(topScaleView);
        linearLayoutContainer.addView(vTest2);
        scrollView.addView(linearLayoutContainer);
        //part2:
        TextView topView = new TextView(this);
        topView.setText("TopView");
        topView.setBackgroundColor(Color.RED);
        topView.setAlpha(0.6f);
        topView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100));
        TextView bottomView = new TextView(this);
        bottomView.setText("PlayerController");
        bottomView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200));
        bottomView.setBackgroundColor(Color.BLUE);
        bottomView.setAlpha(0.6f);
        topScaleView.setTopView(topView);
        topScaleView.setViewUnderTheTop(bottomView);

        setContentView(scrollView);

        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                float scale = (2300 - scrollY) / (float) 2300f;
                topScaleView.scaleChild(scale);
            }
        });

    }
}
