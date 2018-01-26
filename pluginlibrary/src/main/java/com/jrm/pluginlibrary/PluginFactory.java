package com.jrm.pluginlibrary;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import com.jrm.pluginlibrary.ams.AmsHookHelper;
import com.jrm.pluginlibrary.ams.DexClassLoaderPlugin;

import java.io.File;
import java.lang.reflect.Method;
import java.security.cert.X509Certificate;

import dalvik.system.DexClassLoader;

/**
 *
 * @author jiangrenming
 * @date 2018/1/26
 */

public class PluginFactory {

    private  static String clsName;
    private static  String pkgeName;
    private  static Context mContext;
    private  static   String path;

    private  PluginFactory () throws  Exception{
    }

    /**
     * 构建器
     */
    public static class Builder {
        private PluginFactory config;
        public Builder() throws Exception{
            config = new PluginFactory();
        }
        public PluginFactory build() {
            return config;
        }

        public Builder setProxyActivity(String className) {
            clsName = className;
            return this;
        }
        public Builder setPluginPackageName(String packageName) {
            pkgeName = packageName;
            return this;
        }

        public Builder setContext(Context context) {
            mContext = context;
            return this;
        }
        public Builder setPath(String apkPath) {
            path = apkPath;
            return this;
        }
    }
   public  void setConfigParams() throws Exception{
       //加载资源文件
       AmsHookHelper.hookActivityResource();
       //宿主应用的路径
       String cachePath = mContext.getCacheDir().getAbsolutePath();
       //利用DexClassLoader来加载插件
       DexClassLoader dexClassLoader = new DexClassLoader(path,cachePath,cachePath,mContext.getClassLoader());
       DexClassLoaderPlugin.inject(dexClassLoader);
       //获取ams代理对象，并给代理对象找替身重新赋值
       AmsHookHelper.insteadOfAmsNativeActivity();
       //将原有的替身给更换回来
       AmsHookHelper.changeRealPluginActivity();
       //加载广播
       AmsHookHelper.preLoadReceiver(new File(path),dexClassLoader);
   }

    public static  String getPackageName() {
        return pkgeName;
    }

    public static String getClassName() {
        return clsName;
    }

    public static Context getmContext() {
        return mContext;
    }

    public static String getPath() {
        return path;
    }
}
