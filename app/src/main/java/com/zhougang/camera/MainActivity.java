package com.zhougang.camera;


import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private SurfaceView cameraView;
    private FindFaceView faceView;
    private Camera camera;
    private MediaCodec mediaCodec;
    private BufferedOutputStream output;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraView = findViewById(R.id.camera_view);
        faceView = findViewById(R.id.face_view);
        SurfaceHolder holder = cameraView.getHolder();
        holder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.h264";
        File file = new File(path);
        if (!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            output = new BufferedOutputStream(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        camera = Camera.open(1);
        try {
            Camera.Parameters parameters = camera.getParameters();
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                parameters.set("orientation","portrait");
                camera.setDisplayOrientation(0);
                parameters.setRotation(90);
            } else {
                parameters.set("orientation", "landscape");
                camera.setDisplayOrientation(90);
                parameters.setRotation(90);
            }
            parameters.setPreviewSize(640,480);
            parameters.setPreviewFormat(ImageFormat.YV12);
            camera.setParameters(parameters);
            camera.setPreviewDisplay(holder);
        }catch (Exception e){
            e.printStackTrace();
        }
        /*camera.setFaceDetectionListener(new Camera.FaceDetectionListener() {
            @Override
            public void onFaceDetection(Camera.Face[] faces, Camera camera) {
                if (faces !=null && faces.length!=0) {
                    Log.e("face_detection:",faces.length+" faces have been detected!");
                    faceView.setVisibility(View.VISIBLE);
                    faceView.drawRect(faces);
                } else {
                    faceView.setVisibility(View.GONE);
                }
            }
        });*/
        init();
        camera.addCallbackBuffer(new byte[640 * 480 * 3 / 2]);
        camera.startPreview();
        camera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                try {
                    onEncodeFrame(data,0,data.length,0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                camera.addCallbackBuffer(data);
            }
        });
        //camera.startFaceDetection();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopFaceDetection();
        camera.stopPreview();
        camera.release();
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onEncodeFrame(byte[] buffer, int offset, int length, int flag) throws IOException {
        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
        ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
        int inputBufferIndex = mediaCodec.dequeueInputBuffer(0);
        if (inputBufferIndex >= 0){
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(buffer,offset,length);
            mediaCodec.queueInputBuffer(inputBufferIndex,0,length,System.currentTimeMillis(),0);
        }else {
            return;
        }
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo,0);
        while (outBufferIndex >= 0){

            ByteBuffer outBuffer = outputBuffers[outBufferIndex];
            byte[] outData = new byte[bufferInfo.size];
            outBuffer.get(outData);
            output.write(outData);
            mediaCodec.releaseOutputBuffer(outBufferIndex,true);
            outBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo,0);
        }
    }
    public void init(){
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mediaCodec = MediaCodec.createEncoderByType("video/avc");
                Log.e("mediaCodec "," " + mediaCodec);
                MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc",640,480);
                mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE,250000);
                mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE,15);
                mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);
                mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,-1);
                mediaCodec.configure(mediaFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
                mediaCodec.start();
            }
        }catch (IOException e){
            e.printStackTrace();
        }

    }
}
