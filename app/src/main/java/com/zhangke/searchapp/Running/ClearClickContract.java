package com.zhangke.searchapp.Running;

import android.app.Activity;

import com.zhangke.searchapp.model.AppInfo;

/**
 * Created by 张可 on 2017/10/16.
 */

public interface ClearClickContract {

    interface Presenter{
        void onAppClick(final AppInfo appInfo);
    }

    interface View{
        void refresh();
    }
}
