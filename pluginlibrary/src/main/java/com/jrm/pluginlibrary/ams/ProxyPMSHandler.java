package com.jrm.pluginlibrary.ams;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 *
 * @author jiangrenming
 * @date 2018/1/25
 * Pms的hook拦截替换功能的实现
 */

public class ProxyPMSHandler implements InvocationHandler {

    private Object mBase;
    public  ProxyPMSHandler(Object base){
        this.mBase = base;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return null;
    }
}
