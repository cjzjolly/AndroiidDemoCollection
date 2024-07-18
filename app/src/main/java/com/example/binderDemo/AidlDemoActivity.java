package com.example.binderDemo;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class AidlDemoActivity extends Activity {
    private ICat mCatService = null;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) { //todo 没跑进来
            mCatService = ICat.Stub.asInterface(iBinder);
            Log.d("cjztest", "android.content.ServiceConnection.onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mCatService = null;
            Log.d("cjztest", "android.content.ServiceConnection.onServiceDisconnected");
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent();
        intent.setAction("com.example.binderDemo.action.AIDL_SERVICE"); //manifest中对应的action事件
        intent.setPackage("com.example.binderDemo");
        bindService(intent, connection, Service.BIND_AUTO_CREATE);

        Button button = new Button(this);
        button.setOnClickListener(view -> {
            try {
                Toast
                        .makeText(AidlDemoActivity.this
                                , String.format("name:%s, weight:%f", mCatService.getColor(), mCatService.getWeight()),
                                Toast.LENGTH_SHORT)
                        .show();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });



        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(button);

        setContentView(linearLayout);
    }
}
