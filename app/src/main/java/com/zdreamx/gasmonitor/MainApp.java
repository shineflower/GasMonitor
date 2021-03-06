package com.zdreamx.gasmonitor;

import android.app.Application;

import com.zdreamx.gasmonitor.util.PreferenceUtil;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by Ashia on 2015/4/26.
 */
public class MainApp extends Application {
    Utils.NodeDataReturnData[] Datas;

    String spinnerText;

    public Utils.NodeDataReturnData[] getDatas() {
        return Datas;
    }

    public void setDatas(Utils.NodeDataReturnData[] datas) {
        Datas = datas;
    }

    public String getSpinnerText() {
        return spinnerText;
    }

    public void setSpinnerText(String spinnerText) {
        this.spinnerText = spinnerText;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);

        if (PreferenceUtil.isUserExist(this) && PreferenceUtil.getPushSettings(this)) {
            JPushInterface.resumePush(this);
        } else {
            JPushInterface.stopPush(this);
        }
    }
}
