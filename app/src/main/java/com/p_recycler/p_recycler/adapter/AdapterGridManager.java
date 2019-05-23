package com.p_recycler.p_recycler.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.p_recycler.p_recycler.R;
import com.p_recycler.p_recycler.bean.GridBean;

import java.util.List;

// adapter
public class AdapterGridManager extends RecyclerView.Adapter<HolderGridManager> {

    private Context context;
    private List<GridBean> gridBeans;

    public AdapterGridManager(Context context, List<GridBean> gridBeans) {
        this.context = context;
        this.gridBeans = gridBeans;
    }

    public void notifys(List<GridBean> gridBeans) {
        this.gridBeans = gridBeans;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HolderGridManager onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new HolderGridManager(LayoutInflater.from(context).inflate(R.layout.item_rcv_grid, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGridManager holder, int i) {
        GridBean gridBean = gridBeans.get(i);
        holder.iv_grid.setImageDrawable(gridBean.getDrawable());
        holder.tv_grid.setText(gridBean.getContent());
    }

    @Override
    public int getItemCount() {
        return gridBeans.size();
    }
}
