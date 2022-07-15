package com.kmo.topbar2023;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class MainActivity extends Activity {
    private TopBarContainer mTopBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTopBar = new TopBarContainer(this);

        TextView textView = new TextView(this);
        textView.setText("文档名称");
        textView.setGravity(Gravity.CENTER);
        textView.setLayoutParams(new ViewGroup.LayoutParams(300, 300));
        mTopBar.addTitleBar(textView);


        setContentView(mTopBar);
    }


}
