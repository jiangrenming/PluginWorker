package com.jrm.plugindemo.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;

import com.jrm.pluginlibrary.ams.ServiceManager;

/**
 *
 * @author jiangrenming
 * @date 2018/1/27
 *   替身service，只做service的分发操作
 */

public class ProxyService extends Service{


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.d("TAG", "onStart() called with " + "intent = [" + intent + "], startId = [" + startId + "]");
        //service的分发操作
        ServiceManager.getInstance().onStart(intent, startId);
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
