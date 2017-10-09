package com.zhangke.searchapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.zhangke.searchapp.model.AppInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 张可 on 2017/10/9.
 */

public class AppClickPresenter  {

    private Activity activity;

    private AlertDialog alertDialog;

    public AppClickPresenter(Activity activity) {
        this.activity = activity;
    }

    public void onAppClick(final AppInfo appInfo){

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        ListView lv = new ListView(activity);
        lv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        builder.setView(lv);
        final List<String> data = new ArrayList<>();
        data.add("打开 " + appInfo.appName);
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
                switch(position){
                    case 0: {
                        ApplicationInfoUtil.doStartApplicationWithPackageName(activity, appInfo.packageName);
                        break;
                    }
                    case 1: {
                        Uri packageURI = Uri.parse("package:" + appInfo.packageName);
                        Intent intent =  new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,packageURI);
                        activity.startActivity(intent);
                        break;
                    }
                }
            }
        });
        alertDialog = builder.create();
        alertDialog.show();
    }

}
