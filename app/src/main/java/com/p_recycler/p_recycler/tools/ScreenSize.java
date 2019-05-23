package com.p_recycler.p_recycler.tools;/**
 * Created by Administrator on 2016/12/12.
 */

import android.app.Activity;
import android.content.Context;

/**
 * 作用:获取屏幕尺寸大小
 * 作者:Administrator
 * 日期:2016/12/12
 * 时间:23:56
 * 项目:LiyiBuy
 * 作者:Administrator
 */
public class ScreenSize {

    public static SizeBean getSize(Context context) {
        Activity activity = (Activity) context;
        SizeBean sizeBean = new SizeBean();
        sizeBean.width = activity.getWindowManager().getDefaultDisplay().getWidth();
        sizeBean.height = activity.getWindowManager().getDefaultDisplay().getHeight();
        return sizeBean;
    }

    public static class SizeBean {
        public int width;
        public int height;
    }

}
