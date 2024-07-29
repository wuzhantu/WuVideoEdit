package com.example.wuvideoedit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;

import com.example.wuvideoedit.databinding.ActivityMainBinding;

import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'wuvideoedit' library on application startup.
    static {
        System.loadLibrary("wuvideoedit");
    }

    private ActivityMainBinding binding;
    private GLSurfaceView glSurfaceView;
    private MainRenderer mainRenderer;
    private long timeLineDecoderHandle;
    private VideoRecyclerViewAdapter recyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        copyAllAssetToSdCard();

        setupView();
        timeLineLoad();

        glSurfaceView = findViewById(R.id.videoSuraceView);
        mainRenderer = new MainRenderer(glSurfaceView);
        glSurfaceView.setEGLContextClientVersion(3);
        glSurfaceView.setRenderer(mainRenderer);
//        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    void copyAllAssetToSdCard() {
        copyAssetToSdCard("frame.vs");
        copyAssetToSdCard("frame.fs");
        copyAssetToSdCard("sticker.vs");
        copyAssetToSdCard("sticker.fs");
        copyAssetToSdCard("text.vs");
        copyAssetToSdCard("text.fs");
        copyAssetToSdCard("xiaolin.MP4");
    }

    void copyAssetToSdCard(String filename) {
        String assetFileName = filename; // 需要复制的assets文件名
        String outputPath = FileUtils.getExternalStoragePath() + "/" + filename; // 输出路径

        FileUtils.copyAssetToSdCard(this, assetFileName, outputPath);
    }

    void setupView() {
        RecyclerView recyclerView = findViewById(R.id.videoRecyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        recyclerView.setLayoutManager(manager);
        recyclerViewAdapter = new VideoRecyclerViewAdapter(this);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    void timeLineLoad() {
        String basePath = Environment.getExternalStorageDirectory().getPath();
        String videoPath = basePath + "/xiaolin.MP4";
        timeLineDecoderHandle = timeLineDecoderInit(videoPath);

        Thread videoThread = new Thread(new Runnable() {
            @Override
            public void run() {
                timeLineDecode(timeLineDecoderHandle);
            }
        });
        videoThread.start();
    }

    Bitmap getBitmap(int row) {

        // 初始化 Bitmap 和 Buffer
        int width = 1172; // 假设宽度
        int height = 720; // 假设高度

        byte[] buffer = new byte[width * height * 4]; // RGBA 格式
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // 调用 JNI 方法填充 buffer
        getTimeLineFrame(timeLineDecoderHandle, row, buffer);

        // 将 buffer 转换为 Bitmap 并显示在 ImageView 上
        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(buffer));

        return bitmap;
    }

    void refreshCells() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recyclerViewAdapter.notifyDataSetChanged();
            }
        });
    }

    public native long timeLineDecoderInit(String basePath);
    public native void timeLineDecode(long timeLineDecoderHandle);
    public native void getTimeLineFrame(long timeLineDecoderHandle, int row, byte[] buffer);
}