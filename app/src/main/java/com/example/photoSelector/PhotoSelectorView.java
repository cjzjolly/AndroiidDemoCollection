package com.example.photoSelector;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.example.piccut.R;

/**使用FrameLayout方便日后实现拖动更换顺序的效果
 * 1、需要有动态复用原本控件的特性 **/
public class PhotoSelectorView extends FrameLayout {
    private int mWidth, mHeight;
    /**控制每行多少列**/
    private int mRow = 3;
    private int mColumn;
    /**控制每列多高**/
    private int mItemHeight = (int) convertDpToPixel(300, getContext());

    /**控件表**/
    private View mViewTable[][];

    public PhotoSelectorView(Context context) {
        super(context);
    }

    public PhotoSelectorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PhotoSelectorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    private float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    /**构建刚刚充满父控件面积的条目
     * 每个条目对应一条独立的信息**/
    private void initListItem() {
        int columnCount = (int) Math.ceil(mHeight / (float) mItemHeight) + 1;
        int rowCount = mRow;
        mColumn = columnCount;
        mViewTable = new View[columnCount][rowCount];
        for (int x = 0; x < rowCount; x++) {
            for (int y = 0; y < columnCount; y++) {
                ImageView imageView = new ImageView(getContext());
                imageView.setImageResource(R.drawable.tree);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setLayoutParams(new FrameLayout.LayoutParams(mWidth / rowCount, mItemHeight));
                imageView.setX(mWidth / rowCount * x);
                imageView.setY(mItemHeight * y);
                mViewTable[y][x] = imageView;
                addView(imageView);
            }
        }
    }


    /**触摸处理
     * 1、长按某个条目稍微放大，可以拖动
     * 2、拖动时可以上下翻动
     *  2.1、条目的y值小于0时整行下翻到最后一行的后面
     * 3、可以使用Geustor改善**/
    private GestureDetector mGestureDetector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        /**如果数据不需要翻页，则不执行下列逻辑, 数据滚动完也不执行**/
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            for (int y = 0; y < mColumn; y++) {
                for (int x = 0; x < mRow; x++) {
                    View view = mViewTable[y][x];
                    view.setY(view.getY() - distanceY);
                }
            }
            //如果有行已经飞上去，则移动到最底层
            if (mViewTable[0][0].getY() + mViewTable[0][0].getHeight() < 0) {
                for (int x = 0; x < mRow; x++) {
                    mViewTable[0][x].setY(mViewTable[mColumn - 1][x].getY() + mViewTable[mColumn - 1][x].getHeight());
                }
                //矩阵向上移一行
                View matrix[][] = new View[mColumn][mRow];
                matrix[mColumn - 1] = mViewTable[0];
                for (int y = mColumn - 1; y > 0 ; y--) {
                    matrix[y - 1] = mViewTable[y];
                }
                mViewTable = matrix;
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    });


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        invalidate();
        return true;
    }

    /**界面处理**/
    private void resetView() {

    }

    /**显示原理:
     * 1、按控件横向大小，按列数平均分布每个item的宽度，每个item可以为一个有padding的ImageView为基础的复合控件
     * 2、高度按照宽度*16/9或者人为设定改一下
     * **/
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        if (w != mWidth || h != mHeight) { //已经onMeasuer过一次，除非界面大小改动否则不重新初始化view
            mWidth = w;
            mHeight = h;
            initListItem();
            resetView();
        }
        invalidate();
    }


}
