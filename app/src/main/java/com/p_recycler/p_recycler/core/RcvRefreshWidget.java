package com.p_recycler.p_recycler.core;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.p_recycler.p_recycler.R;
import com.p_recycler.p_recycler.tools.Other;
import com.p_recycler.p_recycler.tools.ScreenSize;

/*
 * Created by qianli.ma on 2019/5/14 0014.
 */
public class RcvRefreshWidget extends RelativeLayout {

    /* 内置属性 */
    private String TAG = "RcvRefreshWidget";
    private int screenHeight = 0;// 屏幕高度

    /* 默认值 */
    private float HEAD_LOGO_RATE_VALUE = 0.5f;
    private float BOTTOM_LOGO_RATE_VALUE = 0.5f;
    private int LOGO_ROTATE_DURATION_VALUE = 500;
    private String HEAD_BACKGRAOUND_COLOR_VALUE = "#FFF7ED";
    private String BOTTOM_BACKGRAOUND_COLOR_VALUE = "#ECF9FD";
    private int HEAD_LOGO_SRC_ID_VALUE = R.drawable.head_loading;
    private int BOTTOM_LOGO_SRC_ID_VALUE = R.drawable.bottom_loading;

    /* 自定义属性 */
    private float HEAD_LOGO_RATE = HEAD_LOGO_RATE_VALUE;// 头部图标比例(占父布局, 默认0.5f)
    private float BOTTOM_LOGO_RATE = BOTTOM_LOGO_RATE_VALUE;// 底部图标比例(占父布局, 默认0.5f)
    private int LOGO_ROTATE_DURATION = LOGO_ROTATE_DURATION_VALUE;// 图标旋转一圈的时间(默认500ms)
    private String HEAD_BACKGRAOUND_COLOR = HEAD_BACKGRAOUND_COLOR_VALUE;// 头部背景色
    private String BOTTOM_BACKGRAOUND_COLOR = BOTTOM_BACKGRAOUND_COLOR_VALUE;// 底部背景色
    private int HEAD_LOGO_SRC_ID = HEAD_LOGO_SRC_ID_VALUE;// 头部图标
    private int BOTTOM_LOGO_SRC_ID = BOTTOM_LOGO_SRC_ID_VALUE;// 底部图标

    /* 视图控件 */
    private RelativeLayout rlRefreshHead;// 头部背景
    private ImageView ivRefreshHead;// 头部图标
    private RcvMAWidget rcv;// 列表
    private RelativeLayout rlLoadBottom;// 底部背景
    private ImageView ivLoadBottom;// 底部图标

    public RcvRefreshWidget(Context context) {
        this(context, null, 0);
    }

    public RcvRefreshWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RcvRefreshWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 获取配置属性
        getAttrs(attrs);
        // 获取屏幕高度
        getSrceenSize(context);
        // 初始化视图
        inflateView(context);
        // 初始化监听
        initListener();
    }

    /**
     * 获取列表 (由开发人员对RCV对象进行设置)
     *
     * @return 列表
     */
    public RcvMAWidget getRcv() {
        return rcv;
    }

    /**
     * 刷新完成 (由开发人员根据业务逻辑调用)
     */
    public void refreshFinish() {
        rcv.refreshFinish();
    }

    /**
     * 加载完成 (由开发人员根据业务逻辑调用)
     */
    public void loadFinish() {
        rcv.loadFinish();
    }

    /* -------------------------------------------- private -------------------------------------------- */

    /**
     * 获取配置属性
     */
    private void getAttrs(AttributeSet attrs) {
        String nameSpace = "http://schemas.android.com/apk/res-auto";
        HEAD_LOGO_RATE = attrs.getAttributeFloatValue(nameSpace, "head_logo_rate", HEAD_LOGO_RATE_VALUE);
        BOTTOM_LOGO_RATE = attrs.getAttributeFloatValue(nameSpace, "bottom_logo_rate", BOTTOM_LOGO_RATE_VALUE);
        LOGO_ROTATE_DURATION = attrs.getAttributeIntValue(nameSpace, "logo_rotate_duration", LOGO_ROTATE_DURATION_VALUE);
        HEAD_BACKGRAOUND_COLOR = attrs.getAttributeValue(nameSpace, "head_background_color");
        BOTTOM_BACKGRAOUND_COLOR = attrs.getAttributeValue(nameSpace, "bottom_background_color");
        HEAD_LOGO_SRC_ID = attrs.getAttributeResourceValue(nameSpace, "head_logo_src_id", HEAD_LOGO_SRC_ID_VALUE);
        BOTTOM_LOGO_SRC_ID = attrs.getAttributeResourceValue(nameSpace, "bottom_logo_src_id", BOTTOM_LOGO_SRC_ID_VALUE);
        checkValue();
    }

    /**
     * 检查属性合理性
     */
    private void checkValue() {
        // 头部图标高度比例
        if (HEAD_LOGO_RATE <= 0 | HEAD_LOGO_RATE > 1) {
            HEAD_LOGO_RATE = HEAD_LOGO_RATE_VALUE;
        }
        // 底部图标高度比例
        if (BOTTOM_LOGO_RATE <= 0 | BOTTOM_LOGO_RATE > 1) {
            BOTTOM_LOGO_RATE = BOTTOM_LOGO_RATE_VALUE;
        }
        // 图标旋转一圈时间(毫秒)
        if (LOGO_ROTATE_DURATION < 0) {
            LOGO_ROTATE_DURATION = LOGO_ROTATE_DURATION_VALUE;
        }
        // 头部背景色
        if (!isColorMatch(HEAD_BACKGRAOUND_COLOR)) {
            HEAD_BACKGRAOUND_COLOR = HEAD_BACKGRAOUND_COLOR_VALUE;
        }
        // 底部背景色
        if (!isColorMatch(BOTTOM_BACKGRAOUND_COLOR)) {
            BOTTOM_BACKGRAOUND_COLOR = BOTTOM_BACKGRAOUND_COLOR_VALUE;
        }
        // 头部图标
        if (!isDrawMath(HEAD_LOGO_SRC_ID)) {
            HEAD_LOGO_SRC_ID = HEAD_LOGO_SRC_ID_VALUE;
        }
        // 底部图标
        if (!isDrawMath(BOTTOM_LOGO_SRC_ID)) {
            BOTTOM_LOGO_SRC_ID = BOTTOM_LOGO_SRC_ID_VALUE;
        }
    }

    /**
     * 颜色字符值是否符合条件
     *
     * @param colorStr 颜色字符值
     * @return T: 符合
     */
    private boolean isColorMatch(String colorStr) {
        // 是否为空
        boolean isNotEmpty = !TextUtils.isEmpty(colorStr);
        if (isNotEmpty) {
            // 是否以#号开头
            boolean isStartWith = colorStr.startsWith("#");
            // #号个数是否超限
            boolean isNotOverNum = Other.getCharNumFromStr(colorStr, "#") == 1;
            // 字符个数是否为7位
            boolean isLength = colorStr.length() == 7;
            return isNotEmpty & isStartWith & isNotOverNum & isLength;
        }
        return false;
    }

    /**
     * 资源是否符合条件
     *
     * @param drawId 图元
     * @return T: 符合
     */
    private boolean isDrawMath(int drawId) {
        try {
            Drawable drawable = getResources().getDrawable(drawId);
            return true;
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "resource had not found : " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取屏幕高度
     *
     * @param context 域
     */
    private void getSrceenSize(Context context) {
        screenHeight = ScreenSize.getSize(context).height;
    }

    /**
     * 初始化视图
     *
     * @param context 域
     */
    private void inflateView(Context context) {
        // 查找ID
        View inflate = View.inflate(context, R.layout.widget_rcv_refresh, this);
        rlRefreshHead = inflate.findViewById(R.id.rl_refresh_head);
        ivRefreshHead = inflate.findViewById(R.id.iv_refresh_head);
        rcv = inflate.findViewById(R.id.rcv);
        rlLoadBottom = inflate.findViewById(R.id.rl_load_bottom);
        ivLoadBottom = inflate.findViewById(R.id.iv_load_bottom);
        // 设置默认值
        rlRefreshHead.setBackground(new ColorDrawable(Color.parseColor(HEAD_BACKGRAOUND_COLOR)));
        rlLoadBottom.setBackground(new ColorDrawable(Color.parseColor(BOTTOM_BACKGRAOUND_COLOR)));
        ivRefreshHead.setImageDrawable(ContextCompat.getDrawable(context, HEAD_LOGO_SRC_ID));
        ivLoadBottom.setImageDrawable(ContextCompat.getDrawable(context, BOTTOM_LOGO_SRC_ID));
    }

    /**
     * 初始化监听
     */
    private void initListener() {

        // 1.下拉刷新: 手指拖动过程中使用
        rcv.setOnRefreshFingerMoveListener(marginTop -> {
            setRefreshHead(marginTop);// 设置头部间距
            Log.i("ma_refresh_widget", "setOnRefreshFingerMoveListener: margintop = " + marginTop);
        });

        // 2.下拉刷新: 超出指定margin top自动回滚到指定高度时使用
        rcv.setOnRefreshAutoShrinkListener(marginTop -> {
            setRefreshHead(marginTop);// 设置头部间距
            Log.w("ma_refresh_widget", "setOnRefreshAutoShrinkListener: margintop = " + marginTop);
        });

        // 3.下拉刷新: 主动调用回滚操作后使用
        rcv.setOnRefreshManualShringkListener(marginTop -> {
            stopRotateAnim(ivRefreshHead);// 停止旋转动画
            setRefreshHead(marginTop);// 设置头部间距
            Log.w("ma_refresh_widget", "setOnRefreshManualShringkListener: margintop = " + marginTop);
        });

        // 4.下拉刷新: 手指抬起后使用
        rcv.setOnRefreshFingerUpListener(() -> {
            startRotateAnim(ivRefreshHead);// 启动旋转动画
            beginRefreshNext(rcv);// 回调
            Log.w("ma_refresh_widget", "setOnRefreshFingerUpListener: 到顶松开手指了 -- 可以执行你的业务逻辑了");
        });

        // 5.上拉加载: 滚动到底部处于idle状态使用
        rcv.setOnLoadMoreIdleListener(() -> {
            startRotateAnim(ivLoadBottom);// 启动旋转动画
            beginLoadMoreNext(rcv);// 回调
            Log.i("ma_refresh_widget", "setOnLoadMoreIdleListener: 到底了 -- 可以执行你的业务逻辑了");
        });

        // 6.上拉加载: 底部回滚时使用
        rcv.setOnLoadShrinkListener(marginBottom -> {
            stopRotateAnim(ivLoadBottom);// 停止旋转动画
            setLoadBottom(marginBottom);// 设置底部间距
            Log.w("ma_refresh_widget", "setOnLoadShrinkListener: marginBottom = " + marginBottom);
        });

        // 7.上拉加载: 面板弹出时使用
        rcv.setOnLoadBottomHeightListener(marginBottom -> {
            setLoadBottom(marginBottom);// 设置底部间距
            Log.i("ma_refresh_widget", "setOnLoadBottomHeightListener: marginBottom = " + marginBottom);
        });
    }

    /**
     * 设置头部/底部图标旋转动画
     *
     * @param view 目标view
     */
    private void startRotateAnim(View view) {
        RotateAnimation ra = new RotateAnimation(0, 360, 1, 0.5f, 1, 0.5f);
        ra.setFillAfter(true);
        ra.setRepeatCount(Animation.INFINITE);
        ra.setRepeatMode(Animation.INFINITE);
        ra.setDuration(LOGO_ROTATE_DURATION);
        ra.setInterpolator(new LinearInterpolator());
        view.setAnimation(ra);
        ra.startNow();
        view.startAnimation(ra);
    }

    /**
     * 停止头部/底部图标旋转动画
     *
     * @param view 目标view
     */
    private void stopRotateAnim(View view) {
        Animation animation = view.getAnimation();
        if (animation != null) {
            animation.cancel();
        }
        view.clearAnimation();
    }

    /**
     * 设置头部间距
     *
     * @param height 间距
     */
    private void setRefreshHead(float height) {
        // 1.设置头部底版高度
        LayoutParams lp = (LayoutParams) rlRefreshHead.getLayoutParams();
        lp.height = (int) height;
        rlRefreshHead.setLayoutParams(lp);
        // 2.设置头部图标大小
        RelativeLayout.LayoutParams ip = (LayoutParams) ivRefreshHead.getLayoutParams();
        ip.height = height >= screenHeight * 0.1f ? (int) (screenHeight * 0.1f * HEAD_LOGO_RATE) : (int) (height * HEAD_LOGO_RATE);
        ip.width = ip.height;
        ivRefreshHead.setLayoutParams(ip);
        // 3.设置拖动时logo旋转
        ivRefreshHead.setRotation(height * 2);
    }

    /**
     * 设置底部间距
     *
     * @param height 间距
     */
    private void setLoadBottom(float height) {
        // 1.设置底部底版高度
        LayoutParams lp = (LayoutParams) rlLoadBottom.getLayoutParams();
        lp.height = (int) height;
        rlLoadBottom.setLayoutParams(lp);
        // 2.设置底部图标大小
        RelativeLayout.LayoutParams ip = (LayoutParams) ivLoadBottom.getLayoutParams();
        ip.height = height >= screenHeight * 0.1f ? (int) (screenHeight * 0.1f * BOTTOM_LOGO_RATE) : (int) (height * BOTTOM_LOGO_RATE);
        ip.width = ip.height;
        ivLoadBottom.setLayoutParams(ip);
    }

    /* -------------------------------------------- impl -------------------------------------------- */

    private OnBeginRefreshListener onBeginRefreshListener;

    // Inteerface--> 接口OnBeginRefreshListener
    public interface OnBeginRefreshListener {
        void beginRefresh(RecyclerView rcv);
    }

    // 对外方式setOnBeginRefreshListener
    public void setOnBeginRefreshListener(OnBeginRefreshListener onBeginRefreshListener) {
        this.onBeginRefreshListener = onBeginRefreshListener;
    }

    // 封装方法beginRefreshNext
    private void beginRefreshNext(RecyclerView rcv) {
        if (onBeginRefreshListener != null) {
            onBeginRefreshListener.beginRefresh(rcv);
        }
    }

    private OnBeginLoadMoreListener onBeginLoadMoreListener;

    // Inteerface--> 接口OnBeginLoadMoreListener
    public interface OnBeginLoadMoreListener {
        void beginLoadMore(RecyclerView rcv);
    }

    // 对外方式setOnBeginLoadMoreListener
    public void setOnBeginLoadMoreListener(OnBeginLoadMoreListener onBeginLoadMoreListener) {
        this.onBeginLoadMoreListener = onBeginLoadMoreListener;
    }

    // 封装方法beginLoadMoreNext
    private void beginLoadMoreNext(RecyclerView rcv) {
        if (onBeginLoadMoreListener != null) {
            onBeginLoadMoreListener.beginLoadMore(rcv);
        }
    }
}
