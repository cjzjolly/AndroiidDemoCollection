package com.example.lifecycle

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModel

/**ViewModel的作用感知生命周期以及数据的变化后立即更改view或执行对应生命周期的自定义操作,而不需要自己写view的操作代码**/
class MainViewModel : ViewModel(), LifecycleObserver {
    //cjzmark 使用注解实现特定事件类型监听，SDK到时候会用反射调用
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun cjztestOnCreateEventObs() {
        Log.i("cjztest3", "androidx.lifecycle.LifecycleObserver#cjztestOnCreateEventObs")
    }
}