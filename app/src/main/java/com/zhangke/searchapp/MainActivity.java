package com.zhangke.searchapp;

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zhangke.searchapp.model.AppInfo;
import com.zhangke.searchapp.model.AppInfoDao;
import com.zhangke.searchapp.utils.DownloadAsyncTask;
import com.zhangke.searchapp.utils.HttpUtil;
import com.zhangke.searchapp.utils.UiUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
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
    @BindView(R.id.card_title_view)
    CardView cardTitleView;
    @BindView(R.id.floating_btn)
    FloatingActionButton floatingBtn;

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

    private PopupWindow popupWindow;
    /**
     * FloatingButton 是否为打开状态
     */
    private boolean floatingBtnIsOpen = false;
    private int popupHeight = 0;

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
        popupHeight = UiUtils.dip2px(this, 150);
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
        } else {
            swipeRefresh.setRefreshing(true);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (appOriginList != null && !appOriginList.isEmpty()) {
                    appOriginList.clear();
                }
                appOriginList.addAll(ApplicationInfoUtil.getAllNoSystemProgramInfo(MainActivity.this));
                Collections.sort(appOriginList);
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
                        } else {
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
        if (isSingleShow) {
            recyclerView.setLayoutManager(linearLayoutManager);
        } else {
            recyclerView.setLayoutManager(gridLayoutManager);
        }
        recyclerView.setAdapter(null);
        recyclerView.setAdapter(adapter);
    }

    @OnClick(R.id.search_view)
    public void openSearchActivity(View view) {
        Intent intent = new Intent(this, SearchActivity.class);
        if (Build.VERSION.SDK_INT > 20) {
            ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(this, view, "search");
            ActivityCompat.startActivity(this, intent, compat.toBundle());
        } else {
            startActivity(intent);
        }
    }

    private void checkAppVersion() {
        String url = "http://otp9vas7i.bkt.clouddn.com/AppVersion.txt";
        HttpUtil.getRequest(url,
                new HttpUtil.OnDataCallbackListener() {
                    @Override
                    public void onSuccess(String data) {
                        if (!TextUtils.isEmpty(data)) {
                            try {
                                JSONObject jsonObject = new JSONObject(data);
                                int version = jsonObject.getInt("result");
                                PackageManager manager = MainActivity.this.getPackageManager();
                                PackageInfo info = manager.getPackageInfo(MainActivity.this.getPackageName(), 0);
                                if (version > info.versionCode) {
                                    showUpdateDialog(jsonObject.getString("apkUrl"));
                                }
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
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

    @OnClick(R.id.floating_btn)
    public void onFloatingClick(View view) {
        if (floatingBtnIsOpen) {
            closeFloatingMenu();
        } else {
            openFloatingMenu();
        }
    }

    private void openFloatingMenu() {
        ObjectAnimator openAnim = ObjectAnimator.ofFloat(floatingBtn, "rotation", 0, -155.0F, -135.0F);
        openAnim.setDuration(500);
        openAnim.start();

        if (popupWindow == null) {
            View popupView = LayoutInflater.from(this).inflate(R.layout.view_popup_window, null);
            popupWindow = new PopupWindow(popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true);
            ViewGroup llClearProcess = (ViewGroup) popupView.findViewById(R.id.ll_clear);
            ViewGroup llTODO = (ViewGroup) popupView.findViewById(R.id.ll_todo);
            llClearProcess.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                }
            });
            llTODO.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                }
            });
            popupView.setFocusable(false);
            popupWindow.setBackgroundDrawable(new ColorDrawable());
            popupWindow.setContentView(popupView);
            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    closeFloatingMenu();
                    floatingBtnIsOpen = false;
                }
            });
        }
        popupWindow.showAsDropDown(floatingBtn, 0, -popupHeight - floatingBtn.getHeight() / 2, Gravity.TOP);
        floatingBtnIsOpen = true;
    }

    private void closeFloatingMenu() {
        ObjectAnimator closeAnim = ObjectAnimator.ofFloat(floatingBtn, "rotation", -135F, 20.0F, 0);
        closeAnim.setDuration(500);
        closeAnim.start();

        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && null != popupWindow && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
        return super.onKeyDown(keyCode, event);
    }
}
