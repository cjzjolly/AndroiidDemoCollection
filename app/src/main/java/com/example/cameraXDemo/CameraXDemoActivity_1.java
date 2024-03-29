package com.example.cameraXDemo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import com.bumptech.glide.Glide;
import com.example.piccut.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CameraXDemoActivity_1 extends Activity implements LifecycleOwner {
    private final String TAG = getClass().getName();
    private ProcessCameraProvider mCameraPRrovider = null;
    private int mLensFacing = CameraSelector.LENS_FACING_BACK;
    private PreviewView mPreviewView;
    private LifecycleRegistry mLifecycleRegistry;
    /**拍照器**/
    private ImageCapture mImageCapture;
    private ExecutorService mTakePhotoExecutor;
    private Button mBtnTakePhoto;
    private ImageView mImagePhoto;
    private Preview mPreview;
    private Button mBtnFlashLight;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //拍照专用线程，让它不要卡住主线程:
        mTakePhotoExecutor = Executors.newSingleThreadExecutor();
        //权限申请
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }

        mLifecycleRegistry = new LifecycleRegistry(this);
        mLifecycleRegistry.markState(Lifecycle.State.CREATED);

        setContentView(R.layout.camera_x_demo);
        mBtnFlashLight = findViewById(R.id.btn_flash_light);
        mPreviewView = (PreviewView) findViewById(R.id.pv);
        mBtnTakePhoto = findViewById(R.id.btn_take_photo);
        mImagePhoto = findViewById(R.id.iv_photo);
        //拍照按钮
        mBtnTakePhoto.setOnClickListener(v -> {
            takePhoto(mImageCapture);
        });
        //闪光灯
        mBtnFlashLight.setOnClickListener(v -> {
            switch ((String) v.getTag()) {
                case "auto":
                    mImageCapture.setFlashMode(ImageCapture.FLASH_MODE_ON);
                    ((Button) v).setText("闪光灯:常开");
                    v.setTag("on");
                    break;
                case "on":
                    mImageCapture.setFlashMode(ImageCapture.FLASH_MODE_OFF);
                    ((Button) v).setText("闪光灯:关闭");
                    v.setTag("off");
                    break;
                default:
                    mImageCapture.setFlashMode(ImageCapture.FLASH_MODE_AUTO);
                    ((Button) v).setText("闪光灯:自动");
                    v.setTag("auto");
                    break;
            }
        });
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        bindCameraUseCases(mCameraPRrovider, null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLifecycleRegistry.markState(Lifecycle.State.STARTED);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ListenableFuture<ProcessCameraProvider> processCameraProvider = ProcessCameraProvider.getInstance(this);
        //回来的时候要重新绑定一下:
        processCameraProvider.addListener(() -> {
            try {
                mCameraPRrovider = processCameraProvider.get();
                //绑定预览窗等
                bindCameraUseCases(mCameraPRrovider, null);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, getMainExecutor()); //只能在主线程
        mLifecycleRegistry.markState(Lifecycle.State.RESUMED);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //出去的时候要释放相机资源
        if (null != mCameraPRrovider) {
            mCameraPRrovider.unbindAll();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLifecycleRegistry.markState(Lifecycle.State.DESTROYED);
        mTakePhotoExecutor.shutdown();
    }

    /** Returns true if the device has an available back camera. False otherwise */
    private boolean hasBackCamera() {
        try {
            if (null != mCameraPRrovider) {
                return mCameraPRrovider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA);
            }
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
        if (null == mPreviewView) {
            Log.e(TAG, "预览窗为空");
            return;
        }
        int aspectRatio = AspectRatio.RATIO_4_3;
        //预览界面：
        mPreview = new Preview.Builder()
                .setTargetAspectRatio(aspectRatio)
                .setTargetRotation(mPreviewView.getDisplay().getRotation())
                .build();
        //为预览窗口添加surface通道
        mPreview.setSurfaceProvider(mPreviewView.getSurfaceProvider());
        //照片抓取:
        mImageCapture = new ImageCapture.Builder()
                .setTargetAspectRatio(aspectRatio)
                //低延迟拍照，反应速度会比较快
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetRotation(mPreviewView.getDisplay().getRotation())
                .setFlashMode(ImageCapture.FLASH_MODE_AUTO) //闪光灯调节
                .build();
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetAspectRatio(aspectRatio)
                .setTargetRotation(mPreviewView.getDisplay().getRotation())
                .build();
        //解绑之前可能存在的绑定关系
        cameraProvider.unbindAll();
        //有后置摄像头就选后置，否则用前置:
        if (hasBackCamera()) {
            mLensFacing = CameraSelector.LENS_FACING_BACK;
        } if (hasFrontCamera()) {
//            mLensFacing = CameraSelector.LENS_FACING_FRONT;
        } else {
            throw new IllegalStateException("前后摄像头都没");
        }
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(mLensFacing).build();

        //绑定生命周期、预览窗、拍照获取器等
        cameraProvider.bindToLifecycle(this,
                cameraSelector, mPreview, mImageCapture, imageAnalysis);
    }

    /**实际拍照逻辑**/
    private void takePhoto(ImageCapture imageCapture) {
        if (null == imageCapture) {
            Log.e("cjztest", "imageCapture is null");
            return;
        }
        String fileFormatPattern = "yyyy-MM-dd-HH-mm-ss-SSS";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(fileFormatPattern);
        //先存放到APP本地文件夹:
//        File cacheFileDir = getCacheDir(); //APP内cache地址
        File cacheFileDir = getExternalCacheDir(); //共用的、大家都可以访问的cache地址
        if (cacheFileDir.exists() && cacheFileDir.isDirectory()) {
            File newFile = new File(cacheFileDir.getAbsolutePath() + String.format("/%s.jpg", simpleDateFormat.format(System.currentTimeMillis())));
            Log.i("cjztest", "newFile:" + newFile.getAbsolutePath());
            try {
                newFile.createNewFile();
                if (!newFile.exists()) {
                    return;
                }
                ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(newFile)
                        .setMetadata(new ImageCapture.Metadata())
                        .build();
                //照片拍好之后从回调里面接收(todo ： 改为使用原始数据转成PNG（定为后期任务？）)
                imageCapture.takePicture(mTakePhotoExecutor, new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        super.onCaptureSuccess(image);
//                        image.getImage().
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        super.onError(exception);
                    }
                });
                imageCapture.takePicture(outputOptions, mTakePhotoExecutor, new ImageCapture.OnImageSavedCallback() {

                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri savedUri = outputFileResults.getSavedUri() == null ?
                                Uri.fromFile(newFile) :
                                outputFileResults.getSavedUri();
                        //给按钮弄个照片
                        runOnUiThread(() -> {
                            if (newFile.exists()) {
                                Glide.with(mImagePhoto)
                                        .load(newFile)
//                                .apply(RequestOptions.circleCropTransform())
                                        .into(mImagePhoto);
                            }
                        });
                        // We can only change the foreground Drawable using API level 23+ API
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            // Update the gallery thumbnail with latest picture taken
//                            setGalleryThumbnail(savedUri);
                        }

                        // Implicit broadcasts will be ignored for devices running API level >= 24
                        // so if you only target API level 24+ you can remove this statement
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                            sendBroadcast(
                                    new Intent(android.hardware.Camera.ACTION_NEW_PICTURE, savedUri)
                            );
                        }
                        // If the folder selected is an external media directory, this is
                        // unnecessary but otherwise other apps will not be able to access our
                        // images unless we scan them using [MediaScannerConnection]
                        String mimeType = MimeTypeMap.getSingleton()
                                .getMimeTypeFromExtension(".jpg");
                        MediaScannerConnection.scanFile(CameraXDemoActivity_1.this,
                                new String[] {savedUri.getPath()},
                                new String[] {mimeType},
                                new MediaScannerConnection.OnScanCompletedListener() {
                                    @Override
                                    public void onScanCompleted(String path, Uri uri) {
                                        Log.i("cjztest", "新文件扫描完成, 文件大小:" + newFile.length());
                                    }
                                });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e("cjztest", "拍照失败");
                    }
                });
            } catch (IOException e) {
                Log.e("cjztest", "创建文件失败");
                e.printStackTrace();
            }
        }
        Log.i("cjztest", "cacheFileDir:" + cacheFileDir);
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return mLifecycleRegistry;
    }
}
