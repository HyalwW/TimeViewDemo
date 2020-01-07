package com.example.timeviewdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by Wang.Wenhui
 * Date: 2020/1/7
 */
public class WebSurfaceView extends SurfaceView implements Runnable, SurfaceHolder.Callback {
    private Paint mPaint;
    private Random random;
    private List<WebDot> dots;
    private SurfaceHolder holder;
    private Thread drawThread;
    private boolean flag, running;
    private long UPDATE_RATE = 16;

    public WebSurfaceView(Context context) {
        this(context, null);
    }

    public WebSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WebSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        random = new Random();

        holder = getHolder();
        holder.addCallback(this);
        running = true;
    }

    private void drawEverything() {
        Canvas canvas = holder.lockCanvas();
        clearCanvas(canvas);
        drawDots(canvas);
        drawLines(canvas);
        holder.unlockCanvasAndPost(canvas);
    }

    private void clearCanvas(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }

    private void drawDots(Canvas canvas) {
        for (WebDot dot : dots) {
            mPaint.setColor(Color.rgb(dot.r, dot.g, dot.b));
            canvas.drawCircle(dot.x, dot.y, 2, mPaint);
        }
    }

    private void drawLines(Canvas canvas) {
        Collections.sort(dots);
        for (int i = 0; i < dots.size() - 1; i++) {
            for (int j = i + 1; j < dots.size(); j++) {
                WebDot sd = dots.get(i), ed = dots.get(j);
                float distance = sd.distance(ed);
                if (Math.abs(sd.x - ed.x) > 200)
                    break;
                if (distance < 200 && sd != ed) {
                    mPaint.setColor(Color.rgb((sd.r + ed.r) / 2, (sd.g + ed.g) / 2, (sd.b + ed.b) / 2));
                    canvas.drawLine(sd.x, sd.y, ed.x, ed.y, mPaint);
                }
            }
        }
    }

    public void start() {
        running = true;
    }

    public void pause() {
        running = false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawThread = new Thread(this);
        flag = true;
        if (dots == null) {
            dots = new ArrayList<>();
            for (int i = 0; i < 120; i++) {
                dots.add(new WebDot());
            }
        }
        drawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        flag = false;
    }

    @Override
    public void run() {
        while (flag) {
            if (running) {
                for (WebDot dot : dots) {
                    dot.move();
                }
                drawEverything();
            }
            try {
                Thread.sleep(UPDATE_RATE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class WebDot implements Comparable<WebDot> {
        float x, y, icmX, icmY;
        int r, g, b;

        WebDot() {
            this.x = random.nextInt(getMeasuredWidth());
            this.y = random.nextInt(getMeasuredHeight());
            this.icmX = -5 + random.nextInt(10);
            this.icmY = -5 + random.nextInt(10);
            this.r = random.nextInt(255);
            this.g = random.nextInt(255);
            this.b = random.nextInt(255);
        }

        void move() {
            if (x > getMeasuredWidth() || x < 0) {
                icmX = -icmX;
            }
            if (y > getMeasuredHeight() || y < 0) {
                icmY = -icmY;
            }
            x += icmX;
            y += icmY;
        }

        float distance(WebDot dot) {
            return (float) Math.sqrt((x - dot.x) * (x - dot.x) + (y - dot.y) * (y - dot.y));
        }

        @Override
        public int compareTo(WebDot o) {
            return (int) (x - o.x);
        }
    }
}
