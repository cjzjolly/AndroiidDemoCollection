package com.kmo.topbar2023_1;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.piccut.R;
import com.kmo.topbar2023.TopBarContainer;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.topbar_2023_1);
        View parent = findViewById(R.id.pdf_pad_topbar);
        FrameLayout padToolBar = findViewById(R.id.pdf_pad_topbar);
        Controller controller = new Controller(this, parent);
        findViewById(R.id.btn_test).setOnClickListener(v -> {
            ViewGroup.LayoutParams lp = padToolBar.getLayoutParams();
            lp.width = padToolBar.getWidth() - 50;
            padToolBar.setLayoutParams(lp);
        });
        findViewById(R.id.btn_test2).setOnClickListener(v -> {
            ViewGroup.LayoutParams lp = padToolBar.getLayoutParams();
            lp.width = padToolBar.getWidth() + 50;
            padToolBar.setLayoutParams(lp);
        });
    }


}
