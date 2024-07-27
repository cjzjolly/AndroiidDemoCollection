package com.example.eraserSpeedUp;

import android.graphics.Canvas;
import android.graphics.Path;

public abstract class BaseCurv {

    public abstract void draw(float x, float y, int action, Canvas canvas);

    public abstract Path getPath();
}
