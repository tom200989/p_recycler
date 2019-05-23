package com.p_recycler.p_recycler.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.p_recycler.p_recycler.R;

// holder
public class HolderGridManager extends RecyclerView.ViewHolder {

    public ImageView iv_grid;
    public  TextView tv_grid;

    public HolderGridManager(@NonNull View itemView) {
        super(itemView);
        iv_grid = itemView.findViewById(R.id.iv_grid);
        tv_grid = itemView.findViewById(R.id.tv_grid);
    }
}
