package com.example.lifecycle

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel(), LifecycleObserver {
    //cjzmark 使用注解实现特定事件类型监听，SDK到时候会用反射调用
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun cjztestOnCreateEventObs() {
        Log.i("cjztest3", "androidx.lifecycle.LifecycleObserver#cjztestOnCreateEventObs")
    }
}