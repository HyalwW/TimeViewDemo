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
public class WebSurfaceView extends SurfaceView implements Handler.Callback, SurfaceHolder.Callback {
    private Paint mPaint;
    private Random random;
    private List<WebDot> dots;
    private SurfaceHolder holder;
    private HandlerThread mHandlerThread;
    private Handler drawHandler;
    private boolean running;
    private static final long UPDATE_RATE = 16;
    private MsgBuilder builder;

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

        builder = new MsgBuilder();
    }

    private void drawEverything() {
        Canvas canvas = holder.lockCanvas();
        if (canvas != null) {
            clearCanvas(canvas);
            drawDots(canvas);
            drawLines(canvas);
            holder.unlockCanvasAndPost(canvas);
        }
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
                    int alpha;
                    if (distance < 150) {
                        alpha = 255;
                    } else {
                        alpha = (int) (255 - 255 * (distance - 150) / 50);
                    }
                    mPaint.setColor(Color.argb(alpha, (sd.r + ed.r) / 2, (sd.g + ed.g) / 2, (sd.b + ed.b) / 2));
                    canvas.drawLine(sd.x, sd.y, ed.x, ed.y, mPaint);
                }
            }
        }
    }

    public void start() {
        running = true;
        builder.newMsg().what(0).send();
    }

    public void pause() {
        running = false;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                builder.newMsg().what(1).send();
                break;
            case MotionEvent.ACTION_MOVE:
                builder.newMsg().what(3).send();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                builder.newMsg().what(2).send();
                break;
        }
        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mHandlerThread = new HandlerThread("drawThread");
        if (dots == null) {
            dots = new ArrayList<>();
            for (int i = 0; i < 120; i++) {
                dots.add(new WebDot());
            }
        }
        mHandlerThread.start();
        drawHandler = new Handler(mHandlerThread.getLooper(), this);
        start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mHandlerThread.quitSafely();
        } else {
            mHandlerThread.quit();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 0:
                if (running) {
                    long startTime = System.currentTimeMillis();
                    for (WebDot dot : dots) {
                        dot.move();
                    }
                    drawEverything();
                    long drawTime = System.currentTimeMillis() - startTime;
                    long l = UPDATE_RATE - drawTime;
                    builder.newMsg().what(0).sendDelay(l < 0 ? 10 : l);
                }
                break;
            case 1:
                pause();
                break;
            case 2:
                start();
                break;
            case 3:
                for (WebDot dot : dots) {
                    dot.move();
                }
                drawEverything();
                break;
        }
        return true;
    }

    private class MsgBuilder {
        private Message message;

        MsgBuilder() {
            message = Message.obtain();
        }

        MsgBuilder newMsg() {
            message = Message.obtain();
            return this;
        }

        MsgBuilder what(int what) {
            message.what = what;
            return this;
        }

        MsgBuilder obj(Object obj) {
            message.obj = obj;
            return this;
        }

        MsgBuilder args(int arg1, int arg2) {
            message.arg1 = arg1;
            message.arg2 = arg2;
            return this;
        }

        void send() {
            sendDelay(0);
        }

        void sendDelay(long millis) {
            drawHandler.sendMessageAtTime(message, millis);
        }
    }

    private class WebDot implements Comparable<WebDot> {

        float x, y, icmX, icmY;
        int r, g, b;

        WebDot() {
            x = random.nextInt(getMeasuredWidth());
            y = random.nextInt(getMeasuredHeight());
            icmX = -5 + random.nextFloat() * 10;
            icmY = -5 + random.nextFloat() * 10;
            r = random.nextInt(255);
            g = random.nextInt(255);
            b = random.nextInt(255);
        }

        void move() {
            if (x > getMeasuredWidth() || x < 0) {
                if (x > getMeasuredWidth()) {
                    x = getMeasuredWidth();
                } else {
                    x = 0;
                }
                icmX = -icmX;
            }
            if (y > getMeasuredHeight() || y < 0) {
                if (y > getMeasuredHeight()) {
                    y = getMeasuredHeight();
                } else {
                    y = 0;
                }
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
            return ((int) x - (int) o.x);
        }
    }
}
