package com.jrm.pluginlibrary.ams;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.Field;


/**
 *
 * @author jiangrenming
 * @date 2018/1/24
 */

public class ActivityThreadHandlerCallback implements Handler.Callback {



    private Handler mBase;
    public  ActivityThreadHandlerCallback(Handler mh){
        this.mBase = mh;
    }
    @Override
    public boolean handleMessage(Message msg) {
        Log.e("Main","handleMessage what = " + msg.what);
        switch (msg.what) {
            // ActivityThread里面 "LAUNCH_ACTIVITY" 这个字段的值是100
            // 本来使用反射的方式获取最好, 这里为了简便直接使用硬编码
            case 100:
                handleLaunchActivity(msg);
                break;
            default:
                break;
        }

        mBase.handleMessage(msg);
        return true;
    }

    /**
     * 恢复启动的真身
     * @param msg
     */
    private void handleLaunchActivity(Message msg) {
        // 这里简单起见,直接取出TargetActivity;
        Log.e("Main","handleLaunchActivity方法 拦截");
        Object obj = msg.obj;
        try {
            // 把替身恢复成真身
            Field intent = obj.getClass().getDeclaredField("intent");
            intent.setAccessible(true);
            Intent raw = (Intent) intent.get(obj);
            Intent target = raw.getParcelableExtra(AmsHookHelper.EXTRA_TARGET_INTENT);
            raw.setComponent(target.getComponent());
            Log.e("Main","target = " + target);

        } catch (Exception e) {
            throw new RuntimeException("hook launch activity failed", e);
        }

    }
}
