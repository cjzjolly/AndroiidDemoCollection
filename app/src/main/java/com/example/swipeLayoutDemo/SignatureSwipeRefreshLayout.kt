package com.example.swipeLayoutDemo

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class SignatureSwipeRefreshLayout(context: Context, attrs: AttributeSet?) :
    SwipeRefreshLayout(context, attrs) {

    interface SwipeRefreshDragging {
        fun downing(isDowning : Boolean)
    }
    var mSwipeRefreshDraggingCallback : SwipeRefreshDragging? = null

//    //FAILED:
//    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
//        val result = super.onInterceptTouchEvent(ev)
//        mSwipeRefreshDraggingCallback?.downing(result)
//        return result
//    }

    //failed:
//    override fun onNestedScroll(
//        target: View,
//        dxConsumed: Int,
//        dyConsumed: Int,
//        dxUnconsumed: Int,
//        dyUnconsumed: Int,
//        type: Int,
//        consumed: IntArray
//    ) {
//        super.onNestedScroll(
//            target,
//            dxConsumed,
//            dyConsumed,
//            dxUnconsumed,
//            dyUnconsumed,
//            type,
//            consumed
//        )
//        mSwipeRefreshDraggingCallback?.downing(true)
//    }

    //failed
//    override fun onNestedFling(
//        target: View,
//        velocityX: Float,
//        velocityY: Float,
//        consumed: Boolean
//    ): Boolean {
//        val result = super.onNestedFling(target, velocityX, velocityY, consumed)
//        mSwipeRefreshDraggingCallback?.downing(result)
//        return result
//    }

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?,
        type: Int,
        consumed: IntArray
    ) {
        super.dispatchNestedScroll(
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            offsetInWindow,
            type,
            consumed
        )
    }

    init {
        getChildAt(0).setOnDragListener { v, event ->
            mSwipeRefreshDraggingCallback?.downing(event?.action == DragEvent.ACTION_DRAG_ENTERED)
            Log.e("cjztest", "DragEvent:$event")
            false
        }
        setOnTouchListener { v, event ->
//            Log.e("cjztest", "asd: ${is}")
//            if () {
//
//            }
            false
        }
//        setOnChildScrollUpCallback { parent, child ->
//            Log.e("cjztest", "parentY: ${parent.progressCircleDiameter}")
//            false
//        }
        setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            Log.e("cjztest", "asd: ${v}, scrollX: $scrollX, scrollY： $scrollY, oldScrollX： $oldScrollX, oldScrollY： $oldScrollY")
        }
    }
}