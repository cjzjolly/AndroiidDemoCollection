package com.example.dctDemo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class DCTTestDemo extends Activity {
//    private int mGrayPixels[] = {78,75,79,82,82,86,94,94,76,78,76,82,83,86,85,94,72,75,67,78,80,78,74,82,74,76,75,75,86,80,81,79,73,70,75,67,78,78,79,85,69,63,68,69,75,78,82,80,76,76,71,71,67,79,80,83,72,77,78,69,75,75,78,78};
//    private int mGrayPixels[] = {255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255};
    private int mGrayPixels[] = {0,255,255,255,255,255,255,255,
        255,0,255,255,255,255,255,0,
        255,255,0,255,255,255,0,255,
        255,255,255,0,255,0,255,255,
        255,255,255,0,0,255,255,255,
        255,255,0,255,255,0,255,255,
        255,0,255,255,255,255,0,255,
        255,255,255,255,255,255,255,0};
    private DCTBaseView mDCTBaseMatrix[][] = new DCTBaseView[8][8];
    private double mDCTTransResult[][] = new double[8][8];
    private double mDCTReverseTransResult[][] = new double[8][8];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScrollView scrollViewContainer = new ScrollView(this);
        LinearLayout totalView = new LinearLayout(this);
        totalView.setOrientation(LinearLayout.VERTICAL);
        totalView.setGravity(Gravity.CENTER);
        //输入图像展示:
        TextView inputSignalTitle = new TextView(this);
        inputSignalTitle.setText("输入图像展示");
        totalView.addView(inputSignalTitle);
        for (int y = 0; y < 8; y ++) {
            LinearLayout hLinearLayout = new LinearLayout(this);
            hLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            for (int x = 0; x < 8; x ++) {
                TextView b = new TextView(this);
                b.setText(String.format("%d", mGrayPixels[y * 8 + x]));
                double val = Math.abs(mGrayPixels[y * 8 + x]);
                b.setBackgroundColor(0xFF000000 | ((int) val & 0xFF) << 8 * 2);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(8 * 15, 8 * 8);
                layoutParams.setMargins(8, 8, 8, 8);
                b.setLayoutParams(layoutParams);
                hLinearLayout.addView(b);
            }
            totalView.addView(hLinearLayout);
        }
        //DCT输入信号展示：
        BaseView inputSignalView = new BaseView(this);
        LinearLayout.LayoutParams inputSignalViewLayoutParams = new LinearLayout.LayoutParams(8 * 15, 8 * 15);
        inputSignalView.setLayoutParams(inputSignalViewLayoutParams);
        for (int y = 0; y < 8; y ++) {
            for (int x = 0; x < 8; x++) {
                inputSignalView.setInputSignal(x, y, mGrayPixels[y * 8 + x] / 255f);
            }
        }
        totalView.addView(inputSignalView);

        //DCT基展示
        TextView DCTBaseMatrixTitle = new TextView(this);
        DCTBaseMatrixTitle.setText("DCT变换使用的基表");
        totalView.addView(DCTBaseMatrixTitle);
        for (int y = 0; y < 8; y ++) {
            LinearLayout hLinearLayout = new LinearLayout(this);
            hLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            for (int x = 0; x < 8; x ++) {
                DCTBaseView b = new DCTBaseView(this);
                b.calcDCTBase(x, y);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(8 * 15, 8 * 15);
                layoutParams.setMargins(8, 8, 8, 8);
                b.setLayoutParams(layoutParams);
                hLinearLayout.addView(b);
                mDCTBaseMatrix[x][y] = b;
            }
            totalView.addView(hLinearLayout);
        }

        //DCT变换后的频域矩阵，不同uv坐标的dct基的x,y的值 * 原信号的x,y的值
        for (int u = 0; u < 8; u ++) {
            for (int v = 0; v < 8; v ++) {
                double base = 0;
                for (int x = 0; x < 8; x ++) {
                    for (int y = 0; y < 8; y ++) {
                        base += mGrayPixels[y * 8 + x] * mDCTBaseMatrix[u][v].getSignalDCTBaseVal(x, y);
                    }
                }
                mDCTTransResult[u][v] = 1f / 4f * base / Constant.DCT_BRIGHTNESS_TRANS_TABLE[u * 8 + v];
            }
        }

        //DCT变换后的画面呈现
        TextView DCTTitle = new TextView(this);
        DCTTitle.setText("DCT变换 + 亮度表量化后的结果：");
        totalView.addView(DCTTitle);
        for (int y = 0; y < 8; y ++) {
            LinearLayout hLinearLayout = new LinearLayout(this);
            hLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            for (int x = 0; x < 8; x ++) {
                TextView b = new TextView(this);
                b.setText(String.format("%.2f", mDCTTransResult[x][y]));
                double val = Math.abs(mDCTTransResult[x][y]);
                b.setBackgroundColor(0xFF000000 | ((int) val & 0xFF));
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(8 * 15, 8 * 8);
                layoutParams.setMargins(8, 8, 8, 8);
                b.setLayoutParams(layoutParams);
                hLinearLayout.addView(b);
            }
            totalView.addView(hLinearLayout);
        }

        //DCT逆变换：
        for (int y = 0; y < 8; y ++) {
            for (int x = 0; x < 8; x ++) {
                double base = 0;
                for (int u = 0; u < 8; u++) {
                    for (int v = 0; v < 8; v++) {
                        //逆亮度量化，逆DCT
                        base += mDCTTransResult[u][v] * Constant.DCT_BRIGHTNESS_TRANS_TABLE[u * 8 + v] * mDCTBaseMatrix[u][v].getSignalDCTBaseVal(x, y);
                    }
                }
                mDCTReverseTransResult[x][y] = 1f / 4f * base;
            }
        }

        //DCT逆变换数据呈现
        TextView reverseDCTTitle = new TextView(this);
        reverseDCTTitle.setText("逆亮度量化 + DCT逆变换");
        totalView.addView(reverseDCTTitle);
        for (int y = 0; y < 8; y ++) {
            LinearLayout hLinearLayout = new LinearLayout(this);
            hLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            for (int x = 0; x < 8; x ++) {
                TextView b = new TextView(this);
                b.setText(String.format("%.2f", mDCTReverseTransResult[x][y]));
                double val = Math.abs(mDCTReverseTransResult[x][y]);
                b.setBackgroundColor(0xFF000000 | ((int) (val > 255 ? 255 : val) & 0xFF) << 8 * 2);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(8 * 15, 8 * 8);
                layoutParams.setMargins(8, 8, 8, 8);
                b.setLayoutParams(layoutParams);
                hLinearLayout.addView(b);
            }
            totalView.addView(hLinearLayout);
        }

        //DCT逆变换图像呈现
        BaseView reverseSignalView = new BaseView(this);
        LinearLayout.LayoutParams reverseSignalViewLp = new LinearLayout.LayoutParams(8 * 15, 8 * 15);
        reverseSignalView.setLayoutParams(reverseSignalViewLp);
        for (int y = 0; y < 8; y ++) {
            for (int x = 0; x < 8; x++) {
                double val = Math.abs(mDCTReverseTransResult[x][y]);
                reverseSignalView.setInputSignal(x, y, (val > 255f ? 255f : val) / 255f);
            }
        }
        totalView.addView(reverseSignalView);

        //添加到滚动窗
        scrollViewContainer.addView(totalView);

        setContentView(scrollViewContainer);










    }
}