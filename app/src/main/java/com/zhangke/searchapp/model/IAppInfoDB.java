package com.zhangke.searchapp.model;

/**
 * Created by 张可 on 2017/10/9.
 */

public interface IAppInfoDB {

    String DB_NAME = "AppListDB.db";//DB Name
    int DB_VERSION = 1;//数据库版本
    String APP_LIST_TABLE_NAME = "app_list";//table name
    String PRIMARY_KEY = "Id";//主键
    String APP_NAME = "appName";
    String PACKAGE_NAME = "packageName";
    String VERSION_NAME = "versionName";
    String VERSION_CODE = "versionCode";


}
