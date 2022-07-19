package com.kmo.topbar2023_1;

import android.content.Context;
import android.graphics.Rect;
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

        Rect rectLeft = new Rect(mLeftTopView.getLeft(), mLeftTopView.getTop(), mLeftTopView.getRight(), mLeftTopView.getBottom());
        Rect rectRight = new Rect(mRightTopView.getLeft(), mRightTopView.getTop(), mRightTopView.getRight(), mRightTopView.getBottom());
        Rect rectLeftAndRight = new Rect(mLeftTopView.getLeft(), mLeftTopView.getTop(), mRightTopView.getRight(), mLeftTopView.getBottom());
        Rect rectTitleBar = new Rect(mTitleBar.getLeft(), mTitleBar.getTop(), mTitleBar.getRight(), mTitleBar.getBottom());
        Rect rectCenterBar = new Rect(mCenterTopView.getLeft(), mCenterTopView.getTop(), mCenterTopView.getRight(), mCenterTopView.getBottom());

        //分情况讨论，两两组合：
        //如果主菜单左右两部分准备和中间部分发生交集，就把左右两部分推上和标题栏对齐
        if (mParent.getWidth() > 0 &&
                (mCenterTopView.getLeft() - mLeftTopView.getRight() < limit || mRightTopView.getLeft() - mCenterTopView.getRight() < limit)) {
            Log.e("cjztest", "主菜单与左右菜单相交");
            if (rectTitleBar.left < rectLeft.right || rectTitleBar.right > rectRight.left) {
                Log.e("cjztest", "主菜单与左右菜单相交，且与标题栏相交");
                mLeftRightViewParent.setPadding(mLeftRightViewParent.getPaddingLeft(), mTitleBar.getBottom(), mLeftRightViewParent.getPaddingRight(), mLeftRightViewParent.getPaddingBottom());
                mBgView.setPadding(mBgView.getPaddingLeft(), mTitleBar.getBottom(),
                        mBgView.getPaddingRight(), mBgView.getPaddingBottom());
            } else {
                Log.e("cjztest", "主菜单与左右菜单相交，但不与标题栏相交");
                mLeftRightViewParent.setPadding(mLeftRightViewParent.getPaddingLeft(), mTitleBar.getTop(), mLeftRightViewParent.getPaddingRight(), mLeftRightViewParent.getPaddingBottom());
                mBgView.setPadding(mBgView.getPaddingLeft(), mTitleBar.getTop(),
                        mBgView.getPaddingRight(), mBgView.getPaddingBottom());
            }
            mCenterTopView.setPadding(mCenterTopView.getPaddingLeft(), mLeftRightViewParent.getBottom(),
                    mCenterTopView.getPaddingRight(), mCenterTopView.getPaddingBottom());
        } else {
            Log.e("cjztest", "主菜单不与左右菜单相交");
            if (rectTitleBar.left < rectLeft.right || rectTitleBar.right > rectRight.left) {
                Log.e("cjztest", "主菜单不与左右菜单相交，但与标题栏相交");
                mCenterTopView.setPadding(mCenterTopView.getPaddingLeft(), mTitleBar.getBottom(),
                        mCenterTopView.getPaddingRight(), mCenterTopView.getPaddingBottom());
            } else {
                Log.e("cjztest", "主菜单不与左右菜单相交，也不与标题栏相交");
                mCenterTopView.setPadding(mCenterTopView.getPaddingLeft(), mTitleBar.getTop(),
                        mCenterTopView.getPaddingRight(), mCenterTopView.getPaddingBottom());
            }
            //设置左右菜单的高度为与中间菜单对齐
            mLeftRightViewParent.setPadding(mLeftRightViewParent.getPaddingLeft(), mCenterTopView.getTop(),
                    mLeftRightViewParent.getPaddingRight(), mLeftRightViewParent.getPaddingBottom());
            //设置背景margin和高度为置顶
            mBgView.setPadding(mBgView.getPaddingLeft(), mCenterTopView.getTop(),
                    mBgView.getPaddingRight(), mBgView.getPaddingBottom());
        }
        //二级菜单随着主菜单的上下位移移动
        mSubMenu.setPadding(mSubMenu.getPaddingLeft(), mCenterTopView.getBottom(),
                mSubMenu.getPaddingRight(), mSubMenu.getPaddingBottom());
//        //左右两侧菜单与标题栏位置相交
//        if (rectLeft.intersects(rectTitleBar.left, rectTitleBar.top, rectTitleBar.right, rectTitleBar.bottom) ||
//                rectRight.intersects(rectTitleBar.left, rectTitleBar.top, rectTitleBar.right, rectTitleBar.bottom)) {
//
//        }


//        //左右两侧菜单的外接矩形：

//        //与中间菜单相交吗？
//        if (rectLeft.intersects(rectCenterBar.left, rectCenterBar.top, rectCenterBar.right, rectCenterBar.bottom) ||
//                rectRight.intersects(rectCenterBar.left, rectCenterBar.top, rectCenterBar.right, rectCenterBar.bottom)) {
//                //设置左右菜单的高度为置顶
//                mLeftRightViewParent.setPadding(mLeftRightViewParent.getPaddingLeft(), 0, mLeftRightViewParent.getPaddingRight(), mLeftRightViewParent.getPaddingBottom());
//                //设置背景margin和高度为置顶
//                mBgView.setPadding(mBgView.getPaddingLeft(), 0,
//                        mBgView.getPaddingRight(), mBgView.getPaddingBottom());
//        } else {
//            //设置左右菜单的高度为与中间菜单对齐
//            mLeftRightViewParent.setPadding(mLeftRightViewParent.getPaddingLeft(), mCenterTopView.getTop(),
//                    mLeftRightViewParent.getPaddingRight(), mLeftRightViewParent.getPaddingBottom());
//            //设置背景margin和高度为置顶
//            mBgView.setPadding(mBgView.getPaddingLeft(), mCenterTopView.getTop(),
//                    mBgView.getPaddingRight(), mBgView.getPaddingBottom());
//        }
//    }
    }
}
