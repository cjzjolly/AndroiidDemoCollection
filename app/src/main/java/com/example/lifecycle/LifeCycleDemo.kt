package com.example.lifecycle

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModelProvider

class LifeCycleDemo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(p0: LifecycleOwner, event: Lifecycle.Event) {
                //cjzmark 测试使用lifecycleEventObserver监听Activity生命周期
                Log.i("cjztest", "event:${event.name}")
            }
        })
        lifecycle.addObserver(object : LifecycleObserver {
            //cjzmark 使用注解实现特定事件类型监听，SDK到时候会用反射调用。毕竟用了注解+反射这种魔法。所以其实代码追踪的时候，并不那么的好追踪，我认为这样会增加调试难度
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun cjztestOnCreateEventObs() {
                Log.i("cjztest2", "androidx.lifecycle.LifecycleObserver#cjztestOnCreateEventObs")
            }
        })
        lifecycle.addObserver(MainViewModel())
    }
}