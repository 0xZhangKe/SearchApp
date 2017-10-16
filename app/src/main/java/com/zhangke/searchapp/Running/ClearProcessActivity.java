package com.zhangke.searchapp.Running;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.zhangke.searchapp.Main.MainActivity;
import com.zhangke.searchapp.R;
import com.zhangke.searchapp.model.AppInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ZhangKe on 2017/10/12.
 */

public class ClearProcessActivity extends AppCompatActivity implements ClearClickContract.View{

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.empty_view)
    LinearLayout emptyView;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;

    private List<AppInfo> listData = new ArrayList<>();
    private ClearProcessAdapter adapter;

    private PackageManager packageManager;
    private Drawable defaultDrawable;

    private ClearClickContract.Presenter clickPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_clear_process);
        ButterKnife.bind(this);

        toolbar.setTitle("Running App");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        adapter = new ClearProcessAdapter(this, listData);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryAllRunningAppInfo();
            }
        });

        defaultDrawable = getResources().getDrawable(R.mipmap.ic_launcher_round);
        clickPresenter = new ClearClickPresenter(this, this);

        packageManager = getPackageManager();
        queryAllRunningAppInfo();

        adapter.setOnItemClickListener(new ClearProcessAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                clickPresenter.onAppClick(listData.get(position));
            }
        });
    }

    private void queryAllRunningAppInfo() {
        if (swipeRefresh != null && !swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(true);
        }
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> observableEmitter) throws Exception {
                ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningAppProcessInfo> appProcessList = activityManager.getRunningAppProcesses();

                if (listData != null && !listData.isEmpty()) {
                    listData.clear();
                }

                for (int i = 0; i < appProcessList.size(); i++) {
                    ActivityManager.RunningAppProcessInfo runningAppProcessInfo = appProcessList.get(i);
                    String[] pkgArray = appProcessList.get(i).pkgList;
                    for (int j = 0; j < pkgArray.length; j++) {
                        AppInfo info = new AppInfo();
                        info.packageName = pkgArray[j];
                        info.uid = runningAppProcessInfo.uid;
                        info.pid = runningAppProcessInfo.pid;
                        info.processName = runningAppProcessInfo.processName;
                        info.appName = getAppNameWithPkg(info.packageName);
                        if(TextUtils.isEmpty(info.appName)){
                            info.appName = info.processName;
                        }
                        try {
                            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(info.packageName, PackageManager.GET_ACTIVITIES);
                            info.appIcon = applicationInfo.loadIcon(packageManager);
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                        if (info.appIcon == null) {
                            info.appIcon = defaultDrawable;
                        }

                        listData.add(info);
                    }
                }
                observableEmitter.onNext(1);
                observableEmitter.onComplete();
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        adapter.notifyDataSetChanged();
                        swipeRefresh.setRefreshing(false);
                        if (listData.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            emptyView.setVisibility(View.GONE);
                        }
                    }
                });
    }

    /**
     * 通过包名获取 APP 名
     *
     * @param packageName 包名
     */
    private String getAppNameWithPkg(String packageName) {
        String name = "";
        if (MainActivity.appOriginList != null && !MainActivity.appOriginList.isEmpty()) {
            for (AppInfo info : MainActivity.appOriginList) {
                if (TextUtils.equals(info.packageName, packageName)) {
                    name = info.appName;
                    break;
                }
            }
        }
        return name;
    }

    @Override
    public void refresh() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                queryAllRunningAppInfo();
            }
        });
    }
}
