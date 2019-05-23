package com.p_recycler.p_recycler.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.p_recycler.p_recycler.R;

// holder
public class HolderStaggerManager extends RecyclerView.ViewHolder {

    public ImageView iv_stagger;
    public  TextView tv_stagger;

    public HolderStaggerManager(@NonNull View itemView) {
        super(itemView);
        iv_stagger = itemView.findViewById(R.id.iv_stagger);
        tv_stagger = itemView.findViewById(R.id.tv_stagger);
    }
}
