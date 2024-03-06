package com.example.swipeLayoutDemo

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.example.photoCutter.MeasurelUtils
import java.util.*
import kotlin.collections.HashMap

class SwipeRefreshLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    interface SwipeCallBack {
        fun callRefresh()
    }

    private var mWidth = 0
    private var mHeight = 0
    private var mAnimator: ValueAnimator? = null
    private val OND_DP = MeasurelUtils.convertDpToPixel(1f, context)
    private var mPrevY = 0f
    private var mSwipeCallBack: SwipeCallBack? = null
    /**下拉提示**/
    private var mTipsView: View? = null
    private var mOffsetY = 0f
    private val childYList = HashMap<View, Float>()

    fun setTipsView(view: View) {
        mTipsView = view
        addView(mTipsView, 0)
        view.visibility = INVISIBLE
    }



    override fun onTouchEvent(event: MotionEvent?): Boolean {
        var result = false
        when (event?.action) {
            MotionEvent.ACTION_MOVE -> {
                mTipsView?.visibility = VISIBLE
                val deltaY = event?.rawY - mPrevY
                setViewYAxis(deltaY)
                result = true
            }
            //使用动画回弹
            MotionEvent.ACTION_UP -> {
                mAnimator?.cancel()
                mAnimator = ValueAnimator.ofFloat(mOffsetY, 0f)
                mAnimator?.run {
                    var prevVal = mOffsetY
                    addUpdateListener { animation ->
                        val currentY = animation?.animatedValue as Float
                        val deltaY = currentY - prevVal
                        setViewYAxis(deltaY)
                        prevVal = currentY
                    }
                    addListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator?) {

                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            for (i in 0 until childCount) {
                                val v = getChildAt(i)
                                childYList[v]?.run {
                                    v.y = this@run
                                }
                            }
                            mTipsView?.visibility = INVISIBLE
                        }

                        override fun onAnimationCancel(animation: Animator?) {
                            onAnimationEnd(animation)
                        }

                        override fun onAnimationRepeat(animation: Animator?) {

                        }

                    })
                    duration = 300
                    start()
                    callRefresh()
                }
            }
        }
        mPrevY = event?.rawY!!
        Log.e("cjztest", "test1")
        return result
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        //如果容器处于移动状态，就拦截所有触摸事件，不分发到子控件了
        return onTouchEvent(ev)
    }

    private fun callRefresh() {
        mSwipeCallBack?.callRefresh()
    }

    private fun setViewYAxis(deltaY: Float) {
        if (mOffsetY + deltaY < 0 || mOffsetY + deltaY > mTipsView?.bottom!!) {
            return
        }
        mOffsetY += deltaY
        for (i in 0 until childCount) {
            val v = getChildAt(i)
            if (v === mTipsView) {
                continue
            }
            v.y += deltaY
        }
    }

    private fun recordYData() {
        for (i in 0 until childCount) {
            val v = getChildAt(i)
            if (v === mTipsView) {
                continue
            }
            childYList[v] = v.y
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        if (w != mWidth || h != mHeight) { //已经onMeasuer过一次，除非界面大小改动否则不重新初始化view
            mWidth = MeasureSpec.getSize(widthMeasureSpec)
            mHeight = MeasureSpec.getSize(heightMeasureSpec)
            recordYData()
        }
    }
}