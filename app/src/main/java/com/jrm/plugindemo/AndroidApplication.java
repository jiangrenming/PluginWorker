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

import com.jrm.plugindemo.activity.MainActivity;
import com.jrm.plugindemo.ams.AmsHookHelper;
import com.jrm.plugindemo.ams.DexClassLoaderPlugin;
import com.jrm.plugindemo.utils.AssentsCopyToSdCard;
import com.jrm.plugindemo.utils.Constats;

import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

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
       _initData();
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
                    //加载资源文件
                    AmsHookHelper.hookActivityResource(mContext);
                    //宿主应用的路径
                    String cachePath = getCacheDir().getAbsolutePath();
                    //利用DexClassLoader来加载插件
                    DexClassLoader dexClassLoader = new DexClassLoader(Constats.newPath,cachePath,cachePath,getClassLoader());
                    DexClassLoaderPlugin.inject(dexClassLoader);
                    //获取ams代理对象，并给代理对象找替身重新赋值
                    AmsHookHelper.insteadOfAmsNativeActivity();
                    //将原有的替身给更换回来
                    AmsHookHelper.changeRealPluginActivity();
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
