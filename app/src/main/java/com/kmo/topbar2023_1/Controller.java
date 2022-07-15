package com.kmo.topbar2023_1;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.example.photoCutter.MeasurelUtils;
import com.example.piccut.R;

public class Controller {
    private Context mContext;
    private View mLeftRightViewParent;
    private View mLeftTopView;
    private View mRightTopView;
    private View mCenterTopView;
    private View mSubMenu;
    private View mParent;

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
        //todo 如果中间菜单与左右两侧的菜单相距太近，把左右菜单的父控件位置上移，否则调整为保持水平
        float limit = MeasurelUtils.convertDpToPixel(5, mContext);
        //如果主菜单左右两部分准备和中间部分发生交集，就把左右两部分推上和标题栏对齐
        if (mParent.getWidth() > 0 &&
                (mCenterTopView.getLeft() - mLeftTopView.getRight() < limit || mRightTopView.getLeft() - mCenterTopView.getRight() < limit)) {
            //移动到标题对齐
            if (!mLeftRightViewParentIsOnTop) {
                mLeftRightViewParent.setPadding(0, 0, 0, 0);
                mLeftRightViewParentIsOnTop = true;
            }
        } else { //否则就重新下方
            if (mLeftRightViewParentIsOnTop) {
                mLeftRightViewParent.setPadding(0, mCenterTopView.getTop(), 0, 0);
                mLeftRightViewParentIsOnTop = false;
            }
        }
    }

}
