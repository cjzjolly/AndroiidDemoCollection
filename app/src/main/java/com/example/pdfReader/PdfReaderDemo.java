package com.example.pdfReader;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;

public class PdfReaderDemo extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        File file = new File("/sdcard/eBook/Reading/gcm_lsm.pdf");
        if (!file.exists()) {
            Log.i("cjztest", "test___0");
        } else {
            Log.i("cjztest", "test___1:" + file.length());
            readPdf(file);
        }
    }

    private void readPdf(File pdfFile) {

    }
}
