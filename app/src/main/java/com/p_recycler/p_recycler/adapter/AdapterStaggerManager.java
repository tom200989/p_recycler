package com.p_recycler.p_recycler.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import com.p_recycler.p_recycler.R;
import com.p_recycler.p_recycler.bean.StaggerBean;

import java.util.List;

// adapter
public class AdapterStaggerManager extends RecyclerView.Adapter<HolderStaggerManager> {

    private Context context;
    private List<StaggerBean> staggerBeans;

    public AdapterStaggerManager(Context context, List<StaggerBean> staggerBeans) {
        this.context = context;
        this.staggerBeans = staggerBeans;
    }

    public void notifys(List<StaggerBean> staggerBeans) {
        this.staggerBeans = staggerBeans;
        notifyDataSetChanged();
        // notifyItemRangeChanged(0, staggerBeans.size());
    }

    @NonNull
    @Override
    public HolderStaggerManager onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new HolderStaggerManager(LayoutInflater.from(context).inflate(R.layout.item_rcv_stagger, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HolderStaggerManager holder, int i) {
        StaggerBean gridBean = staggerBeans.get(i);
        holder.iv_stagger.setImageDrawable(gridBean.getDrawable());
        holder.tv_stagger.setText(gridBean.getContent());
        holder.iv_stagger.setOnClickListener(v -> Toast.makeText(context, "click position: " + i, Toast.LENGTH_LONG).show());
    }

    @Override
    public int getItemCount() {
        return staggerBeans.size();
    }
}
