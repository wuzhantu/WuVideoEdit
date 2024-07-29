package com.example.wuvideoedit;

import android.content.Context;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {
    /**
     * 将assets目录下的文件写入到SD卡
     *
     * @param context    上下文
     * @param assetName  assets目录下的文件名
     * @param outputPath 输出路径
     */
    public static void copyAssetToSdCard(Context context, String assetName, String outputPath) {
        InputStream inputStream = null;
        FileOutputStream outputStream = null;

        try {
            // 打开assets目录下的文件
            inputStream = context.getAssets().open(assetName);

            // 创建输出文件
            File outFile = new File(outputPath);
            outFile.getParentFile().mkdirs();
            outFile.createNewFile();

            // 打开输出流
            outputStream = new FileOutputStream(outFile);

            // 缓冲区大小
            byte[] buffer = new byte[1024];
            int length;

            // 从输入流中读取数据并写入输出流
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            // 刷新输出流
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭流
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取SD卡路径
     *
     * @return SD卡路径
     */
    public static String getExternalStoragePath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }
}
