package com.example.effectByScrollInOut;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

public class TopPullPushEffectView extends FrameLayout {
    private int mContentW;
    private int mContentH;
    private int mWidth;
    private int mHeight;
    private View mVideoView;
    private View mTopView;
    private View mBottomView;

    public TopPullPushEffectView(Context context) {
        super(context);
    }

    public TopPullPushEffectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TopPullPushEffectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        if (w != mWidth || h != mHeight) { //已经onMeasuer过一次，除非界面大小改动否则不重新初始化view
            mWidth = MeasureSpec.getSize(widthMeasureSpec);
            mHeight = MeasureSpec.getSize(heightMeasureSpec);
        }
        mBottomView.setY(mHeight - mBottomView.getLayoutParams().height);
    }

    public void setVideoView(View v, int contentW, int contentH) {
        if (mVideoView != null) {
            removeView(mVideoView);
        }
        mVideoView = null;
        addView(v);
        mVideoView = v;
        this.mContentW = contentW;
        this.mContentH = contentH;
    }

    public void setTopView(View view) {
        mTopView = view;
        addView(view);
    }


    public void setViewUnderTheTop(View view) {
        mBottomView = view;
        addView(view);
    }


//    public void scaleChild(float scale) {
//        if (scale > 1) {
//            scale = 1;
//        }
//        //计算容器可见区域
//        //当前贴顶控件可见高度
//        float visibleH = mHeight * scale - mTopView.getHeight() - mBottomView.getHeight();
//        View child = getChildAt(0);
//        float widthPercentage = (float) mContentW / (float) mWidth;
//        float heightPercentage = (float) mContentH / (float) visibleH;
//        if (widthPercentage > heightPercentage) { //如果宽占比更多，宽拉伸到尽，高按照视口比例重新调整为统一密度的单位，然后再根据图片高对宽的比例调整物体的高的边的缩放
//            //宽已拉满，使content居中： mHeight - visibleH为可见区域在本控件的起始y值，mContentH * ((float) mWidth / (float) mContentW)为素材缩放到容器可以刚好放入时的实际高度，
//            //(mHeight - mContentH * ((float) mWidth / (float) mContentW)) / 2 上下黑边的每一边的长度
//            child.setY(mHeight - visibleH - (mHeight - mContentH * ((float) mWidth / (float) mContentW)) / 2 + (visibleH - mContentH * ((float) mWidth / (float) mContentW)) / 2);
//        } else {
//            //缩放量：
////            float height;
////            if (child.getY() <= mTopView.getY()) {
////               height = (visibleH - mBottomView.getHeight() - mTopView.getHeight());
////            } else {
////                height = visibleH;
////            }
////            float scaleChild = height / (mContentH * ((float) mWidth / (float) mContentW));
////            child.setScaleX(scaleChild);
////            child.setScaleY(scaleChild);
////            child.setY(mHeight - visibleH - (mHeight - mContentH * ((float) mWidth / (float) mContentW) * scaleChild) / 2); //推到不可见区域，再回退走上下黑边
////            child.setY(child.getY() + mTopView.getHeight());
//
//
//            float scaleChild = visibleH / (mContentH * ((float) mWidth / (float) mContentW));
//            child.setScaleX(scaleChild);
//            child.setScaleY(scaleChild);
//            child.setY(mHeight - visibleH - (mHeight - mContentH * ((float) mWidth / (float) mContentW) * scaleChild) / 2); //推到不可见区域，再回退走上下黑边
//            child.setY(child.getY() - mTopView.getHeight() - mBottomView.getHeight());
//        }
//        mTopView.setY(mHeight - mHeight * scale);
//    }

    public void scaleChild(float scale) {
        if (scale > 1) {
            scale = 1;
        }
        //计算容器可见区域
        //当前贴顶控件可见高度
        float visibleH = mHeight * scale;
        View child = getChildAt(0);
        float widthPercentage = (float) mContentW / (float) mWidth;
        float heightPercentage = (float) mContentH / (float) visibleH;
        if (widthPercentage > heightPercentage) { //如果宽占比更多，宽拉伸到尽，高按照视口比例重新调整为统一密度的单位，然后再根据图片高对宽的比例调整物体的高的边的缩放
            //宽已拉满，使content居中： mHeight - visibleH为可见区域在本控件的起始y值，mContentH * ((float) mWidth / (float) mContentW)为素材缩放到容器可以刚好放入时的实际高度，
            //(mHeight - mContentH * ((float) mWidth / (float) mContentW)) / 2 上下黑边的每一边的长度
            child.setY(mHeight - visibleH - (mHeight - mContentH * ((float) mWidth / (float) mContentW)) / 2 + (visibleH - mContentH * ((float) mWidth / (float) mContentW)) / 2);
        } else {
            //缩放量：
            float scaleChild = visibleH / (mContentH * ((float) mWidth / (float) mContentW));
            child.setScaleX(scaleChild);
            child.setScaleY(scaleChild);
            child.setY(mHeight - visibleH - (mHeight - mContentH * ((float) mWidth / (float) mContentW) * scaleChild) / 2); //推到不可见区域，再回退走上下黑边
        }
        mTopView.setY(mHeight - mHeight * scale);
    }


}
