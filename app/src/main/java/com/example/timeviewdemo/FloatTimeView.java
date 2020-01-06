package com.example.timeviewdemo;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Wang.Wenhui
 * Date: 2020/1/6
 */
public class FloatTimeView extends View {
    private Paint textPaint;
    private float progress;
    private ValueAnimator animator;
    private String now, last;
    private Calendar calendar;
    private Date date;
    private SimpleDateFormat format;
    private float mTextSize, showTextSize;

    private float radius, cx, cy;

    public FloatTimeView(Context context) {
        this(context, null);
    }

    public FloatTimeView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatTimeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected void init() {
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.TRANSPARENT);

        animator = new ValueAnimator();
        animator.setFloatValues(0, 1);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            progress = (float) animation.getAnimatedValue();
            invalidate();
        });

        calendar = Calendar.getInstance();
        format = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
        date = new Date();
        now = format.format(date);
        last = now;
    }

    public void start() {
        post(tickRun);
    }

    public void stop() {
        removeCallbacks(tickRun);
    }

    public void destroy() {
        stop();
        animator.cancel();
    }

    private Runnable tickRun = new Runnable() {
        @Override
        public void run() {
            animator.cancel();
            calendar.setTimeInMillis(System.currentTimeMillis());
            int millis = calendar.get(Calendar.MILLISECOND);
            postDelayed(this, 1000 - millis);
            date.setTime(System.currentTimeMillis());
            last = now;
            now = format.format(date);
            animator.setDuration((1000 - millis) / 2);
            animator.start();
        }
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(widthSize, heightSize);
        setMeasuredDimension(size, size);
        radius = (float) ((getMeasuredWidth() >> 1) * 0.8);
        cx = cy = getMeasuredWidth() >> 1;
        mTextSize = (float) getMeasuredWidth() / 15;
        showTextSize = (float) getMeasuredWidth() / 6;
        textPaint.setTextSize(showTextSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画圆盘
        drawRulers(canvas);
        //圆盘到时间
        drawToText(canvas);
        //时间到圆盘
        drawFormText(canvas);
    }

    private void drawToText(Canvas canvas) {
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(showTextSize);
        float baseX = cx - textPaint.measureText(last) / 2;
        float baseY = cy + showTextSize / 2;
        float fx, fy, tx, ty;
        for (int i = 0; i < last.length(); i++) {
            String temp = String.valueOf(last.charAt(i));
            if (i == 0) {
                fx = baseX;
            } else {
                textPaint.setTextSize(showTextSize);
                fx = baseX + textPaint.measureText(last.substring(0, i));
            }
            fy = baseY;
            if (isNumber(last.charAt(i)) && !temp.equals(String.valueOf(now.charAt(i)))) {
                int target = Integer.parseInt(temp);
                float[] to = rulerPosition(target);
                tx = to[0];
                ty = to[1];
                textPaint.setTextSize(showTextSize + (mTextSize - showTextSize) * progress);
                canvas.drawText(temp, fx + (tx - fx) * progress, fy + (ty - fy) * progress, textPaint);
            }
        }
    }

    private void drawFormText(Canvas canvas) {
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(showTextSize);
        float baseX = cx - textPaint.measureText(now) / 2;
        float baseY = cy + showTextSize / 2;
        float fx, fy, tx, ty;
        for (int i = 0; i < now.length(); i++) {
            String temp = String.valueOf(now.charAt(i));
            if (i == 0) {
                fx = baseX;
            } else {
                textPaint.setTextSize(showTextSize);
                fx = baseX + textPaint.measureText(now.substring(0, i));
            }
            fy = baseY;
            if (isNumber(now.charAt(i)) && !temp.equals(String.valueOf(last.charAt(i)))) {
                int target = Integer.parseInt(temp);
                float[] to = rulerPosition(target);
                tx = to[0];
                ty = to[1];
                textPaint.setTextSize(mTextSize + (showTextSize - mTextSize) * progress);
                canvas.drawText(temp, tx + (fx - tx) * progress, ty + (fy - ty) * progress, textPaint);
            } else {
                textPaint.setTextSize(showTextSize);
                canvas.drawText(temp, fx, fy, textPaint);
            }
        }
    }

    private void drawRulers(Canvas canvas) {
        textPaint.setTextSize(mTextSize);
        for (int i = 0; i < 10; i++) {
            textPaint.setColor(Color.WHITE);
            String text = String.valueOf(i);
            for (int j = 0; j < now.length(); j++) {
                if (String.valueOf(now.charAt(j)).equals(String.valueOf(i)) || String.valueOf(last.charAt(j)).equals(String.valueOf(i))) {
                    textPaint.setColor(0x66666666);
                }
            }
            float[] position = rulerPosition(i);
            canvas.drawText(text, position[0], position[1], textPaint);
            textPaint.setColor(Color.WHITE);
        }
    }

    private float[] rulerPosition(int num) {
        float[] positions = new float[2];
        float ruler = (float) (Math.PI / 5);
        double offset = Math.PI / 2;
        double rulerAngle = num * ruler - offset;
        positions[0] = (float) (cx + radius * Math.cos(rulerAngle));
        positions[1] = ((float) (cy + radius * Math.sin(rulerAngle))) + mTextSize / 2;
        return positions;
    }

    private boolean isNumber(char c) {
        return c >= '0' && c <= '9';
    }

}
