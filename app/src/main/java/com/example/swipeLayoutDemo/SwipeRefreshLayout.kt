package com.example.swipeLayoutDemo

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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
    private var mPrevY = 0f
    private var mDownY = 0f
    private var mSwipeCallBack: SwipeCallBack? = null
    /**下拉提示**/
    private var mTipsView: View? = null
    private var mOffsetY = 0f
    private val childYList = HashMap<View, Float>()
    private var mPreDispatchDownY = 0f

    fun setTipsView(view: View) {
        mTipsView = view
        addView(mTipsView, 0)
        view.visibility = INVISIBLE
    }

    fun setSwipeCallBack(cb : SwipeCallBack) {
        mSwipeCallBack = cb
    }

    fun touch(event: MotionEvent?): Boolean {
        var result = false
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownY = event?.rawY!!
                mAnimator?.cancel()
            }
            MotionEvent.ACTION_MOVE -> {
                if (event?.rawY!! - mDownY > 30) {
                    mTipsView?.visibility = VISIBLE
                    val deltaY = event?.rawY!! - mPrevY
                    setViewYAxis(deltaY)
                    result = true
                } else {
                    result = false
                }
            }
            //使用动画回弹
            MotionEvent.ACTION_UP -> {
                startAnimateFadeUp()
                callRefresh()
            }
        }
        mPrevY = event?.rawY!!
        return result
    }

    private fun startAnimateFadeUp () {
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
                override fun onAnimationStart(p0: Animator) {

                }

                override fun onAnimationEnd(p0: Animator) {
                    for (i in 0 until childCount) {
                        val v = getChildAt(i)
                        childYList[v]?.run {
                            v.y = this@run
                        }
                    }
                    mTipsView?.visibility = INVISIBLE
                }

                override fun onAnimationCancel(p0: Animator) {
                    onAnimationEnd(p0)
                }

                override fun onAnimationRepeat(p0: Animator) {

                }

            })
            duration = 300
            start()
            callRefresh()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean { //由自定义逻辑接管事件分发
        if (getChildAt(1) == null || getChildAt(1) !is ViewGroup) {
            super.dispatchTouchEvent(ev)
        }
        val child = getChildAt(1) as ViewGroup
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                mPreDispatchDownY = ev?.rawY!!
                child.onTouchEvent(ev)
            }
            MotionEvent.ACTION_UP -> {
                child.onTouchEvent(ev)
            }
        }
        //判断子容器是否滚动到最前面的位置，而且不是向上滚
        val deltaY = ev?.rawY!! - mPreDispatchDownY
        if (!child.canScrollVertically(-1) && deltaY > 0) {
            touch(ev)
        } else {
            child.dispatchTouchEvent(ev)
        }
        return true
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