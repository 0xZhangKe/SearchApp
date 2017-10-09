package com.zhangke.searchapp;

import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by 张可 on 2017/10/9.
 */

public class SearchAppApplication extends android.app.Application {

    private static final String TAG = "SearchAppApplication";

    private static SearchAppApplication application;
    private RequestQueue mRequestQueue;


    @Override
    public void onCreate() {
        super.onCreate();
        application = this;

        mRequestQueue = Volley.newRequestQueue(getApplicationContext());

    }

    public static SearchAppApplication getInstance() {
        return application;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> request, String tag) {
        request.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(request);
    }

    public <T> void addToRequestQueue(Request<T> request) {
        request.setTag(TAG);
        getRequestQueue().add(request);
    }

    public void cancelPendingRequest(Object tag) {
        getRequestQueue().cancelAll(tag);
    }
}
