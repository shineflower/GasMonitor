package com.zdreamx.gasmonitor;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zdreamx on 2015/3/26.
 */
public class FragmentHistory extends Fragment {
    private ListView listview;
    private Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            ListAdapter listAdapter = new SimpleAdapter(getActivity(),getData(),R.layout.node_list_history,new String[]{"nick","mobile","note"},new int[]{R.id.nick,R.id.mobile,R.id.note});
            listview.setAdapter(listAdapter);
        }
    };
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history,container,false);
        listview = (ListView) view.findViewById(R.id.history_listview);
        myHandler.sendEmptyMessage(0);
        return view;
    }
    private List<Map<String,String>> getData(){
        List<Map<String,String>> list = new ArrayList<Map<String, String>>();
        Map<String,String> map = new HashMap<String,String>();
        map.put("nick","测试1");
        map.put("mobile","13548484");
        map.put("note","shosugjeige");
        list.add(map);

         map = new HashMap<String,String>();
        map.put("nick","测试2");
        map.put("mobile","13548484");
        map.put("note","shosugjeige");
        list.add(map);

        map = new HashMap<String,String>();
        map.put("nick","测试3");
        map.put("mobile","13548484");
        map.put("note","shosugjeige");
        list.add(map);

        map = new HashMap<String,String>();
        map.put("nick","测试3");
        map.put("mobile","13548484");
        map.put("note","shosugjeige");
        list.add(map);
        map = new HashMap<String,String>();
        map.put("nick","测试3");
        map.put("mobile","13548484");
        map.put("note","shosugjeige");
        list.add(map);
        map = new HashMap<String,String>();
        map.put("nick","测试3");
        map.put("mobile","13548484");
        map.put("note","shosugjeige");
        list.add(map);
        map = new HashMap<String,String>();
        map.put("nick","测试3");
        map.put("mobile","13548484");
        map.put("note","shosugjeige");
        list.add(map);
        map = new HashMap<String,String>();
        map.put("nick","测试3");
        map.put("mobile","13548484");
        map.put("note","shosugjeige");
        list.add(map);
        map = new HashMap<String,String>();
        map.put("nick","测试3");
        map.put("mobile","13548484");
        map.put("note","shosugjeige");
        list.add(map);
        map = new HashMap<String,String>();
        map.put("nick","测试3");
        map.put("mobile","13548484");
        map.put("note","shosugjeige");
        list.add(map);
        map = new HashMap<String,String>();
        map.put("nick","测试3");
        map.put("mobile","13548484");
        map.put("note","shosugjeige");
        list.add(map);
        map = new HashMap<String,String>();
        map.put("nick","测试3");
        map.put("mobile","13548484");
        map.put("note","shosugjeige");
        list.add(map);
        map = new HashMap<String,String>();
        map.put("nick","测试3");
        map.put("mobile","13548484");
        map.put("note","shosugjeige");
        list.add(map);
        map = new HashMap<String,String>();
        map.put("nick","测试3");
        map.put("mobile","13548484");
        map.put("note","shosugjeige");
        list.add(map);
        map = new HashMap<String,String>();
        map.put("nick","测试3");
        map.put("mobile","13548484");
        map.put("note","shosugjeige");
        list.add(map);
        map = new HashMap<String,String>();
        map.put("nick","测试3");
        map.put("mobile","13548484");
        map.put("note","shosugjeige");
        list.add(map);
        map = new HashMap<String,String>();
        map.put("nick","测试3");
        map.put("mobile","13548484");
        map.put("note","shosugjeige");
        list.add(map);
        map = new HashMap<String,String>();
        map.put("nick","测试3");
        map.put("mobile","13548484");
        map.put("note","shosugjeige");
        list.add(map);

        return list;
    }
}
