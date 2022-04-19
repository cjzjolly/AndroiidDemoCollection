package com.example.cameraXDemo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceRequest;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Consumer;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import com.example.piccut.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class MainActivity extends Activity implements LifecycleOwner {
    private ProcessCameraProvider mCameraPRrovider = null;
    private int mLensFacing = CameraSelector.LENS_FACING_BACK;
    private PreviewView mPreview;
    private LifecycleRegistry mLifecycleRegistry;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //权限申请
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }

        mLifecycleRegistry = new LifecycleRegistry(this);
        mLifecycleRegistry.markState(Lifecycle.State.CREATED);

        setContentView(R.layout.camera_x_demo);
        mPreview = (PreviewView) findViewById(R.id.pv);

        ListenableFuture<ProcessCameraProvider> processCameraProvider = ProcessCameraProvider.getInstance(this);
        processCameraProvider.addListener(() -> {
            try {
                mCameraPRrovider = processCameraProvider.get();
                if (hasBackCamera()) {
                    mLensFacing = CameraSelector.LENS_FACING_BACK;
                } if (hasFrontCamera()) {
//                    mLensFacing = CameraSelector.LENS_FACING_FRONT;
                } else {
                    throw new IllegalStateException("前后摄像头都没");
                }
                //绑定预览窗等
                bindCameraUseCases(mCameraPRrovider, null);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, getMainExecutor());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLifecycleRegistry.markState(Lifecycle.State.STARTED);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLifecycleRegistry.markState(Lifecycle.State.RESUMED);
    }

    /** Returns true if the device has an available back camera. False otherwise */
    private boolean hasBackCamera() {
        try {
            return mCameraPRrovider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA);
        } catch (CameraInfoUnavailableException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Returns true if the device has an available front camera. False otherwise */
    private boolean hasFrontCamera() {
        try {
            return mCameraPRrovider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA);
        } catch (CameraInfoUnavailableException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void bindCameraUseCases(ProcessCameraProvider cameraProvider, Surface surface) {
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(mLensFacing).build();
        int aspectRatio = AspectRatio.RATIO_4_3;
        //预览界面：
        Preview preview = new Preview.Builder()
                .setTargetAspectRatio(aspectRatio)
                .setTargetRotation(getDisplay().getRotation())
                .build();
        //照片抓取:
        ImageCapture imageCapture = new ImageCapture.Builder()
                .setTargetAspectRatio(aspectRatio)
                //低延迟拍照，反应速度会比较快
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetRotation(getDisplay().getRotation())
                .build();
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetAspectRatio(aspectRatio)
                .setTargetRotation(getDisplay().getRotation())
                .build();
        //解绑之前可能存在的绑定关系
        cameraProvider.unbindAll();
        //绑定生命周期、预览窗、拍照获取器等
        Camera camera = cameraProvider.bindToLifecycle(this,
                cameraSelector, preview, imageCapture, imageAnalysis);
        //为预览窗口添加surface通道
        preview.setSurfaceProvider(mPreview.getSurfaceProvider());
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return mLifecycleRegistry;
    }
}
