package com.zhangke.searchapp.model;

import android.graphics.drawable.Drawable;

/**
 * Created by ZhangKe on 2017/10/8.
 */

public class AppInfo  implements Comparable<AppInfo>{
    public String appName; // 应用名
    public String packageName; // 包名
    public String versionName; // 版本名
    public int versionCode = 0; // 版本号
    public Drawable appIcon = null; // 应用图标
    public String sortTarget;

    public int pid;
    public int uid;
    public String processName;


    @Override
    public String toString() {
        return appName + " , " + packageName + " ," + versionName + " ," + versionCode;
    }

    public int compareTo(AppInfo arg0) {
        return this.sortTarget.compareTo(arg0.sortTarget);
    }
}
