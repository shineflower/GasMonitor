package com.zdreamx.gasmonitor.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by ashia on 15-7-7.
 */
public class PreferenceUtil {
    final static String TAG = PreferenceUtil.class.getSimpleName();

    public static boolean getPushSettings(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        boolean allowPush = settings.getBoolean("push_switch", false);
        Log.i(TAG, "allow Push : " + allowPush);
        return allowPush;
    }

    public static boolean isUserExist(Context context) {
        SharedPreferences sp = context.getSharedPreferences("setting", context.MODE_PRIVATE);
        String uname = sp.getString("uname","STRING_NOT_EXIST");
        String passwd = sp.getString("passwd", "STRING_NOT_EXIST");
        if (uname == "STRING_NOT_EXIST" || passwd == "STRING_NOT_EXIST") {
            return false;
        }
        return true;
    }
}
