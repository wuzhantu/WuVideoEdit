package com.example.wuvideoedit;

import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.os.Handler;
import android.view.Choreographer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainRenderer implements GLSurfaceView.Renderer {

    static {
        System.loadLibrary("wuvideoedit");
    }

    private long videoRenderHandle;
    private long previewDecoderHandle;
    private long displayFrameHandle;
    private GLSurfaceView glSurfaceView;
    private Choreographer.FrameCallback frameCallback;
    private Handler updateHandler = new Handler();
    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            updateFrame();
            updateHandler.postDelayed(this, 33);
        }
    };

    public MainRenderer(GLSurfaceView glSurfaceView) {
        this.glSurfaceView = glSurfaceView;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        String basePath = Environment.getExternalStorageDirectory().getPath();
        String videoPath = basePath + "/xiaolin.MP4";
        previewDecoderHandle = previewDecoderInit(videoPath);
        videoRenderHandle = videoRenderInit(basePath);

        Thread videoThread = new Thread(new Runnable() {
            @Override
            public void run() {
                videoPlayDecode(previewDecoderHandle);
            }
        });
        videoThread.start();

        Thread audioThread = new Thread(new Runnable() {
            @Override
            public void run() {
                audioPlayDecode(previewDecoderHandle);
            }
        });
        audioThread.start();

        updateHandler.post(updateRunnable);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {

    }

    @Override
    public void onDrawFrame(GL10 gl10) {

    }

    void updateFrame() {
        updateFrame(previewDecoderHandle);
    }

    public void displayVideo(long frame) {
//        Thread currentThread = Thread.currentThread();
//        long threadid = currentThread.getId();
        glSurfaceView.queueEvent(() -> {
            displayFrame(videoRenderHandle, frame);
        });
    }
    public native long previewDecoderInit(String basePath);
    public native long videoRenderInit(String basePath);
    public native void videoPlayDecode(long previewDecoderHandle);
    public native void audioPlayDecode(long previewDecoderHandle);
    public native void updateFrame(long previewDecoderHandle);
    public native void displayFrame(long videoRenderHandle, long frameHandle);
}
