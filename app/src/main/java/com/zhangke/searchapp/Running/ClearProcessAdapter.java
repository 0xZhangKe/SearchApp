package com.zhangke.searchapp.Running;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhangke.searchapp.Main.APPListAdapter;
import com.zhangke.searchapp.R;
import com.zhangke.searchapp.model.AppInfo;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by 张可 on 2017/10/16.
 */

public class ClearProcessAdapter extends RecyclerView.Adapter<ClearProcessAdapter.ViewHolder>{

    private Context context;
    private List<AppInfo> listData;

    private LayoutInflater inflater;
    private OnItemClickListener onItemClickListener;

    public ClearProcessAdapter(Context context, List<AppInfo> listData) {
        this.context = context;
        this.listData = listData;

        inflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.adapter_clear_process, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AppInfo info = listData.get(position);
        holder.tvName.setText(info.appName);
        holder.tvDesc.setText(info.processName);
        if(holder.imgIcon != null){
            holder.imgIcon.setImageDrawable(info.appIcon);
        }
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.img_icon)
        ImageView imgIcon;
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_desc)
        TextView tvDesc;

        public ViewHolder(View itemView) {
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

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    interface OnItemClickListener{
        void onItemClick(int position);
    }
}
