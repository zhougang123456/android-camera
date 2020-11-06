package com.zhougang.camera;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class FindFaceView extends SurfaceView implements SurfaceHolder.Callback{
    private SurfaceHolder mHolder;
    private int mWidth;
    private int mHeight;
    public FindFaceView(Context context, AttributeSet attrs){
        super(context,attrs);
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setFormat(PixelFormat.TRANSPARENT);
        setZOrderOnTop(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
    public void drawRect(Camera.Face[] faces){
        Canvas canvas = mHolder.lockCanvas();
        if (canvas != null) {
            Paint clipPaint = new Paint();
            clipPaint.setAntiAlias(true);
            clipPaint.setStyle(Paint.Style.STROKE);
            clipPaint
                    .setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawPaint(clipPaint);
            //canvas.drawColor(getResources().getColor(color.transparent));
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.GREEN);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5.0f);
            Log.e("face_show:",faces.length+" faces have been showed!");
            for (int i = 0; i < faces.length; i++) {
                Rect rect = faces[i].rect;
                canvas.drawRect(
                        mWidth - (rect.bottom + 1000)* mWidth / 2000f,
                        mHeight - (rect.right + 1000) * mHeight / 2000f,
                        mWidth - (rect.top + 1000) * mWidth / 2000f,
                        mHeight - (rect.left + 1000) * mHeight / 2000f, paint);
            }
            mHolder.unlockCanvasAndPost(canvas);
        }
    }
}
