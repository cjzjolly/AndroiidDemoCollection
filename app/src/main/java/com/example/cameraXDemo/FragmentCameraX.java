package com.example.cameraXDemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import android.app.Fragment;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.piccut.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FragmentCameraX extends Fragment implements LifecycleOwner {
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.camera_x_demo,null);
        //拍照专用线程，让它不要卡住主线程:
        mTakePhotoExecutor = Executors.newSingleThreadExecutor();
        //权限申请
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 1);
        }

        mLifecycleRegistry = new LifecycleRegistry(this);
        mLifecycleRegistry.markState(Lifecycle.State.CREATED);

        mPreviewView = (PreviewView) rootView.findViewById(R.id.pv);
        mBtnTakePhoto = rootView.findViewById(R.id.btn_take_photo);
        mImagePhoto = rootView.findViewById(R.id.iv_photo);
        ListenableFuture<ProcessCameraProvider> processCameraProvider = ProcessCameraProvider.getInstance(getContext());
        //todo : 放到resume年，onResume之后就不会黑屏。看起来像是线程被卡
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
//        }, getMainExecutor()); //只能在主线程
        }, ContextCompat.getMainExecutor(getContext())); //只能在主线程
        mBtnTakePhoto.setOnClickListener(v -> {
            takePhoto(mImageCapture);
        });
        return rootView;
    }


    @Override
    public void onStart() {
        super.onStart();
        mLifecycleRegistry.markState(Lifecycle.State.STARTED);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPreview != null) { //todo 这个mPreview onPause的时候被destroyed了，导致回来的时候预览卡住,可能要使用fragement避一下？
            //为预览窗口添加surface通道
            mPreview.setSurfaceProvider(mPreviewView.getSurfaceProvider());
        }
        mLifecycleRegistry.markState(Lifecycle.State.RESUMED);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLifecycleRegistry.markState(Lifecycle.State.DESTROYED);
        mTakePhotoExecutor.shutdown();
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
            mLensFacing = CameraSelector.LENS_FACING_FRONT;
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
        File cacheFileDir = getActivity().getExternalCacheDir(); //共用的、大家都可以访问的cache地址
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
                //照片拍好之后从回调里面接收
                imageCapture.takePicture(outputOptions, mTakePhotoExecutor, new ImageCapture.OnImageSavedCallback() {

                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri savedUri = outputFileResults.getSavedUri() == null ?
                                Uri.fromFile(newFile) :
                                outputFileResults.getSavedUri();
                        //给按钮弄个照片
                        getActivity().runOnUiThread(() -> {
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
                            getContext().sendBroadcast(
                                    new Intent(android.hardware.Camera.ACTION_NEW_PICTURE, savedUri)
                            );
                        }
                        // If the folder selected is an external media directory, this is
                        // unnecessary but otherwise other apps will not be able to access our
                        // images unless we scan them using [MediaScannerConnection]
                        String mimeType = MimeTypeMap.getSingleton()
                                .getMimeTypeFromExtension(".jpg");
                        MediaScannerConnection.scanFile(getContext(),
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
