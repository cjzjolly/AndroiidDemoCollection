package com.example.swipeLayoutDemo

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.example.piccut.R

class MainActivity : Activity() {
    private var mSr: SwipeRefreshLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.swipe_demo)
        mSr = findViewById(R.id.sr_test)
    }
}