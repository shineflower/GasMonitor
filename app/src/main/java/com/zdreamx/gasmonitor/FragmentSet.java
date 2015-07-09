package com.zdreamx.gasmonitor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.zdreamx.gasmonitor.util.PreferenceUtil;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by zdreamx on 2015/3/26.
 */
public class FragmentSet extends PreferenceFragment implements OnSharedPreferenceChangeListener {

    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        addPreferencesFromResource(R.xml.settings_preference);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();
        if ("logout".equals(key)) {
            SharedPreferences sp = mContext.getSharedPreferences("setting", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.remove("uname");
            editor.remove("passwd");
            editor.commit();

            JPushInterface.stopPush(mContext.getApplicationContext());

            Intent intent = new Intent(mContext,LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ("push_switch".equals(key)) {
            boolean allowPush = sharedPreferences.getBoolean("push_switch", false);
            if (allowPush) {
                JPushInterface.resumePush(mContext.getApplicationContext());
            } else {
                JPushInterface.stopPush(mContext.getApplicationContext());
            }
            PreferenceUtil.getPushSettings(mContext);
        }
    }
}
