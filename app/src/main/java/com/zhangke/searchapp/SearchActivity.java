package com.zhangke.searchapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.zhangke.searchapp.model.AppInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by 张可 on 2017/10/9.
 */

public class SearchActivity extends AppCompatActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.et_search)
    EditText etSearch;
    @BindView(R.id.img_clear)
    ImageView imgClear;
    @BindView(R.id.ll_empty)
    LinearLayout llEmpty;

    private List<AppInfo> listData = new ArrayList<>();
    private APPListAdapter adapter;
    private AppClickPresenter appClickPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        adapter = new APPListAdapter(this, listData);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        appClickPresenter = new AppClickPresenter(this);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    imgClear.setVisibility(GONE);
                    listData.clear();
                    recyclerView.setVisibility(GONE);
                    llEmpty.setVisibility(VISIBLE);
                } else {
                    imgClear.setVisibility(VISIBLE);
                    listData.clear();
                    for (AppInfo info : MainActivity.appOriginList) {
                        if (!TextUtils.isEmpty(info.appName) &&
                                (info.appName.contains(s) || info.packageName.contains(s))) {
                            listData.add(info);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    if(listData.isEmpty()){
                        recyclerView.setVisibility(GONE);
                        llEmpty.setVisibility(VISIBLE);
                    }else{
                        recyclerView.setVisibility(VISIBLE);
                        llEmpty.setVisibility(GONE);
                    }
                }
            }
        });

        adapter.setOnItemClickListener(new APPListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                appClickPresenter.onAppClick(listData.get(position));
            }
        });
    }

    @OnClick(R.id.img_back)
    public void finishClick() {
        onBackPressed();
    }

    @OnClick(R.id.img_clear)
    public void clear() {
        etSearch.setText("");
    }

}
