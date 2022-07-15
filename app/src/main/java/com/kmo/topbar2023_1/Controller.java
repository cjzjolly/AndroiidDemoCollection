package com.kmo.topbar2023_1;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.photoCutter.MeasurelUtils;
import com.example.piccut.R;

public class Controller {
    private View mTitleBar;
    private Context mContext;
    private View mLeftRightViewParent;
    private View mLeftTopView;
    private View mRightTopView;
    private View mCenterTopView;
    private View mSubMenu;
    private View mParent;
    /**背景**/
    private View mBgView;

    private boolean mLeftRightViewParentIsOnTop = false;
    /**上下偏移值**/
    private float mLeftRightViewParentTransVal = 0;

    public Controller(Context context, View parent) {
        this.mContext = context;
        this.mParent = parent;
        this.mLeftTopView = parent.findViewById(R.id.leftTopView);
        this.mRightTopView = parent.findViewById(R.id.rightTopView);
        /**leftRightViewParent 左右两部分主菜单的父控件**/
        this.mLeftRightViewParent = parent.findViewById(R.id.leftRightViewParent);
        this.mCenterTopView = parent.findViewById(R.id.centerTopView);
        this.mSubMenu = parent.findViewById(R.id.subMenu);
        this.mBgView = parent.findViewById(R.id.bg_view);
        this.mTitleBar = parent.findViewById(R.id.tv_title_bar);
        this.mLeftRightViewParentTransVal = MeasurelUtils.convertDpToPixel(100, mContext);
        mParent.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                Log.e("cjztest", "onLayoutChange");
                refresh();
            }
        });
    }

    /**todo 通过测量父控件现在的宽度与子控件之间宽度的关系，确定子控件的排布方式**/
    public void refresh() {
        if (mParent == null || mLeftTopView == null || mRightTopView == null || mCenterTopView == null || mSubMenu == null) {
            return;
        }
        // 如果中间菜单与左右两侧的菜单相距太近，把左右菜单的父控件位置上移，否则调整为保持水平  //todo 需要添加动画
        float limit = MeasurelUtils.convertDpToPixel(5, mContext);
        //如果主菜单左右两部分准备和中间部分发生交集，就把左右两部分推上和标题栏对齐
        if (mParent.getWidth() > 0 &&
                (mCenterTopView.getLeft() - mLeftTopView.getRight() < limit || mRightTopView.getLeft() - mCenterTopView.getRight() < limit)) {
            //移动到标题对齐
            if (!mLeftRightViewParentIsOnTop) {
                //设置左右菜单的高度为置顶
                mLeftRightViewParent.setPadding(mLeftRightViewParent.getPaddingLeft(), 0, mLeftRightViewParent.getPaddingRight(), mLeftRightViewParent.getPaddingBottom());
                //设置背景margin和高度为置顶
                mBgView.setPadding(mBgView.getPaddingLeft(), 0,
                        mBgView.getPaddingRight(), mBgView.getPaddingBottom());
                //标记已设置完成
                mLeftRightViewParentIsOnTop = true;
            } else {  //todo bug
                if (mParent.getWidth() > 0 &&
                        (mTitleBar.getLeft() - mLeftTopView.getRight() < limit || mRightTopView.getLeft() - mTitleBar.getRight() < limit)) { //todo 如果再继续收紧到和标题栏接近相交时，整个菜单栏在维持两个左右菜单在第一行的同时，下移到标题栏下方
                    //设置左右菜单的高度为与中间菜单对齐
                    mLeftRightViewParent.setPadding(mLeftRightViewParent.getPaddingLeft(), mCenterTopView.getTop(),
                            mLeftRightViewParent.getPaddingRight(), mLeftRightViewParent.getPaddingBottom());
                    //设置背景margin和高度为置顶
                    mBgView.setPadding(mBgView.getPaddingLeft(), mCenterTopView.getTop(),
                            mBgView.getPaddingRight(), mBgView.getPaddingBottom());
                    //让中间菜单下移
                    mCenterTopView.setPadding(mCenterTopView.getPaddingLeft(), mLeftRightViewParent.getBottom(),
                            mCenterTopView.getPaddingRight(), mCenterTopView.getPaddingBottom());
                } else {
                    mLeftRightViewParentIsOnTop = false;
                }
            }
        } else { //否则就重新下放
            if (mLeftRightViewParentIsOnTop) {
                //设置左右菜单的高度为与中间菜单对齐
                mLeftRightViewParent.setPadding(mLeftRightViewParent.getPaddingLeft(), mCenterTopView.getTop(),
                        mLeftRightViewParent.getPaddingRight(), mLeftRightViewParent.getPaddingBottom());
                //设置背景margin和高度为置顶
                mBgView.setPadding(mBgView.getPaddingLeft(), mCenterTopView.getTop(),
                        mBgView.getPaddingRight(), mBgView.getPaddingBottom());
                //标记已设置完成
                mLeftRightViewParentIsOnTop = false;
            }
        }
    }

}
