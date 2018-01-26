package com.jrm.plugindemo.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.jrm.plugindemo.AndroidApplication;
import com.jrm.plugindemo.R;
import com.jrm.plugindemo.ams.AmsHookHelper;
import com.jrm.plugindemo.ams.DexClassLoaderPlugin;
import com.jrm.plugindemo.utils.AssentsCopyToSdCard;
import com.jrm.plugindemo.utils.Constats;

import java.io.File;
import java.util.Map;

import dalvik.system.DexClassLoader;

/**
 * @author jiangrenming
 */
public class MainActivity extends Activity {

    static final String ACTION = "com.weishu.upf.demo.app2.PLUGIN_ACTION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_skip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.jrm.plugin", "com.jrm.plugin.StartActivity"));
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        //广播插件的使用
        findViewById(R.id.receiver).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(new Intent("com.jrm.plugin.TestReceiver"));
            }
        });
        registerReceiver(mReceiver, new IntentFilter(ACTION));
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "插件插件,我是宿主,我已经接收你的请求了!", Toast.LENGTH_SHORT).show();
        }
    };

}
