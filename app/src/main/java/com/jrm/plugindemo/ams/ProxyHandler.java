package com.jrm.plugindemo.ams;

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import com.jrm.plugindemo.AndroidApplication;
import com.jrm.plugindemo.activity.ProxyActivity;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 *
 * @author jiangrenming
 * @date 2018/1/24
 * 消息的拦截,存储目标对象
 */

public class ProxyHandler implements InvocationHandler {

    private  Object mBase;
    public ProxyHandler(Object base){
        this.mBase = base;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("startActivity".equals(method.getName())) {
            Log.i("Main","startActivity方法拦截了");
            // 找到参数里面的第一个Intent 对象
            Intent raw;
            int index = 0;
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Intent) {
                    index = i;
                    break;
                }
            }
            raw = (Intent) args[index];
            //创建一个要被掉包的Intent
            Intent newIntent = new Intent();
            // 如果再插件里,不同的插件有不同的包  传递插件的包名即可
            String stubPackage = AndroidApplication.getContext().getPackageName();
            // 这里我们把启动的Activity临时替换为
            ComponentName componentName = new ComponentName(stubPackage, ProxyActivity.class.getName());
            newIntent.setComponent(componentName);
            // 把我们原始要启动的TargetActivity先存起来
            newIntent.putExtra(AmsHookHelper.EXTRA_TARGET_INTENT, raw);
            // 替换掉Intent, 达到欺骗AMS的目的
            args[index] = newIntent;
            Log.e("Main","args[index] hook = " + args[index]);
            return method.invoke(mBase, args);
        }
        return method.invoke(mBase, args);
    }
}
