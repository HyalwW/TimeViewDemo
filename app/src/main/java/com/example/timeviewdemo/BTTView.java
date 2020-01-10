package com.example.timeviewdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;

public class BTTView extends BaseSurfaceView {
    private Bitmap bitmap, base;
    private String[][] strings;
    private String[] temps;
    private int colorGap;
    private int textSize;
    //一行多少字
    private int textInLine = 250;
    private Rect dst;

    public BTTView(Context context) {
        super(context);
    }

    public BTTView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BTTView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onInit() {
        base = BitmapFactory.decodeResource(getResources(), R.drawable.test1);
        temps = new String[]{"一", "二", "三", "四", "五"};
        colorGap = -Color.BLACK / temps.length;
        dst = new Rect();
        mPaint.setFilterBitmap(true);
    }

    @Override
    protected void onDataUpdate() {
    }

    public void draw() {
        if (textInLine == 0) {
            return;
        }
        stopAnim();
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
        bitmap = scaleBitmap(base, (float) textInLine / base.getWidth());
        strings = new String[bitmap.getHeight()][bitmap.getWidth()];
        textSize = getMeasuredWidth() / textInLine;
        for (int i = 0; i < strings.length; i++) {
            for (int j = 0; j < strings[i].length; j++) {
                int pixel = bitmap.getPixel(j, i);
                strings[i][j] = genText(pixel);
            }
        }
        callDraw(strings);
    }

    private String genText(int pixel) {
        int index = temps.length - (pixel - Color.BLACK) / colorGap - 1;
        return temps[index < 0 ? 0 : index];
    }

    @Override
    protected void onRefresh(Canvas canvas) {

    }

    @Override
    protected void draw(Canvas canvas, Object data) {
        canvas.drawColor(Color.WHITE);
        mPaint.setTextSize(textSize);
        mPaint.setColor(Color.CYAN);
        StringBuilder builder;
        for (int i = 0; i < strings.length; i++) {
            String[] string = strings[i];
            builder = new StringBuilder();
            for (String s : string) {
                builder.append(s);
            }
            canvas.drawText(builder.toString(), 0, textSize * (i + 1), mPaint);
        }
        int top = textSize * (strings.length + 1);
        dst.set(0, top, getMeasuredWidth(), (int) (top + base.getHeight() * ((float) getMeasuredWidth() / base.getWidth())));
        canvas.drawBitmap(base, null, dst, mPaint);
    }

    private Bitmap scaleBitmap(Bitmap origin, float ratio) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(ratio, ratio);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        return newBM;
    }

}
