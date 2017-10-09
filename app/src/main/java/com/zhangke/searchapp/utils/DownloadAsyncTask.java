package com.zhangke.searchapp.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.BuildConfig;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by 张可 on 2017/10/9.
 */

public class DownloadAsyncTask  extends AsyncTask<Integer, Integer, String> {

    private Activity activity;
    private String downloadUrl;
    private String filePath;
    private ProgressDialog progressDialog;

    public DownloadAsyncTask(Activity activity,
                             String downloadUrl,
                             String filePath,
                             ProgressDialog progressDialog) {
        this.activity = activity;
        this.downloadUrl = downloadUrl;
        this.filePath = filePath;
        this.progressDialog = progressDialog;
    }

    /**
     * 这里的Integer参数对应AsyncTask中的第一个参数
     * 这里的String返回值对应AsyncTask的第三个参数
     * 该方法并不运行在UI线程当中，主要用于异步操作，所有在该方法中不能对UI当中的空间进行设置和修改
     * 但是可以调用publishProgress方法触发onProgressUpdate对UI进行操作
     */
    @Override
    protected String doInBackground(Integer... params) {
        File f = new File(filePath);
        if (f.exists()) {
            f.delete();
        }
        InputStream is = null;
        OutputStream os = null;
        try {
            URL url = new URL(downloadUrl);
            URLConnection con = url.openConnection();
            //获得文件的长度
            double contentLength = con.getContentLength();

            // 输入流
            is = con.getInputStream();
            // 1K的数据缓冲
            byte[] bs = new byte[1024];
            // 读取到的数据长度
            int len;
            // 输出的文件流
            os = new FileOutputStream(filePath);
            // 开始读取
            double totalLength = 0;
            int progress = 0;
            while ((len = is.read(bs)) != -1) {
                os.write(bs, 0, len);
                totalLength += len;
                if (progress != (int) (totalLength / contentLength * 100)) {
                    progress = (int) (totalLength / contentLength * 100);
                    publishProgress(progress);
                }
            }
            // 完毕，关闭所有链接
            os.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 这里的String参数对应AsyncTask中的第三个参数（也就是接收doInBackground的返回值）
     * 在doInBackground方法执行结束之后在运行，并且运行在UI线程当中 可以对UI控件进行设置
     */
    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        progressDialog.cancel();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //判断是否是AndroidN以及更高的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".fileProvider", new File(filePath));
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(new File(filePath)), "application/vnd.android.package-archive");
        }
        activity.startActivity(intent);
        activity.finish();
    }

    //该方法运行在UI线程当中,并且运行在UI线程当中 可以对UI空间进行设置
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.show();
    }

    /**
     * 这里的Intege参数对应AsyncTask中的第二个参数
     * 在doInBackground方法当中，每次调用publishProgress方法都会触发onProgressUpdate执行
     * onProgressUpdate是在UI线程中执行，所有可以对UI空间进行操作
     */
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        progressDialog.setProgress(values[0]);
    }
}
