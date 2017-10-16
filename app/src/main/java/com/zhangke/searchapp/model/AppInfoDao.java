package com.zhangke.searchapp.model;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;

import com.zhangke.searchapp.utils.HanziToPinyin;

import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 张可 on 2017/10/9.
 */

public class AppInfoDao implements IAppInfoDB {

    private static final String TAG = "AppInfoDao";

    private Context context;
    private AppInfoDBHelper appInfoDBHelper;

    private PackageManager packageManager;

    public AppInfoDao(Context context) {
        this.context = context;
        appInfoDBHelper = new AppInfoDBHelper(context);

        packageManager = context.getPackageManager();
    }

    public void insertAppList(List<AppInfo> appList) {
        SQLiteDatabase db = appInfoDBHelper.getWritableDatabase();
        try {
            for (AppInfo entity : appList) {
                db.execSQL("INSERT INTO " + APP_LIST_TABLE_NAME +
                        " (" + APP_NAME + ", " + PIN_YIN + ", " + PACKAGE_NAME + ", " + VERSION_CODE + ", " + VERSION_NAME + ") VALUES " +
                        "('" + entity.appName + "', '" + entity.sortTarget + "', '" + entity.packageName + "', '" + entity.versionCode + "', '" + entity.versionName + "')");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (db.isOpen()) {
                db.close();
            }
        }
    }

    /**
     * 查询数据
     */
    public List<AppInfo> readAllData() {
        List<AppInfo> list = new ArrayList<>();
        SQLiteDatabase db = appInfoDBHelper.getWritableDatabase();
        try {
            Cursor cursor = db.rawQuery("SELECT " + APP_NAME + ", " + PIN_YIN + ", " + PACKAGE_NAME + ", " +
                    VERSION_CODE + ", " + VERSION_NAME + " FROM " + APP_LIST_TABLE_NAME, null);
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    AppInfo entity = parseNotify(cursor);
                    list.add(entity);
                }
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (db.isOpen()) {
                db.close();
            }
        }
        return list;
    }

    public void clearAppList() {
        SQLiteDatabase db = appInfoDBHelper.getWritableDatabase();
        try {
            Log.e(TAG, "deleted num:" + db.delete(APP_LIST_TABLE_NAME, null, null));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (db.isOpen()) {
                db.close();
            }
        }
    }

    private AppInfo parseNotify(Cursor cursor) {
        AppInfo entity = new AppInfo();
        try {
            entity.appName = cursor.getString(0);
            entity.sortTarget = cursor.getString(1);
            entity.packageName = cursor.getString(2);
            entity.versionCode = Integer.valueOf(cursor.getString(3));
            entity.versionName = cursor.getString(4);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            ApplicationInfo info = packageManager.getApplicationInfo(entity.packageName, PackageManager.GET_ACTIVITIES);
            entity.appIcon = info.loadIcon(packageManager);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if(TextUtils.isEmpty(entity.sortTarget)) {
            ArrayList<HanziToPinyin.Token> sort = HanziToPinyin.getInstance().get(entity.appName);
            if (sort == null || sort.isEmpty()) {
                entity.sortTarget = entity.appName;
            } else {
                StringBuilder sbSort = new StringBuilder();
                for (HanziToPinyin.Token token : sort) {
                    if (!TextUtils.isEmpty(token.target)) {
                        sbSort.append(token.target.substring(0, 1));
                    }
                }
                entity.sortTarget = sbSort.toString();
            }
        }
        return entity;
    }
}
