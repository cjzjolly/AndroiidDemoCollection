package com.example.swipeLayoutDemo

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.piccut.R

class MainActivity : Activity() {
    private var mSr: SwipeRefreshLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.swipe_demo)
        mSr = findViewById(R.id.sr_test)
        val v = View(this)
        v.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 300)
        v.setBackgroundColor(Color.RED)
        mSr?.setTipsView(v)
    }
}