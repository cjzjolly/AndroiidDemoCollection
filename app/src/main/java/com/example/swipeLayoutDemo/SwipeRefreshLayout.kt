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



    fun touch(event: MotionEvent?): Boolean {
        var result = false
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownY = event?.rawY
                mAnimator?.cancel()
            }
            MotionEvent.ACTION_MOVE -> {
                if (event?.rawY - mDownY > 30) {
                    mTipsView?.visibility = VISIBLE
                    val deltaY = event?.rawY - mPrevY
                    setViewYAxis(deltaY)
                    result = true
                } else {
                    result = false
                }
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
        return result
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean { //由自定义逻辑接管事件分发
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            mPreDispatchDownY = ev?.rawY
        }
        val child = getChildAt(1) as ViewGroup
        //判断子容器是否滚动到最前面的位置，而且不是向下滚
        val deltaY = ev?.rawY!! - mPreDispatchDownY
        if ((child.scrollY == 0) && deltaY > 0) {
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