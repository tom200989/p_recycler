package com.p_recycler.p_recycler.tools;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by qianli.ma on 2019/5/17 0017.
 */
public class Other {

    /**
     * 获取指定字符串中指定字符的个数
     *
     * @param str 字符串
     * @param chr 目标支付
     * @return 个数
     */
    public static int getCharNumFromStr(String str, String chr) {
        List<String> temps = new ArrayList<>();
        char[] chars = str.toCharArray();
        for (char aChar : chars) {
            String charstr = String.valueOf(aChar);
            if (charstr.equalsIgnoreCase(chr)) {
                temps.add(charstr);
            }
        }
        return temps.size();
    }

    /**
     * 隐藏软键盘
     */
    public static void hideKeyBoard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
        }
    }
}
