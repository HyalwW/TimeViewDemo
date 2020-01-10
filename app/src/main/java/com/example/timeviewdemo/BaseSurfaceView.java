package com.example.timeviewdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Wang.Wenhui
 * Date: 2020/1/10
 */
public abstract class BaseSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Handler.Callback {
    private HandlerThread mHandlerThread;
    private Handler drawHandler;
    private MsgBuilder builder;
    private SurfaceHolder holder;
    protected long UPDATE_RATE = 16;
    protected Paint mPaint;
    private boolean running = true;

    public BaseSurfaceView(Context context) {
        this(context, null);
    }

    public BaseSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setFilterBitmap(true);

        holder = getHolder();
        holder.addCallback(this);

        builder = new MsgBuilder();
        onInit();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 207:
                drawEverything(null);
                builder.newMsg().what(207).sendDelay(UPDATE_RATE);
                break;
            case 208:
                drawEverything(msg.obj);
                break;

        }
        return true;
    }

    private synchronized void drawEverything(Object data) {
        Canvas canvas = holder.lockCanvas();
        if (canvas != null) {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            if (running) {
                onRefresh(canvas);
            }
            if (data != null) {
                draw(canvas, data);
            }
            if (holder.getSurface().isValid()) {
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mHandlerThread = new HandlerThread("drawThread");
        mHandlerThread.start();
        drawHandler = new Handler(mHandlerThread.getLooper(), this);
        startAnim();
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

    public void startAnim() {
        running = true;
        builder.newMsg().what(207).send();
    }

    public void stopAnim() {
        running = false;
    }

    public void callDraw(Object data) {
        builder.newMsg().obj(data).what(208).send();
    }

    private final class MsgBuilder {

        private Message message;

        MsgBuilder newMsg() {
            message = Message.obtain();
            return this;
        }

        MsgBuilder what(int what) {
            checkMessageNonNull();
            message.what = what;
            return this;
        }

        MsgBuilder obj(Object o) {
            checkMessageNonNull();
            message.obj = o;
            return this;
        }

        void send() {
            sendDelay(0);
        }

        private void sendDelay(long millis) {
            checkMessageNonNull();
            onDataUpdate();
            drawHandler.sendMessageAtTime(message, millis);
        }

        private void checkMessageNonNull() {
            if (message == null) {
                throw new IllegalStateException("U should call newMsg() before use");
            }
        }

    }

    protected abstract void onInit();

    protected abstract void onDataUpdate();

    protected abstract void onRefresh(Canvas canvas);

    protected abstract void draw(Canvas canvas, Object data);
}
