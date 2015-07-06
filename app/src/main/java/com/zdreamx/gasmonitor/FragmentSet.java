package com.zdreamx.gasmonitor;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by zdreamx on 2015/3/26.
 */
public class FragmentSet extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_preference);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();
        if ("logout".equals(key)) {
            SharedPreferences sp = getActivity().getSharedPreferences("setting", Context.MODE_PRIVATE );
            SharedPreferences.Editor editor = sp.edit();
            editor.remove("uname");
            editor.remove("passwd");
            editor.commit();
            Intent intent = new Intent(getActivity(),LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
