package com.kmo.topbar2023;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TopBarContainer extends FrameLayout {
    private int mWidth, mHeight;
    private View mTitleBar;

    public TopBarContainer(@NonNull Context context) {
        super(context);
    }

    public TopBarContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TopBarContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void addTitleBar(View titleBar) {
        if (mTitleBar != null) {
            removeView(mTitleBar);
        }
        mTitleBar = titleBar;
        addView(mTitleBar);
        invalidate();
    }

    public void addMainMenuLeft(View leftView) {

    }

    public void addMainMenuCenter(View centerView) {

    }

    public void addMainMenuRight(View rightView) {

    }

    public void addSubMenu(View rightView) {

    }

    private void init() {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        if (w != mWidth || h != mHeight) { //已经onMeasuer过一次，除非界面大小改动否则不重新初始化view
            mWidth = w;
            mHeight = h;
            init();
        }
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        //todo 绘制控件之前先摆好他们的位置
        if (mTitleBar != null) {
            mTitleBar.setX((right - left) / 2 - mTitleBar.getWidth() / 2);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
