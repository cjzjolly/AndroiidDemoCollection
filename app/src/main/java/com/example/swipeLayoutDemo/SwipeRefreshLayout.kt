package com.example.swipeLayoutDemo

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.FrameLayout
import com.example.photoCutter.MeasurelUtils

class SwipeRefreshLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private var mAnimator: ValueAnimator? = null
    private val OND_DP = MeasurelUtils.convertDpToPixel(1f, context)
    private var mPrevY = 0f
    private var mMaxOffsetY = 100 * OND_DP

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        Log.e("cjztest", "MotionEvent:${event}")
        when (event?.action) {
            MotionEvent.ACTION_MOVE -> {
                val deltaY = event?.rawY - mPrevY
                if (y + deltaY > 0 && y + deltaY < mMaxOffsetY) {
                    y += deltaY
                } else {

                }
            }
            //todo 使用动画回弹
            MotionEvent.ACTION_UP -> {
                mAnimator?.cancel()
                mAnimator = ValueAnimator.ofFloat(y, 0f)
                mAnimator?.run {
                    addUpdateListener { animation ->
                        val currentY = animation?.animatedValue as Float
                        y = currentY
                        Log.e("cjztest", "CurrentY:${currentY}")
                    }
                    duration = 300
                    start()
                }
            }
        }
        mPrevY = event?.rawY!!
        return true
    }
}