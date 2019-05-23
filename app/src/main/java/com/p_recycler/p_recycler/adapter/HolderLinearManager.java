package com.p_recycler.p_recycler.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.p_recycler.p_recycler.R;

// holder
    public class HolderLinearManager extends RecyclerView.ViewHolder {

        public TextView tv_linear;

        public HolderLinearManager(@NonNull View itemView) {
            super(itemView);
            tv_linear = itemView.findViewById(R.id.tv_linear);
        }
    }
