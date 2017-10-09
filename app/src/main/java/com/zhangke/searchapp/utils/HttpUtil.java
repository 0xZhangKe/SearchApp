package com.zhangke.searchapp.utils;

import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.zhangke.searchapp.R;
import com.zhangke.searchapp.SearchAppApplication;

/**
 * Created by 张可 on 2017/10/9.
 */
public class HttpUtil {

    /**
     * 使用GET方式交换数据
     *
     * @param url
     * @param onDataCallbackListener
     */
    public static void getRequest(String url,
                                  final OnDataCallbackListener onDataCallbackListener) {
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                onDataCallbackListener.onSuccess(s);
            }
        }, new HttpErrorListener(onDataCallbackListener));
        SearchAppApplication.getInstance().addToRequestQueue(request);
    }

    /**
     * 网络访问的监听器
     */
    public interface OnDataCallbackListener {
        void onSuccess(String data);

        void onError(String error);
    }

    /**
     * 所有网络访问出错时都使用相同的行为
     */
    public static class HttpErrorListener implements Response.ErrorListener {

        private OnDataCallbackListener onDataCallbackListener;

        public HttpErrorListener(OnDataCallbackListener onDataCallbackListener) {
            this.onDataCallbackListener = onDataCallbackListener;
        }

        @Override
        public void onErrorResponse(VolleyError volleyError) {
            if (volleyError instanceof TimeoutError) {
                onDataCallbackListener.onError(SearchAppApplication.getInstance().getResources().getString(R.string.time_out_error));
            } else {
                if (null != volleyError.networkResponse) {
                    onDataCallbackListener
                            .onError(SearchAppApplication.getInstance().getResources().getString(R.string.internet_error) + "-" +
                                    "code:" + volleyError.networkResponse.statusCode);
                } else {
                    onDataCallbackListener.onError(SearchAppApplication.getInstance().getResources().getString(R.string.internet_error));
                }
            }
        }
    }
}
