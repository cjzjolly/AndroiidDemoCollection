package com.example.binderDemo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class BinderDemoActivity extends Activity {

    private BindService.MyBinder binder;

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (BindService.MyBinder) iBinder;
            Log.d("cjztest", "service成功链接");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d("cjztest", "service断开链接");
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(BinderDemoActivity.this, BindService.class);

        Button startBtn = new Button(this);
        startBtn.setText("startService");
        startBtn.setOnClickListener(view -> {
//            startService(intent); //这种只适合于service完全独立运行，不需要与它通信的情况
            bindService(intent, conn, BindService.BIND_AUTO_CREATE);
        });

        Button stopBtn = new Button(this);
        stopBtn.setText("stopService");
        stopBtn.setOnClickListener(view -> {
//            stopService(intent); //这种只适合于service完全独立运行，不需要与它通信的情况
            unbindService(conn);
        });

        Button toastBtn = new Button(this);
        toastBtn.setText("show service's count");
        toastBtn.setOnClickListener(view -> {
            Toast
                    .makeText(BinderDemoActivity.this, "service的count计数已到：" + binder.getCount(),  Toast.LENGTH_SHORT)
                    .show();
        });

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(startBtn);
        linearLayout.addView(stopBtn);
        linearLayout.addView(toastBtn);


        setContentView(linearLayout);
    }
}
