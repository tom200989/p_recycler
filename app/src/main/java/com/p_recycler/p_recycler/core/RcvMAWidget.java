package com.p_recycler.p_recycler.core;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.AbsListView;
import android.widget.RelativeLayout;

import com.p_recycler.p_recycler.tools.Other;
import com.p_recycler.p_recycler.tools.TimerHelper;

import java.util.Objects;

/*
 * Created by qianli.ma on 2019/5/13 0013.
 */
public class RcvMAWidget extends RecyclerView {

    private Context context;
    private TimerHelper timerHelper;
    private boolean isShrink = false;// 是否正在收回面板(防止用户在［刷新/加载］过程中再次拖动)
    private int scrollState = -1;// 滚动状态
    private int STABLE_VALUE = 1;// 防抖动参数(在下拉刷新时提供一个像素的抖动范围)
    private final String TAG = "RcvWidget";
    private final int TOP_REFRESH_HEIGHT = 150;// 下拉刷新头部高度(默认: 150px)
    private final int BOTTOM_LOAD_HEIGHT = 100;// 上拉加载底部高度(默认: 100px)
    private final int TOP_REFRESH_SPEED = 3;// 下拉刷新头部速率值(默认: 1/3)
    private final int TOP_REFRESH_RESET_SPEED = 1;// 下拉刷新头部恢复速率(默认: 1ms)
    private final int BOTTOM_LOAD_RESET_SPEED = 1;// 上拉加载底部恢复速率(默认: 1ms)
    private int delMarginTopHeight;// 距离指定高度的差值


    public RcvMAWidget(@NonNull Context context) {
        this(context, null, 0);
    }

    public RcvMAWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RcvMAWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        // 滚动监听器(主用于监听滚动到［底部］的操作)
        setScrollListener();
        // 设置无阴影
        setOverScrollMode(OVER_SCROLL_NEVER);
        setVerticalFadingEdgeEnabled(false);
        setItemAnimator(null);
    }

    /**
     * 刷新完毕 (由外部调用收回面板)
     */
    protected void refreshFinish() {
        isShrink = true;
        new Thread(() -> {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) getLayoutParams();
            int topMargin = lp.topMargin;
            for (int i = topMargin; i >= 0; i--) {
                lp.topMargin = i;
                try {
                    Thread.sleep(TOP_REFRESH_RESET_SPEED);
                    ((Activity) context).runOnUiThread(() -> {
                        // TOAT: 主动触发回滚时 -- 回调
                        refreshManualShrinkNext(lp.topMargin);
                        setLayoutParams(lp);
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            isShrink = false;
        }).start();
    }

    /**
     * 加载完毕 (由外部调用收回面板)
     */
    protected void loadFinish() {
        isShrink = true;
        new Thread(() -> {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) getLayoutParams();
            int bottomMargin = lp.bottomMargin;
            for (int i = bottomMargin; i >= 0; i--) {
                lp.bottomMargin = i;
                try {
                    Thread.sleep(BOTTOM_LOAD_RESET_SPEED);
                    ((Activity) context).runOnUiThread(() -> {
                        // TOAT: 上拉加载回滚时 -- 回调
                        loadShrinkNext(lp.bottomMargin);
                        setLayoutParams(lp);
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            isShrink = false;
        }).start();
    }


    private float downY;// 按下坐标点(距离)
    private float moveY;// 移动坐标点(距离)
    private float distance;// 移动距离

    private float downDY;// 按下坐标点(方向)
    private float moveDY;// 移动坐标点(方向)
    private float direct;// 方向(正: 上推(手指往上推); 负: 下拖(手指往下拖))

    private float downTime;// 按下时间
    private float moveTime;// 移动时间

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                downY = downDY = event.getY();
                downTime = System.currentTimeMillis();
                Other.hideKeyBoard((Activity) context);
                break;

            case MotionEvent.ACTION_MOVE:
                moveY = moveDY = event.getY();
                moveTime = System.currentTimeMillis();
                distance = moveY - downY;
                direct = moveDY - downDY;
                Log.i(TAG, "方向 : " + (direct > 0 ? "下拖(手指往下拖)" : "上推(手指往上推)"));// 备用变量

                if (direct < 0) {
                    // 此处做拖动中的判断 -- 瀑布布局除外, 瀑布布局会出现误判
                    if (isSecondLastBottom(RcvMAWidget.this, 2)) {
                        if (!(getLayoutManager() instanceof StaggeredGridLayoutManager)) {
                            // TOAT: 到达底部弹出面板时 -- 回调
                            loadBottomHeightNext(BOTTOM_LOAD_HEIGHT);
                            // 设置底部间距
                            setRcvMarginBottom(BOTTOM_LOAD_HEIGHT);
                            // TOAT: 已滚动到底部并停止 -- 回调
                            loadMoreIdleNext();
                            Log.v(TAG, "到达倒数第2个");
                            return false;
                        }
                    }
                }

                // 手指移动时逻辑
                if (fingerMove()) {
                    // 手指触摸移动时不分发
                    return false;
                }
                break;

            case MotionEvent.ACTION_UP:
                // 手指抬起时的逻辑
                fingerUp();
                // 回复位移为0
                downY = moveY = distance = 0;
                downTime = moveTime = 0;
                break;
        }

        downDY = moveDY;// 重置当前移动点为下压值(辅助判断下拖上推用)

        // 默认情况: 手指抬起走系统分发逻辑
        return super.dispatchTouchEvent(event);
    }

    /**
     * 手指移动时逻辑
     *
     * @return T:正在移动
     */
    private boolean fingerMove() {
        // 1.判断是否为［手势拖动］状态
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            // 2.已经到顶
            if (!canScrollVertically(-1)) {
                // 3.是否处于自动回滚状态
                if (!isShrink) {
                    // 3.0.方向是否为［下拖(手指往下拖)］

                    /*
                     * 当手势为［下拖］时, distance > 1个像素作为下拖的前提条件
                     * 当手势为［上推］时, distance > 1个像素的条件下, 依旧按照距离动态计算marginTop
                     * 当distance <= 1个像素时, 马上强制marginTop = 0, 而不要再动态计算. 因为"(int) (distance / TOP_REFRESH_SPEED)"
                     * 这一句是存在细微误差的, 因此会出现一种现象:
                     * ［当手指缓慢往上推, 即将到达顶部时, 由于Action_MOVE会不停的进行, 那么margintop会不停的计算, 如果此时小于1像素时
                     * 计算的误差值 > 1px, 则会出现了抖动, 因此, 以1px作为强制归零的标准］
                     */

                    if (direct > 0) {
                        // 3.1.防抖动设置 
                        if (distance > STABLE_VALUE) {
                            Log.w(TAG, "实时距离 : " + distance);
                            // 3.2.动态改变上间距
                            int marginTop = (int) (distance / TOP_REFRESH_SPEED);
                            setRcvMarginTop(marginTop);
                            // TOAT : 移动中上间距变动 -- 回调
                            refreshFingerMoveNext(marginTop);
                        }

                    } else {

                        if (distance > STABLE_VALUE) {
                            // 3.1.动态改变上间距
                            Log.w(TAG, "direct > 1 在上推(手指往上推)");
                            int marginTop = (int) (distance / TOP_REFRESH_SPEED);
                            setRcvMarginTop(marginTop);
                            // TOAT : 移动中上间距变动 -- 回调
                            refreshFingerMoveNext(marginTop);
                        } else {

                            Log.w(TAG, "direct <= 1 在上推(手指往上推)");
                            setRcvMarginTop(0);
                            refreshFingerMoveNext(0);

                            /*
                             * 此处可以按照业务需求进行更改, 现在的状态是
                             * ［手指先下拖, 出现head之后, 再上推, 当把head推到顶之后(完全收回去之后), 就不再响应手指的移动逻辑］
                             * 而微信是可以继续响应的, 具体做法, 是在此处设置为: scrollToPosition(0); return false;
                             *  */
                            scrollToPosition(0);
                            return false;
                        }

                    }
                }
                // 4.正在移动 -- 不分发 -- 交付给自身逻辑处理
                return true;
            }
        }

        return false;
    }

    /* -------------------------------------------- private -------------------------------------------- */

    /**
     * 当手指抬起时的逻辑 -- 回滚 + 回调
     */
    private void fingerUp() {
        // 1.如果超过了指定下拉高度 --> 先回滚到指定高度
        if (getRcvMarginTop() >= TOP_REFRESH_HEIGHT) {
            // 2.回滚到下拉的指定高度
            shrinkTopHeight();
            // 3.启动定时器 --> 开启延迟
            if (timerHelper != null) {
                timerHelper.stop();
                timerHelper = null;
            }

            timerHelper = new TimerHelper((Activity) context) {
                @Override
                public void doSomething() {
                    ((Activity) context).runOnUiThread(() -> {
                        // 4.TOAT 手指抬起 -- 回调
                        delMarginTopHeight = 0;
                        refreshFingerUpNext();
                        Log.w(TAG, "到顶啦, 可以刷新啦");
                    });
                }
            };

            // 3.1.计算回调延迟时间() -- 避免与自动回滚冲突
            float delay = TOP_REFRESH_RESET_SPEED * 1.2f * delMarginTopHeight;
            timerHelper.startDelay((int) delay);

        } else {
            refreshFinish();
        }
    }

    /**
     * 回滚收缩到指定高度
     */
    private void shrinkTopHeight() {
        isShrink = true;
        new Thread(() -> {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) getLayoutParams();
            int topMargin = lp.topMargin;
            delMarginTopHeight = topMargin - TOP_REFRESH_HEIGHT;
            for (int i = topMargin; i > TOP_REFRESH_HEIGHT; i--) {
                lp.topMargin = i;
                try {
                    Thread.sleep(TOP_REFRESH_RESET_SPEED);
                    ((Activity) context).runOnUiThread(() -> {
                        // TOAT: 自动回滚至指定高度 -- 回调
                        refreshAutoShringkNext(lp.topMargin);
                        setLayoutParams(lp);
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            isShrink = false;
        }).start();
    }

    /**
     * 滚动监听器(主用于监听滚动到［底部］的操作)
     */
    private void setScrollListener() {
        addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                scrollState = newState;

                /* 此处处理［滑到底］逻辑 */
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    if (isVisBottom(RcvMAWidget.this)) {
                        if (!canScrollVertically(1)) {
                            // TOAT: 到达底部弹出面板时 -- 回调
                            loadBottomHeightNext(BOTTOM_LOAD_HEIGHT);
                            // 设置底部间距
                            setRcvMarginBottom(BOTTOM_LOAD_HEIGHT);
                            // 设置rcv自动滚到底部
                            scrollToPosition(Objects.requireNonNull(getAdapter()).getItemCount() - 1);
                            // TOAT: 已滚动到底部并停止 -- 回调
                            loadMoreIdleNext();
                            Log.v(TAG, "到底了, 可以加载数据了");
                        }
                    }
                }


            }
        });
    }

    /**
     * 是否滚到底
     *
     * @param recyclerView 列表
     * @return T: 到底
     */
    private boolean isVisBottom(RecyclerView recyclerView) {
        // 获取布局管理器
        LayoutManager lp = recyclerView.getLayoutManager();
        /* 分格布局 */
        if (lp instanceof GridLayoutManager) {
            GridLayoutManager layoutManager = (GridLayoutManager) lp;
            //屏幕中最后一个完全可见子项的 position (注意:是完全可见)
            int lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
            //当前屏幕所看到的子项个数
            int visibleItemCount = layoutManager.getChildCount();
            //当前 RecyclerView 的所有子项个数O
            int totalItemCount = layoutManager.getItemCount();
            //RecyclerView 的滑动状态
            int state = recyclerView.getScrollState();
            return visibleItemCount > 0// 屏幕可见item数大于0
                           && lastVisibleItemPosition == totalItemCount - 1 // 屏幕中最后一个完全可见子项的 position为最后一个
                           && state == RecyclerView.SCROLL_STATE_IDLE;// 处于滑动停止状态
        } else
            /* 瀑布布局 */
            if (lp instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) lp;
                //屏幕中最后一个完全可见子项的 position (注意:是完全可见)
                int[] lastVisibleItemPositionArr = layoutManager.findLastCompletelyVisibleItemPositions(null);
                int lastVisibleItemPosition = Math.max(lastVisibleItemPositionArr[0], lastVisibleItemPositionArr[1]);
                //当前屏幕所看到的子项个数
                int visibleItemCount = layoutManager.getChildCount();
                //当前 RecyclerView 的所有子项个数O
                int totalItemCount = layoutManager.getItemCount();
                //RecyclerView 的滑动状态
                int state = recyclerView.getScrollState();
                return visibleItemCount > 0// 屏幕可见item数大于0
                               && lastVisibleItemPosition == totalItemCount - 1 // 屏幕中最后一个完全可见子项的 position为最后一个
                               && state == RecyclerView.SCROLL_STATE_IDLE;// 处于滑动停止状态
            } else
                /* 线性布局 */
                if (lp instanceof LinearLayoutManager) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) lp;
                    //屏幕中最后一个完全可见子项的 position (注意:是完全可见)
                    int lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
                    //当前屏幕所看到的子项个数
                    int visibleItemCount = layoutManager.getChildCount();
                    //当前 RecyclerView 的所有子项个数O
                    int totalItemCount = layoutManager.getItemCount();
                    //RecyclerView 的滑动状态
                    int state = recyclerView.getScrollState();
                    return visibleItemCount > 0// 屏幕可见item数大于0
                                   && lastVisibleItemPosition == totalItemCount - 1 // 屏幕中最后一个完全可见子项的 position为最后一个
                                   && state == RecyclerView.SCROLL_STATE_IDLE;// 处于滑动停止状态
                }
        return false;
    }

    /**
     * 是否到达倒数第x个位置
     *
     * @param recyclerView       列表
     * @param lastSecondPosition 指定倒数第X个
     * @return T: 到达指定位置 (倒数第X个)
     */
    private boolean isSecondLastBottom(RecyclerView recyclerView, int lastSecondPosition) {
        // 获取布局管理器
        LayoutManager lp = getLayoutManager();
        /* 分格布局 */
        if (lp instanceof GridLayoutManager) {
            GridLayoutManager layoutManager = (GridLayoutManager) lp;
            //屏幕中最后一个完全可见子项的 position (注意:是完全可见)
            int lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
            //当前屏幕所看到的子项个数
            int visibleItemCount = layoutManager.getChildCount();
            //当前 RecyclerView 的所有子项个数O
            int totalItemCount = layoutManager.getItemCount();
            //RecyclerView 的滑动状态
            int state = recyclerView.getScrollState();
            return visibleItemCount > 0// 屏幕可见item数大于0
                           && lastVisibleItemPosition == totalItemCount - lastSecondPosition // 屏幕中最后一个完全可见子项的 position为最后第X个
                           && state == RecyclerView.SCROLL_STATE_DRAGGING;// 处于手指按压滑动状态
        }

        /* 瀑布布局 */
        if (lp instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) lp;
            //屏幕中最后一个完全可见子项的 position (注意:是完全可见)
            int[] lastVisibleItemPositionArr = layoutManager.findLastCompletelyVisibleItemPositions(null);
            int lastVisibleItemPosition = Math.max(lastVisibleItemPositionArr[0], lastVisibleItemPositionArr[1]);
            //当前屏幕所看到的子项个数
            int visibleItemCount = layoutManager.getChildCount();
            //当前 RecyclerView 的所有子项个数O
            int totalItemCount = layoutManager.getItemCount();
            //RecyclerView 的滑动状态
            int state = recyclerView.getScrollState();
            return visibleItemCount > 0// 屏幕可见item数大于0
                           && lastVisibleItemPosition == totalItemCount - lastSecondPosition // 屏幕中最后一个完全可见子项的 position为最后第X个
                           && state == RecyclerView.SCROLL_STATE_DRAGGING;// 处于手指按压滑动状态
        }

        /* 线性布局 */
        if (lp instanceof LinearLayoutManager) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) lp;
            //屏幕中最后一个完全可见子项的 position (注意:是完全可见)
            int lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
            //当前屏幕所看到的子项个数
            int visibleItemCount = layoutManager.getChildCount();
            //当前 RecyclerView 的所有子项个数O
            int totalItemCount = layoutManager.getItemCount();
            //RecyclerView 的滑动状态
            int state = recyclerView.getScrollState();
            return visibleItemCount > 0// 屏幕可见item数大于0
                           && lastVisibleItemPosition == totalItemCount - lastSecondPosition // 屏幕中最后一个完全可见子项的 position为最后第X个
                           && state == RecyclerView.SCROLL_STATE_DRAGGING;// 处于手指按压滑动状态
        }

        return false;
    }

    /**
     * 设置顶部间距
     *
     * @param marginTop 顶部间距变量
     */
    private void setRcvMarginTop(int marginTop) {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) getLayoutParams();
        lp.topMargin = marginTop;
        setLayoutParams(lp);
    }

    /**
     * 获取顶部间距
     *
     * @return 顶部间距
     */
    private int getRcvMarginTop() {
        return ((RelativeLayout.LayoutParams) getLayoutParams()).topMargin;
    }

    /**
     * 设置底部间距
     *
     * @param marginBottom 底部间距变量
     */
    private void setRcvMarginBottom(int marginBottom) {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) getLayoutParams();
        lp.bottomMargin = marginBottom;
        setLayoutParams(lp);
    }

    /**
     * 获取底部间距
     *
     * @return 底部间距
     */
    private int getRcvMarginBottom() {
        return ((RelativeLayout.LayoutParams) getLayoutParams()).bottomMargin;
    }



    /* -------------------------------------------- impl -------------------------------------------- */

    /* 1.下拉刷新: 手指拖动过程中使用 */
    private OnRefreshFingerMoveListener onRefreshFingerMoveListener;

    // Inteerface--> 接口OnRefreshFingerMoveListener
    public interface OnRefreshFingerMoveListener {
        void refreshFingerMove(float marginTop);
    }

    /**
     * 下拉刷新: 手指拖动过程中使用
     */
    protected void setOnRefreshFingerMoveListener(OnRefreshFingerMoveListener onRefreshFingerMoveListener) {
        this.onRefreshFingerMoveListener = onRefreshFingerMoveListener;
    }

    // 封装方法refreshFingerMoveNext
    private void refreshFingerMoveNext(float marginTop) {
        if (onRefreshFingerMoveListener != null) {
            onRefreshFingerMoveListener.refreshFingerMove(marginTop);
        }
    }

    /* 2.下拉刷新: 超出指定Margin top自动回滚到指定高度时使用 */
    private OnRefreshAutoShrinkListener onRefreshAutoShrinkListener;

    // Inteerface--> 接口OnRefreshAutoShrinkListener
    public interface OnRefreshAutoShrinkListener {
        void refreshAutoShringk(float marginTop);
    }

    /**
     * 下拉刷新: 超出指定Margin top自动回滚到指定高度时使用
     */
    protected void setOnRefreshAutoShrinkListener(OnRefreshAutoShrinkListener onRefreshAutoShrinkListener) {
        this.onRefreshAutoShrinkListener = onRefreshAutoShrinkListener;
    }

    // 封装方法refreshAutoShringkNext
    private void refreshAutoShringkNext(float marginTop) {
        if (onRefreshAutoShrinkListener != null) {
            onRefreshAutoShrinkListener.refreshAutoShringk(marginTop);
        }
    }

    /* 3.下拉刷新: 主动调用回滚操作后使用 */
    private OnRefreshManualShringkListener onRefreshManualShringkListener;

    // Inteerface--> 接口OnRefreshManualShringkListener
    public interface OnRefreshManualShringkListener {
        void refreshManualShrink(float marginTop);
    }

    /**
     * 下拉刷新: 主动调用回滚操作后使用
     */
    protected void setOnRefreshManualShringkListener(OnRefreshManualShringkListener onRefreshManualShringkListener) {
        this.onRefreshManualShringkListener = onRefreshManualShringkListener;
    }

    // 封装方法refreshManualShrinkNext
    private void refreshManualShrinkNext(float marginTop) {
        if (onRefreshManualShringkListener != null) {
            onRefreshManualShringkListener.refreshManualShrink(marginTop);
        }
    }

    /* 4.下拉刷新: 手指抬起后使用 */
    private OnRefreshFingerUpListener onRefreshFingerUpListener;

    // Inteerface--> 接口OnRefreshListener
    public interface OnRefreshFingerUpListener {
        void refreshFingerUp();
    }

    /**
     * 下拉刷新: 手指抬起后使用
     */
    protected void setOnRefreshFingerUpListener(OnRefreshFingerUpListener onRefreshFingerUpListener) {
        this.onRefreshFingerUpListener = onRefreshFingerUpListener;
    }

    // 封装方法RefreshNext
    private void refreshFingerUpNext() {
        if (onRefreshFingerUpListener != null) {
            onRefreshFingerUpListener.refreshFingerUp();
        }
    }

    /* 5.上拉加载: 滚到底部并停止后使用 */
    private OnLoadMoreIdleListener onLoadMoreIdleListener;

    // Inteerface--> 接口OnloadMoreListener
    public interface OnLoadMoreIdleListener {
        void loadMoreIdle();
    }

    /**
     * 上拉加载: 滚到底部并停止后使用
     */
    protected void setOnLoadMoreIdleListener(OnLoadMoreIdleListener onLoadMoreIdleListener) {
        this.onLoadMoreIdleListener = onLoadMoreIdleListener;
    }

    // 封装方法loadMoreNext
    private void loadMoreIdleNext() {
        if (onLoadMoreIdleListener != null) {
            onLoadMoreIdleListener.loadMoreIdle();
        }
    }

    /* 6.上拉加载: 底部回滚时使用 */
    private OnLoadShrinkListener onLoadShrinkListener;

    // Inteerface--> 接口OnLoadShrinkListener
    public interface OnLoadShrinkListener {
        void loadShrink(float marginBottom);
    }

    /**
     * 上拉加载: 底部回滚时使用
     */
    protected void setOnLoadShrinkListener(OnLoadShrinkListener onLoadShrinkListener) {
        this.onLoadShrinkListener = onLoadShrinkListener;
    }

    // 封装方法loadShrinkNext
    private void loadShrinkNext(float marginBottom) {
        if (onLoadShrinkListener != null) {
            onLoadShrinkListener.loadShrink(marginBottom);
        }
    }

    /* 7.上拉加载: 面板弹出时使用 */
    private OnLoadBottomHeightListener onLoadBottomHeightListener;

    // Inteerface--> 接口OnLoadBottomHeightListener
    public interface OnLoadBottomHeightListener {
        void loadBottomHeight(float marginBottom);
    }

    /**
     * 上拉加载: 面板弹出时使用
     */
    protected void setOnLoadBottomHeightListener(OnLoadBottomHeightListener onLoadBottomHeightListener) {
        this.onLoadBottomHeightListener = onLoadBottomHeightListener;
    }

    // 封装方法loadBottomHeightNext
    private void loadBottomHeightNext(float marginBottom) {
        if (onLoadBottomHeightListener != null) {
            onLoadBottomHeightListener.loadBottomHeight(marginBottom);
        }
    }
}
