package com.zhangke.searchapp.Running;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.zhangke.searchapp.model.AppInfo;
import com.zhangke.searchapp.utils.ApplicationInfoUtil;
import com.zhangke.searchapp.utils.UiUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 张可 on 2017/10/16.
 */

public class ClearClickPresenter implements ClearClickContract.Presenter {

    private ClearClickContract.View view;
    private Activity activity;

    private AlertDialog alertDialog;

    private ActivityManager activityManager;

    public ClearClickPresenter(ClearClickContract.View view, Activity activity) {
        this.view = view;
        this.activity = activity;

        activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
    }

    @Override
    public void onAppClick(final AppInfo appInfo) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        ListView lv = new ListView(activity);
        lv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        builder.setView(lv);
        final List<String> data = new ArrayList<>();
        data.add("打开 " + appInfo.appName);
        data.add("关闭该进程");
        data.add("查看应用信息");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                activity,
                android.R.layout.simple_list_item_1,
                data);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                alertDialog.dismiss();
                switch (position) {
                    case 0: {
                        ApplicationInfoUtil.doStartApplicationWithPackageName(activity, appInfo.packageName);
                        break;
                    }
                    case 1: {
                        if (appInfo.pid != 0) {
                            try {
                                //http://www.cnblogs.com/crazypebble/archive/2011/04/09/2010196.html
//                                Runtime.getRuntime().exec("am force-stop " + appInfo.packageName);
//                                activityManager.killBackgroundProcesses(appInfo.packageName);

                                if(ApplicationInfoUtil.isRoot()){
                                    UiUtils.showToast(activity, ApplicationInfoUtil.execCommand("kill -9 " + appInfo.pid).toString());
                                }else{
                                    showRootDialog();
                                }
                                ClearClickPresenter.this.view.refresh();
                            } catch (Exception e) {
                                UiUtils.showToast(activity, "error: " + e.getMessage());
                            }
                        }
                        break;
                    }
                    case 2: {
                        Uri packageURI = Uri.parse("package:" + appInfo.packageName);
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                        activity.startActivity(intent);
                        break;
                    }
                }
            }
        });
        alertDialog = builder.create();
        alertDialog.show();
    }

    private void showRootDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("获取 root 权限可增强清理效果，是否提升至 root 权限？");
        builder.setNegativeButton("取消", null);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (ApplicationInfoUtil.RootCommand("chmod 777 " + activity.getPackageCodePath())) {
                    UiUtils.showToast(activity, "root 成功");
                } else {
                    UiUtils.showToast(activity, "失败");
                }
            }
        });
        builder.create().show();
    }
}
