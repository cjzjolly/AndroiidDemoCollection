package com.example.binderDemo;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class BindService extends Service {

    //copy from 疯狂安卓讲义Ver4
    private boolean quit = false;
    private int count = 0;

    protected class MyBinder extends Binder {
        public int getCount() {
            return count;
        }
    }
    private Binder binder = new MyBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("cjztest", "service已经绑定");
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(() -> {
            while (!quit) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                count++;
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        quit = true;
        super.onDestroy();
    }
}
