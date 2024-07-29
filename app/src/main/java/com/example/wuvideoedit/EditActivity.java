package com.example.wuvideoedit;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wuvideoedit.databinding.ActivityMainBinding;

import java.nio.ByteBuffer;

public class EditActivity extends AppCompatActivity {

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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        setupView();
        timeLineLoad();
    }

    void setupView() {
        glSurfaceView = findViewById(R.id.videoSuraceView);
        mainRenderer = new MainRenderer(glSurfaceView);
        glSurfaceView.setEGLContextClientVersion(3);
        glSurfaceView.setRenderer(mainRenderer);

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