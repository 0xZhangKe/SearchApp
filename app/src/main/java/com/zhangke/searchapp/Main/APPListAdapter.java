package com.zhangke.searchapp.Main;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhangke.searchapp.R;
import com.zhangke.searchapp.model.AppInfo;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ZhangKe on 2017/10/8.
 */

public class APPListAdapter extends RecyclerView.Adapter<APPListAdapter.AppInfoViewHolder> {

    private Context context;
    private List<AppInfo> listData;

    private LayoutInflater inflater;

    private boolean isSingleShow = true;//单排显示
    private OnItemClickListener onItemClickListener;

    public APPListAdapter(Context context, List<AppInfo> listData) {
        this.context = context;
        this.listData = listData;

        inflater = LayoutInflater.from(context);
    }

    @Override
    public AppInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AppInfoViewHolder(
                        inflater.inflate(isSingleShow ? R.layout.adapter_single_list : R.layout.adapter_double_list,
                                parent,
                                false));
    }

    @Override
    public void onBindViewHolder(AppInfoViewHolder holder, int position) {
        AppInfo info = listData.get(position);
        holder.tvDesc.setText(info.packageName);
        holder.tvName.setText(info.appName);
        holder.imgIcon.setImageDrawable(info.appIcon);
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    class AppInfoViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.img_icon)
        ImageView imgIcon;
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_desc)
        TextView tvDesc;

        public AppInfoViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if(onItemClickListener != null){
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemClickListener.onItemClick(getAdapterPosition());
                    }
                });
            }
        }
    }

    class AppInfoViewGridHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.img_icon)
        ImageView imgIcon;
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_desc)
        TextView tvDesc;

        public AppInfoViewGridHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if(onItemClickListener != null){
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemClickListener.onItemClick(getAdapterPosition());
                    }
                });
            }
        }
    }

    public void setSingleShow(boolean singleShow) {
        isSingleShow = singleShow;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    interface OnItemClickListener{
        void onItemClick(int position);
    }
}
