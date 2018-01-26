package com.jrm.plugindemo.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import com.jrm.plugindemo.R;
import com.jrm.plugindemo.utils.AssentsCopyToSdCard;

/**
 *
 * @author jiangrenming
 * @date 2018/1/25
 */

public class WelcomeActivity extends AppCompatActivity {

    private static final int BAI_DU_READ_PHONE_STATE = 100;
    private static final  String oldPath = "app-release.apk";
    private String newPath ;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_layout);
        getPermission();
    }

    private void getPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                    ) {
                // 申请一个（或多个）权限，并提供用于回调返回的获取码（用户定义）
                ActivityCompat.requestPermissions(WelcomeActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},BAI_DU_READ_PHONE_STATE);
            }
        }else {
            new MyThread().start();
            startActivity(new Intent(WelcomeActivity.this,MainActivity.class));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            // requestCode即所声明的权限获取码，在checkSelfPermission时传入
            case BAI_DU_READ_PHONE_STATE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获取到权限，作相应处理（调用定位SDK应当确保相关权限均被授权，否则可能引起定位失败）
                    Toast.makeText(getApplicationContext(), "获取读写操作允许", Toast.LENGTH_SHORT).show();
                    new MyThread().start();
                    startActivity(new Intent(WelcomeActivity.this,MainActivity.class));
                } else {
                    // 没有获取到权限，做特殊处理
                    Toast.makeText(getApplicationContext(), "获取读写操作权限失败，请手动开启", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 加载插件apk的信息（资源，布局，方法类等）
     * 利用DexClassLoader加载其他的dex文件来加载拼接宿主apk的长度+插件的长度
     * 获取ams代理对象并替换代理对象，拦截消息找回本要启动的真正对象再发送消息
     */
    private class MyThread extends Thread{
        @Override
        public void run() {
            super.run();
            try{
                newPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+oldPath;
                boolean result = AssentsCopyToSdCard.copyFilesFassets(WelcomeActivity.this, oldPath, newPath);
                if (result){
                    //宿主应用的路径
                   /* String cachePath = WelcomeActivity.this.getCacheDir().getAbsolutePath();
                    //利用DexClassLoader来加载插件
                    DexClassLoader dexClassLoader = new DexClassLoader(newPath,cachePath,cachePath,getClassLoader());
                    DexClassLoaderPlugin.inject(dexClassLoader);
                    //获取ams代理对象，并给代理对象找替身重新赋值
                    AmsHookHelper.insteadOfAmsNativeActivity();
                    //将原有的替身给更换回来
                    AmsHookHelper.changeRealPluginActivity();
                    //加载资源文件
                    //              AmsHookHelper.hookActivityResource(AndroidApplication.getContext());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(WelcomeActivity.this,"加载插件成功",Toast.LENGTH_SHORT).show();
                            //startActivity(new Intent(WelcomeActivity.this,MainActivity.class));
                        }
                    });*/
                }
            }catch (Exception e){
                e.printStackTrace();
                Log.i("插件加载失败",e.getMessage());
            }
        }
    }
}
