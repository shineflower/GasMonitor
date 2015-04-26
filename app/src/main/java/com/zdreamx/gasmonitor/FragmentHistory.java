package com.zdreamx.gasmonitor;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zdreamx on 2015/3/26.
 */
public class FragmentHistory extends Fragment {
    private ListView mListView;
    private TextView mHistoryHint, mChoose, mNick;
    private Utils.NodeDataReturnData[] mDatas;
    private String mSpinnerText;

    private static final int REQUEST_CODE = 1;

    private Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            ListAdapter listAdapter = new SimpleAdapter(getActivity(), getData(), R.layout.node_list_history, new String[]{"井底压力", "套压", "液柱高度", "气体流量", "气体总量", "气体压力", "运行频率", "设定频率", "母线电压"}, new int[]{R.id.Wellpress, R.id.Taopress, R.id.Liquidhigh, R.id.Gasstandflow, R.id.Gastotalflow, R.id.Gaspress, R.id.Speed, R.id.Motortemp, R.id.Motordcbus});
            mListView.setAdapter(listAdapter);
        }
    };
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_history,container,false);
        mListView = (ListView) view.findViewById(R.id.history_listview);
        mHistoryHint = (TextView) view.findViewById(R.id.history_hint);
        mNick = (TextView) view.findViewById(R.id.mobile);

        return view;
    }

    @Override
    public void onResume() {
        getActivity().setTitle(R.string.historydata);
        super.onResume();
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_history_query, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_history_query) {
            Intent chooseIntent = new Intent(getActivity(), ChooseItemActivity.class);
            startActivityForResult(chooseIntent, REQUEST_CODE);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        MainApp mainApp = (MainApp) getActivity().getApplication();
        mDatas = mainApp.getDatas();
        mSpinnerText = mainApp.getSpinnerText();
        myHandler.sendEmptyMessage(0);
    }

    private List<Map<String, String>> getData(){
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        Map<String, String> map;

        if (mDatas == null) {
            return list;
        }

        mHistoryHint.setVisibility(View.GONE);
        mNick.setVisibility(View.VISIBLE);
        mNick.setText(mSpinnerText);
        for (Utils.NodeDataReturnData data : mDatas) {
            map = new HashMap<String, String>();
            map.put("井底压力", "井底压力: " + data.Data.Wellpress);
            map.put("套压", "套压: " + data.Data.Taopress);
            map.put("液柱高度", "液柱高度: " + data.Data.Liquidhigh);
            map.put("气体流量", "气体流量: " + data.Data.Gasflow);
            map.put("气体总量", "气体总量: " + data.Data.Gastotalflow);
            map.put("气体压力", "气体压力: " + data.Data.Gaspress);
            map.put("运行频率", "运行频率: " + data.Data.Speed);
            map.put("设定频率", "设定频率: " + data.Data.Motortemp);
            map.put("母线电压", "母线电压:" + data.Data.Motordcbus);
            list.add(map);
        }

        return list;
    }
}
