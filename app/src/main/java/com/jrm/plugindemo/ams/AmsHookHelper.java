package com.jrm.plugindemo.ams;

import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 *
 * @author jiangrenming
 * @date 2018/1/24
 */

public class AmsHookHelper {


    public static  final String EXTRA_TARGET_INTENT = "extra_target_intent";


    /**
     * ams获取代理对象并替换真身
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     */
    public static void insteadOfAmsNativeActivity() throws ClassNotFoundException,
            NoSuchMethodException, InvocationTargetException,IllegalAccessException, NoSuchFieldException{
       /* Field gDefaultField =null;
        if (Build.VERSION.SDK_INT >= 26) {
            Class<?> activityManager = Class.forName("android.app.ActivityManager");
            gDefaultField = activityManager.getDeclaredField("IActivityManagerSingleton");
        }else{
            Class<?> activityManagerNativeClass = Class.forName("android.app.ActivityManagerNative");
            gDefaultField = activityManagerNativeClass.getDeclaredField("gDefault");
        }

        gDefaultField.setAccessible(true);
        //从gDefault字段中取出这个对象的值
        Object gDefault = gDefaultField.get(null);
        // gDefault是一个 android.util.Singleton对象; 我们取出这个单例里面的字段
        Class<?> singleton = Class.forName("android.util.Singleton");
        //这个gDefault是一个Singleton类型的，我们需要从Singleton中再取出这个单例的AMS代理
        Field mInstanceField = singleton.getDeclaredField("mInstance");
        mInstanceField.setAccessible(true);
        //ams的代理对象
        Object rawIActivityManager = mInstanceField.get(gDefault);
        // 创建一个这个对象的代理对象, 然后替换这个字段, 让我们的代理对象帮忙干活,这里我们使用动态代理
        Class<?> iActivityManagerInterface = Class.forName("android.app.IActivityManager");
        Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { iActivityManagerInterface }, new ProxyHandler(rawIActivityManager));
        mInstanceField.set(gDefault, proxy);*/

        //获取ActivityManagerNative的类
        //拿到gDefault字段
        Field gDefaultField =null;
        if (Build.VERSION.SDK_INT >= 26) {
            Class<?> activityManager = Class.forName("android.app.ActivityManager");
            gDefaultField = activityManager.getDeclaredField("IActivityManagerSingleton");
        }else{
            Class<?> activityManagerNativeClass = Class.forName("android.app.ActivityManagerNative");
            gDefaultField = activityManagerNativeClass.getDeclaredField("gDefault");
        }
        gDefaultField.setAccessible(true);
        //从gDefault字段中取出这个对象的值
        Object gDefault = gDefaultField.get(null);
        // gDefault是一个 android.util.Singleton对象; 我们取出这个单例里面的字段
        Class<?> singleton = Class.forName("android.util.Singleton");

        //这个gDefault是一个Singleton类型的，我们需要从Singleton中再取出这个单例的AMS代理
        Field mInstanceField = singleton.getDeclaredField("mInstance");
        mInstanceField.setAccessible(true);
        //ams的代理对象
        Object rawIActivityManager = mInstanceField.get(gDefault);

        // 创建一个这个对象的代理对象, 然后替换这个字段, 让我们的代理对象帮忙干活,这里我们使用动态代理
        Class<?> iActivityManagerInterface = Class.forName("android.app.IActivityManager");
        Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { iActivityManagerInterface }, new ProxyHandler(rawIActivityManager));
        mInstanceField.set(gDefault, proxy);

    }

    /**
     * 将替身给换回真身并启动起来，同时发送利用mH发送消息
     * @throws Exception
     */
    public  static  void changeRealPluginActivity()throws Exception {
        // 先获取到当前的ActivityThread对象
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        //他有一个方法返回了自己
        Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
        currentActivityThreadMethod.setAccessible(true);
        //执行方法得到ActivityThread对象
        Object currentActivityThread = currentActivityThreadMethod.invoke(null);
        // 由于ActivityThread一个进程只有一个,我们获取这个对象的mH
        Field mHField = activityThreadClass.getDeclaredField("mH");
        mHField.setAccessible(true);
        //得到H这个Handler
        Handler mH = (Handler) mHField.get(currentActivityThread);
        Field mCallBackField = Handler.class.getDeclaredField("mCallback");
        mCallBackField.setAccessible(true);
        //设置我们自己的CallBackField
        mCallBackField.set(mH, new ActivityThreadHandlerCallback(mH));
    }

    /**
     * hookActivity的Resource，如果想马上生效需要针对使用Resource的组件hook
     * @param context
     */
    public static void hookActivityResource(Context context) throws  Exception{
        try {
            //获取ActiivtiyThread类
            Class<?> mActivityThreadClass = Class.forName("android.app.ActivityThread");
            //获取当前ActivityThread
            Method currentActivityThread = mActivityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThread.setAccessible(true);
            Object mCurrentActivityThread = currentActivityThread.invoke(null);
            //获取mInstrumentation字段
            Field mInstrumentationField = mActivityThreadClass.getDeclaredField("mInstrumentation");
            mInstrumentationField.setAccessible(true);
            Instrumentation baseInstrumentation = (Instrumentation) mInstrumentationField.get(mCurrentActivityThread);
            //设置我们自己的mInstrumentation
            Instrumentation proxy = new RecourceInstrumentation(baseInstrumentation,context);
            //替换
            mInstrumentationField.set(mCurrentActivityThread,proxy);
            Log.e("Main","替换Resource成功");
        } catch (Exception e) {
            Log.e("Main","替换Resource失败 = " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 修改hook代理PackageManagerService
     * @param context
     */
    public  static  void hookPMS(Context context){
        try{
            //获取全局的ActivityThread对象
            Class<?> aClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThread = aClass.getDeclaredMethod("currentActivityThread");
            currentActivityThread.setAccessible(true);
            Object currentThread = currentActivityThread.invoke(null);
            //获取ActivityThread里面原始的 sPackageManager
            Field sPackageManager = aClass.getDeclaredField("sPackageManager");
            sPackageManager.setAccessible(true);
            Object pkmObject = sPackageManager.get(currentThread);
            //准备代理对象,替换原始对象
            Class<?> packageManangerInterface = Class.forName("android.content.pm.IPackageManager");
            Object newProxyInstance = Proxy.newProxyInstance(packageManangerInterface.getClassLoader(), new Class<?>[]{packageManangerInterface}, new ProxyPMSHandler(pkmObject));
            // 1. 替换掉ActivityThread里面的 sPackageManager 字段
            sPackageManager.set(currentThread,newProxyInstance);
            // 2. 替换 ApplicationPackageManager里面的 mPM对象
            PackageManager packageManager = context.getPackageManager();
            Field mPMField = packageManager.getClass().getDeclaredField("mPM");
            mPMField.setAccessible(true);
            mPMField.set(packageManager,newProxyInstance);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
