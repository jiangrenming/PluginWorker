package com.jrm.plugindemo;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;
import com.jrm.plugindemo.activity.ProxyActivity;
import com.jrm.plugindemo.service.ProxyService;
import com.jrm.plugindemo.utils.AssentsCopyToSdCard;
import com.jrm.plugindemo.utils.Constats;
import com.jrm.pluginlibrary.PluginFactory;
import java.io.File;
import java.lang.reflect.Method;

/**
 *
 * @author jiangrenming
 * @date 2018/1/24
 */

public class AndroidApplication extends Application{

    public  static Context  mContext;
    private AssetManager assetManager;
    private Resources newResource;
    private Resources.Theme mTheme;
    private  boolean result;



    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
         mContext = base;
        //将apk文件从assents复制到sd卡中
         result = AssentsCopyToSdCard.copyFilesFassets(base, Constats.oldPath,Constats.newPath);
        if (result){
            new MyThread().start();
        }
    }

    /**
     * 在创建自己的资源文件(资源的合并)之前先要替换资源（插件中的资源文件）
     */
    private void _initData() {
        try{
            assetManager = AssetManager.class.newInstance();
            Method addAssetPathMethod = assetManager.getClass().getDeclaredMethod("addAssetPath", String.class);
            addAssetPathMethod.setAccessible(true);
            addAssetPathMethod.invoke(assetManager, Constats.newPath);
            Method ensureStringBlocks = AssetManager.class.getDeclaredMethod("ensureStringBlocks");
            ensureStringBlocks.setAccessible(true);
            ensureStringBlocks.invoke(assetManager);
            Resources supResource = getResources();
            Log.e("Main", "supResource = " + supResource);
            newResource = new Resources(assetManager, supResource.getDisplayMetrics(), supResource.getConfiguration());
            Log.e("Main", "设置 getResource = " + getResources());
            mTheme = newResource.newTheme();
            mTheme.setTo(super.getTheme());
        }catch (Exception e){
            e.printStackTrace();
            Log.e("Main", "走了我的callActivityOnCreate 错了 = " + e.getMessage());
        }
    }
    @Override
    public AssetManager getAssets() {
        return assetManager == null ? super.getAssets() : assetManager;
    }

    @Override
    public Resources getResources() {
        return newResource == null ? super.getResources() : newResource;
    }

    @Override
    public Resources.Theme getTheme() {
        return mTheme == null ? super.getTheme() : mTheme;
    }
    public static Context getContext() {
        return mContext;
    }

    /**
     * 加载插件apk的信息（资源，布局，方法类等）
     * 利用DexClassLoader加载其他的dex文件来加载拼接宿主apk的长度+插件的长度
     * 获取ams代理对象并替换代理对象，拦截消息找回本要启动的真正对象再发送消息
     */
    private class MyThread extends Thread{
        @Override
        public void run() {
            try{
                if (result){
                   _initData();
                    PluginFactory build = new PluginFactory.Builder()
                            .setContext(mContext)  //上下文
                            .setPath(Constats.newPath)  //apk存储的位置，这里存在sd卡里
                            .setPluginPackageName(mContext.getPackageName())   //如果在插件里，就输入插件里的包名 ，反之为宿主包名
                            .setProxyActivity(ProxyActivity.class.getName())   //替身的activity
                            .setServiceName(ProxyService.class.getName())      //替身service
                            .build();
                    build.setConfigParams();  //开始加载插件
                    mHandler.sendEmptyMessage(0x01);
                }
            }catch (Exception e){
                e.printStackTrace();
                Log.i("插件加载失败",e.getMessage());
            }
        }
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Toast.makeText(mContext,"插件加载完成",Toast.LENGTH_SHORT).show();
        }
    };

}
