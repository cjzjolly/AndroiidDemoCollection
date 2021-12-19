package com.example.picCutview;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.piccut.R;

public class PicCutActivity extends Activity {

    private PicCutView mPicCutView;
    private Button mBtnCut;
    private ImageView mIvCut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cut_cover);
        mPicCutView = findViewById(R.id.pic_cut_view);
        mBtnCut = findViewById(R.id.btn_cut);
        mIvCut = findViewById(R.id.iv_cut);

        mBtnCut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIvCut.setImageBitmap(mPicCutView.cutPic());
            }
        });

        mPicCutView.setPic(BitmapFactory.decodeResource(getResources(), R.drawable.bg));
    }
}