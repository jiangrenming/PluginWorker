package com.jrm.plugindemo.utils;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 *
 * @author jiangrenming
 * @date 2018/1/24
 * 讲assets中的某个文件复制到sd卡中
 */

public class AssentsCopyToSdCard {

    public static  boolean copyFilesFassets(Context context, String oldPath, String newPath) {
        boolean reslut = false;
        try {
            String fileNames[] = context.getAssets().list(oldPath);//获取assets目录下的所有文件及目录名
            if (fileNames.length > 0) {//如果是目录
                File file = new File(newPath);
                file.mkdirs();//如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    copyFilesFassets(context, oldPath + "/" + fileName, newPath + "/" + fileName);
                }

            } else {//如果是文件
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1) {//循环从输入流读取 buffer字节
                    fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
                }
                fos.flush();//刷新缓冲区
                is.close();
                fos.close();
                reslut = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            reslut =  false;
        }
        return  reslut;
    }
}
