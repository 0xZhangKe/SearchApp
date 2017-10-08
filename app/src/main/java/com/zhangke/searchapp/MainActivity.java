package com.zhangke.searchapp;

import android.os.Bundle;
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
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhangke.searchapp.widget.RoundProgressDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.search_view)
    SearchView searchView;
    @BindView(R.id.tv_app_num)
    TextView tvAppNum;
    @BindView(R.id.img_show_type)
    ImageView imgShowType;

    private List<AppInfo> appOriginList = new ArrayList<>();

    private List<AppInfo> listData = new ArrayList<>();
    private APPListAdapter adapter;

    private RoundProgressDialog progressDialog;

    private LinearLayoutManager linearLayoutManager;
    private GridLayoutManager gridLayoutManager;

    private boolean isSingleShow = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        toolbar.setTitle("SearchApp");
        setSupportActionBar(toolbar);

        progressDialog = new RoundProgressDialog(this);

        initSearchView();

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        gridLayoutManager = new GridLayoutManager(this, 2);

        recyclerView.setHasFixedSize(true);

        getAppList();
    }

    private void initSearchView() {
        searchView.setSubmitButtonEnabled(true);
        SpannableString spanText = new SpannableString("输入 APP 名搜索");
        spanText.setSpan(new ForegroundColorSpan(0xff777777), 0, spanText.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        searchView.setQueryHint(spanText);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                listData.clear();
                for(AppInfo info : appOriginList){
                    if(!TextUtils.isEmpty(info.appName) &&
                            (info.appName.contains(query) || info.packageName.contains(query))){
                        listData.add(info);
                    }
                }
                adapter.notifyDataSetChanged();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                listData.clear();
                listData.addAll(appOriginList);
                return false;
            }
        });
    }

    private void getAppList() {
        progressDialog.showProgressDialog();
        if (appOriginList != null && !appOriginList.isEmpty()) {
            appOriginList.clear();
        }
        if (listData != null && !listData.isEmpty()) {
            listData.clear();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                appOriginList = ApplicationInfoUtil.getAllNoSystemProgramInfo(MainActivity.this);
                listData.addAll(appOriginList);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter = new APPListAdapter(MainActivity.this, listData);
                        recyclerView.setAdapter(adapter);
                        tvAppNum.setText("APP 数：" + listData.size());
                        progressDialog.closeProgressDialog();

                        adapter.setOnItemClickListener(new APPListAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(int position) {
                                ApplicationInfoUtil.doStartApplicationWithPackageName(MainActivity.this, listData.get(position).packageName);
                            }
                        });
                    }
                });
            }
        }).start();
    }

    @OnClick(R.id.img_show_type)
    public void changeShowType() {
        isSingleShow = !isSingleShow;
        imgShowType.setImageDrawable(getResources().getDrawable(isSingleShow ? R.mipmap.single : R.mipmap.double_show));
        adapter.setSingleShow(isSingleShow);
        if(isSingleShow){
            recyclerView.setLayoutManager(linearLayoutManager);
        }else{
            recyclerView.setLayoutManager(gridLayoutManager);
        }
    }
}
