package com.jrm.plugindemo.utils;

import android.os.Environment;

/**
 *
 * @author jiangrenming
 * @date 2018/1/26
 */

public class Constats {
    public  static final  String oldPath = "app-release.apk";
    public  static String  newPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+oldPath;
}
