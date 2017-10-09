package com.zhangke.searchapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zhangke.searchapp.model.AppInfo;
import com.zhangke.searchapp.model.AppInfoDao;
import com.zhangke.searchapp.utils.DownloadAsyncTask;
import com.zhangke.searchapp.utils.HttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.tv_app_num)
    TextView tvAppNum;
    @BindView(R.id.img_show_type)
    ImageView imgShowType;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.progress)
    ProgressBar progress;

    public static List<AppInfo> appOriginList = new ArrayList<>();

    private List<AppInfo> listData = new ArrayList<>();
    private APPListAdapter adapter;

    private LinearLayoutManager linearLayoutManager;
    private GridLayoutManager gridLayoutManager;

    private boolean isSingleShow = true;
    private AppInfoDao appInfoDao;

    private ProgressDialog downloadDialog;
    private AlertDialog updateDialog;

    private AppClickPresenter appClickPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        toolbar.setTitle("SearchApp");
        setSupportActionBar(toolbar);

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        gridLayoutManager = new GridLayoutManager(this, 2);

        appClickPresenter = new AppClickPresenter(this);

        adapter = new APPListAdapter(MainActivity.this, listData);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new APPListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                appClickPresenter.onAppClick(listData.get(position));
            }
        });

//        recyclerView.setHasFixedSize(true);

        appInfoDao = new AppInfoDao(this);
        getAppList(true);

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAppList(false);
            }
        });

        downloadDialog = new ProgressDialog(this);
        downloadDialog.setCancelable(false);
        downloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        checkAppVersion();
    }

    private void getAppList(final boolean useCache) {
        if (appOriginList != null && !appOriginList.isEmpty()) {
            appOriginList.clear();
        }
        if (listData != null && !listData.isEmpty()) {
            listData.clear();
        }
        if (useCache) {
            progress.setVisibility(View.VISIBLE);
            appOriginList.addAll(appInfoDao.readAllData());
            listData.addAll(appOriginList);
            adapter.notifyDataSetChanged();
            tvAppNum.setText("APP 数：" + appOriginList.size());
        }else{
            swipeRefresh.setRefreshing(true);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (appOriginList != null && !appOriginList.isEmpty()) {
                    appOriginList.clear();
                }
                appOriginList.addAll(ApplicationInfoUtil.getAllNoSystemProgramInfo(MainActivity.this));
                if (listData != null && !listData.isEmpty()) {
                    listData.clear();
                }
                listData.addAll(appOriginList);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        tvAppNum.setText("APP 数：" + listData.size());
                        if (useCache) {
                            progress.setVisibility(View.GONE);
                        }else{
                            swipeRefresh.setRefreshing(false);
                        }
                    }
                });
                appInfoDao.clearAppList();
                appInfoDao.insertAppList(appOriginList);
            }
        }).start();
    }

    @OnClick(R.id.img_show_type)
    public void changeShowType() {
        isSingleShow = !isSingleShow;
        imgShowType.setImageDrawable(getResources().getDrawable(isSingleShow ? R.mipmap.single : R.mipmap.double_show));
        adapter.setSingleShow(isSingleShow);
        listData.clear();
        if (isSingleShow) {
            recyclerView.setLayoutManager(linearLayoutManager);
        } else {
            recyclerView.setLayoutManager(gridLayoutManager);
        }
        adapter.notifyDataSetChanged();
        listData.addAll(appOriginList);
        adapter.notifyDataSetChanged();
    }

    @OnClick(R.id.search_view)
    public void openSearchActivity(View view){
        Intent intent = new Intent(this, SearchActivity.class);
        if(Build.VERSION.SDK_INT > 20){
            ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(this, view, "search");
            ActivityCompat.startActivity(this, intent, compat.toBundle());
        }else{
            startActivity(intent);
        }
    }

    private void checkAppVersion(){
        String url = "http://otp9vas7i.bkt.clouddn.com/SearchAppVersion.txt";
        HttpUtil.getRequest(url,
                new HttpUtil.OnDataCallbackListener() {
                    @Override
                    public void onSuccess(String data) {
                        if(!TextUtils.isEmpty(data)){
                            try{
                                JSONObject jsonObject = new JSONObject(data);
                                int version = jsonObject.getInt("result");
                                PackageManager manager = MainActivity.this.getPackageManager();
                                PackageInfo info = manager.getPackageInfo(MainActivity.this.getPackageName(), 0);
                                if (version > info.versionCode) {
                                    showUpdateDialog(jsonObject.getString("apkUrl"));
                                }
                            } catch(PackageManager.NameNotFoundException e){
                                e.printStackTrace();
                            } catch(JSONException e){
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, error);
                    }
                });
    }

    private void showUpdateDialog(final String url) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("检测到有新的版本，是否更新？\n当前版本：" + getAppVersionCode());
        builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                downloadApk(url);
            }
        });
        builder.setNegativeButton("暂不更新", null);
        updateDialog = builder.create();
        updateDialog.setCanceledOnTouchOutside(false);
        updateDialog.show();
    }

    private void downloadApk(String url) {
        String apkFilePath = getExternalFilesDir(null).getPath() + "/SearchApp.apk";
        DownloadAsyncTask downloadAsyncTask = new DownloadAsyncTask(this, url, apkFilePath, downloadDialog);
        downloadAsyncTask.execute(0);
    }

    private String getAppVersionCode() {
        String versionName = "";
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(), 0);
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }
}
