package com.example.binderDemo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

public class AidlService extends Service {

    private String colors[] = new String[] {"Red", "Green", "Blue"};
    private String color = colors[0];

    private double weight = 0;

    class CatBinder extends ICat.Stub {

        @Override
        public String getColor() throws RemoteException {
            return AidlService.this.color;
        }

        @Override
        public double getWeight() throws RemoteException {
            return AidlService.this.weight;
        }
    }
    private CatBinder mCatBinder;
    private Timer timer = new Timer();

    @Override
    public void onCreate() {
        super.onCreate();
        mCatBinder = new CatBinder();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                double rand = Math.random() * 3f;
                color = colors[(int) rand % colors.length];
                weight = rand;
            }
        }, 0, 800);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mCatBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }
}
