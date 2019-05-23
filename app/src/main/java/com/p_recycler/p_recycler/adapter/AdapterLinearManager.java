package com.p_recycler.p_recycler.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.p_recycler.p_recycler.R;

import java.util.List;

// adapter
public class AdapterLinearManager extends RecyclerView.Adapter<HolderLinearManager> {

    private Context context;
    private List<String> contents;

    public AdapterLinearManager(Context context, List<String> contents) {
        this.context = context;
        this.contents = contents;
    }

    public void notifys(List<String> contents) {
        this.contents = contents;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HolderLinearManager onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        return new HolderLinearManager(LayoutInflater.from(context).inflate(R.layout.item_rcv_linear, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HolderLinearManager holderLinearManager, int i) {
        holderLinearManager.tv_linear.setText(contents.get(i));
    }

    @Override
    public int getItemCount() {
        return contents.size();
    }
}
