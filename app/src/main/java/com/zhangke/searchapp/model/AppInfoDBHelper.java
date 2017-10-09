package com.zhangke.searchapp.model;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 张可 on 2017/10/9.
 */

public class AppInfoDBHelper extends SQLiteOpenHelper implements IAppInfoDB{

    public AppInfoDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table if not exists " + APP_LIST_TABLE_NAME +
                " (" + PRIMARY_KEY + " integer primary key autoincrement, " +
                APP_NAME + " text, " +
                PACKAGE_NAME + " text, " +
                VERSION_CODE + " text, " +
                VERSION_NAME + " text)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS " + APP_LIST_TABLE_NAME;
        db.execSQL(sql);
        onCreate(db);
    }
}
