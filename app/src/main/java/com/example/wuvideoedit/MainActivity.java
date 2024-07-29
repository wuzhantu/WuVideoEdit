package com.example.wuvideoedit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.Manifest;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.wuvideoedit.databinding.ActivityMainBinding;

import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupView();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
        } else {
            // 已授予权限，执行需要权限的代码
            copyAllAssetToSdCard();
        }
    }

    void setupView() {
        Button startEditButton = findViewById(R.id.startEditButton);
        startEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                startActivity(intent);
            }
        });
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 用户允许权限，执行需要权限的代码
                copyAllAssetToSdCard();
            } else {
                // 用户拒绝权限，显示提示信息或采取其他措施
                Toast.makeText(this, "存储权限被拒绝，无法执行操作", Toast.LENGTH_SHORT).show();
            }
        }
    }
}