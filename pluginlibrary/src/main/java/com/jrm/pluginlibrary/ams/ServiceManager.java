package com.jrm.pluginlibrary.ams;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import com.jrm.pluginlibrary.PluginFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jiangrenming
 * @date 2018/1/27
 * 管理插件service的所有操作
 */

public class ServiceManager {


    private Map<String, Service> mServiceMap = new HashMap<String, Service>();
    private static volatile ServiceManager sInstance;
    public synchronized static ServiceManager getInstance() {
        if (sInstance == null) {
            sInstance = new ServiceManager();
        }
        return sInstance;
    }

    /**
     * 启动某个插件Service; 如果Service还没有启动, 那么会创建新的插件Service
     * @param proxyIntent
     * @param startId
     */
    public void onStart(Intent proxyIntent, int startId) {
        Intent targetIntent = proxyIntent.getParcelableExtra(AmsHookHelper.EXTRA_TARGET_INTENT_SERVICE);
        Log.i("service的长度",targetIntent+"");
        ServiceInfo serviceInfo = selectPluginService(targetIntent);
        if (serviceInfo == null) {
            Log.w("启动插件service", "can not found service : " + targetIntent.getComponent());
            return;
        }
        try {
            if (!mServiceMap.containsKey(serviceInfo.name)) {
                // service还不存在, 先创建
                proxyCreateService(serviceInfo);
            }
            Service service = mServiceMap.get(serviceInfo.name);
            service.onStart(targetIntent, startId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止某个插件Service, 当全部的插件Service都停止之后, ProxyService也会停止
     * @param targetIntent
     * @return
     */
    public int stopService(Intent targetIntent) {
        ServiceInfo serviceInfo = selectPluginService(targetIntent);
        if (serviceInfo == null) {
            Log.w("停止插件service", "can not found service: " + targetIntent.getComponent());
            return 0;
        }
        Service service = mServiceMap.get(serviceInfo.name);
        if (service == null) {
            Log.w("停止插件service", "can not runnning, are you stopped it multi-times?");
            return 0;
        }
        service.onDestroy();
        mServiceMap.remove(serviceInfo.name);
        if (mServiceMap.isEmpty()) {
            // 没有Service了, 这个没有必要存在了
            Log.d("停止插件service", "service all stopped, stop proxy");
            PluginFactory.getmContext().stopService(new Intent().setComponent(new ComponentName(PluginFactory.getPackageName(),PluginFactory.getClsServiceName())));
        }
        return 1;
    }
    /**
     * 通过ActivityThread的handleCreateService方法创建出Service对象
     * @param serviceInfo 插件的ServiceInfo
     * @throws Exception
     */
    private void proxyCreateService(ServiceInfo serviceInfo) throws  Exception{

        IBinder token = new Binder();
        // 创建CreateServiceData对象, 用来传递给ActivityThread的handleCreateService 当作参数
        Class<?> createServiceDataClass = Class.forName("android.app.ActivityThread$CreateServiceData");
        Constructor<?> constructor  = createServiceDataClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object createServiceData = constructor.newInstance();
        // 写入我们创建的createServiceData的token字段, ActivityThread的handleCreateService用这个作为key存储Service
        Field tokenField = createServiceDataClass.getDeclaredField("token");
        tokenField.setAccessible(true);
        tokenField.set(createServiceData, token);
        // 写入info对象
        // 这个修改是为了loadClass的时候, LoadedApk会是主程序的ClassLoader, 我们选择Hook BaseDexClassLoader的方式加载插件
        serviceInfo.applicationInfo.packageName = PluginFactory.getPackageName();
        Field infoField = createServiceDataClass.getDeclaredField("info");
        infoField.setAccessible(true);
        infoField.set(createServiceData, serviceInfo);

        // 写入compatInfo字段
        // 获取默认的compatibility配置
        Class<?> compatibilityClass = Class.forName("android.content.res.CompatibilityInfo");
        Field defaultCompatibilityField = compatibilityClass.getDeclaredField("DEFAULT_COMPATIBILITY_INFO");
        Object defaultCompatibility = defaultCompatibilityField.get(null);
        Field compatInfoField = createServiceDataClass.getDeclaredField("compatInfo");
        compatInfoField.setAccessible(true);
        compatInfoField.set(createServiceData, defaultCompatibility);
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
        Object currentActivityThread = currentActivityThreadMethod.invoke(null);
        Method handleCreateServiceMethod = activityThreadClass.getDeclaredMethod("handleCreateService", createServiceDataClass);
        handleCreateServiceMethod.setAccessible(true);
        handleCreateServiceMethod.invoke(currentActivityThread, createServiceData);
        // handleCreateService创建出来的Service对象并没有返回, 而是存储在ActivityThread的mServices字段里面, 这里我们手动把它取出来
        Field mServicesField = activityThreadClass.getDeclaredField("mServices");
        mServicesField.setAccessible(true);
        Map mServices = (Map) mServicesField.get(currentActivityThread);
        Service service = (Service) mServices.get(token);
        // 获取到之后, 移除这个service, 我们只是借花献佛
        mServices.remove(token);
        // 将此Service存储起来
        mServiceMap.put(serviceInfo.name, service);
    }

    /**
     * 选择匹配的ServiceInfo
     * @param pluginIntent 插件的Intent
     * @return
     */
    private ServiceInfo selectPluginService(Intent pluginIntent) {
        Log.i("service的长度",AmsHookHelper.mServiceInfoMap.size()+"");
        for (ComponentName componentName : AmsHookHelper.mServiceInfoMap.keySet()) {
            if (componentName.equals(pluginIntent.getComponent())) {
                return AmsHookHelper.mServiceInfoMap.get(componentName);
            }
        }
        return null;
    }
}
