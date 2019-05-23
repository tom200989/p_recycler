package com.p_recycler.p_recycler.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;

import com.p_recycler.p_recycler.R;
import com.p_recycler.p_recycler.adapter.AdapterGridManager;
import com.p_recycler.p_recycler.adapter.AdapterLinearManager;
import com.p_recycler.p_recycler.adapter.AdapterStaggerManager;
import com.p_recycler.p_recycler.bean.GridBean;
import com.p_recycler.p_recycler.bean.StaggerBean;
import com.p_recycler.p_recycler.core.RcvMAWidget;
import com.p_recycler.p_recycler.core.RcvRefreshWidget;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private RcvRefreshWidget refresh;
    private RcvMAWidget rcv;
    private final String TAG = "ma_rcv";
    private List<String> contents = new ArrayList<>();
    private List<GridBean> gridBeans = new ArrayList<>();
    private List<StaggerBean> staggerBeans = new ArrayList<>();
    private AdapterLinearManager linearAdapter;
    private AdapterGridManager gridAdapter;
    private AdapterStaggerManager staggerAdapter;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 准备数据
        for (int i = 0; i < 30; i++) {
            contents.add("content : " + i);
        }

        for (int i = 0; i < 30; i++) {
            GridBean gridBean = new GridBean();
            gridBean.setDrawable(ContextCompat.getDrawable(this, R.drawable.grid_1));
            gridBean.setContent("content:" + i);
            gridBeans.add(gridBean);
        }

        for (int i = 0; i < 30; i++) {
            StaggerBean staggerBean = new StaggerBean();
            Drawable draw1 = ContextCompat.getDrawable(this, R.drawable.stagger_1);
            Drawable draw2 = ContextCompat.getDrawable(this, R.drawable.stagger_2);
            staggerBean.setDrawable(i % 2 == 0 ? draw1 : draw2);
            staggerBean.setContent("content:" + i);
            staggerBeans.add(staggerBean);
        }

        refresh = findViewById(R.id.refresh);
        rcv = refresh.getRcv();

        // 视图处理
        // rcv = findViewById(R.id.rcv);

        // rcv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        // linearAdapter = new AdapterLinearManager(this, contents);
        // rcv.setAdapter(linearAdapter);

        rcv.setLayoutManager(new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false));
        gridAdapter = new AdapterGridManager(this, gridBeans);
        rcv.setAdapter(gridAdapter);

        // StaggeredGridLayoutManager slm = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        // slm.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        // rcv.setLayoutManager(slm);
        // staggerAdapter = new AdapterStaggerManager(this, staggerBeans);
        // rcv.setAdapter(staggerAdapter);

        refresh.setOnBeginRefreshListener(rcv1 -> {
            refreshNew();
            // linearAdapter.notifys(contents);
            gridAdapter.notifys(gridBeans);
            // staggerAdapter.notifys(staggerBeans);
            new Handler().postDelayed(() -> refresh.refreshFinish(), 1000);
        });
        refresh.setOnBeginLoadMoreListener(rcv1 -> {
            addMore();
            // linearAdapter.notifys(contents);
            gridAdapter.notifys(gridBeans);
            // staggerAdapter.notifys(staggerBeans);
            new Handler().postDelayed(() -> refresh.loadFinish(), 1000);
        });

    }

    private void refreshNew() {
        contents.clear();
        for (int i = 0; i < 30; i++) {
            contents.add("refresh : " + i);
        }

        gridBeans.clear();
        for (int i = 0; i < 30; i++) {
            GridBean gridBean = new GridBean();
            gridBean.setDrawable(ContextCompat.getDrawable(this, R.drawable.grid_2));
            gridBean.setContent("refresh:" + i);
            gridBeans.add(gridBean);
        }

        staggerBeans.clear();
        for (int i = 0; i < 30; i++) {
            StaggerBean staggerBean = new StaggerBean();
            Drawable draw1 = ContextCompat.getDrawable(this, R.drawable.stagger_1);
            Drawable draw2 = ContextCompat.getDrawable(this, R.drawable.stagger_2);
            staggerBean.setDrawable(i % 2 == 0 ? draw1 : draw2);
            staggerBean.setContent("refresh:" + i);
            staggerBeans.add(staggerBean);
        }
    }

    private void addMore() {
        for (int i = 31; i < 60; i++) {
            contents.add("content : " + i);
        }

        for (int i = 31; i < 60; i++) {
            GridBean gridBean = new GridBean();
            gridBean.setDrawable(ContextCompat.getDrawable(this, R.drawable.grid_1));
            gridBean.setContent("content:" + i);
            gridBeans.add(gridBean);
        }

        for (int i = 31; i < 60; i++) {
            StaggerBean staggerBean = new StaggerBean();
            Drawable draw1 = ContextCompat.getDrawable(this, R.drawable.stagger_1);
            Drawable draw2 = ContextCompat.getDrawable(this, R.drawable.stagger_2);
            staggerBean.setDrawable(i % 2 == 0 ? draw1 : draw2);
            staggerBean.setContent("content:" + i);
            staggerBeans.add(staggerBean);
        }
    }
}
