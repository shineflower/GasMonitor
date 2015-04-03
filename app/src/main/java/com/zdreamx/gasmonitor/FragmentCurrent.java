package com.zdreamx.gasmonitor;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.litesuits.http.LiteHttpClient;
import com.litesuits.http.async.HttpAsyncExecutor;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.request.Request;
import com.litesuits.http.response.Response;
import com.litesuits.http.response.handler.HttpModelHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zdreamx on 2015/3/26.
 */
public class FragmentCurrent extends Fragment {
    private ListView listview;
    private HttpAsyncExecutor asyncExecutor;
    private LiteHttpClient client;
    private final int Request_NODE_Data = 0;
    private final int Request_Mobile_Data = 1;
    private Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            switch (msg.what) {
                case Request_NODE_Data: //获取节点数据
                    SharedPreferences sp = getActivity().getSharedPreferences("setting", Context.MODE_PRIVATE);
                    String uname = sp.getString("uname","STRING_NOT_EXIST");
                    if(uname == "STRING_NOT_EXIST" ) { //如果本地参数不存在则直接到登录页面
                        Intent it = new Intent(getActivity(),LoginActivity.class); //需要跳转到登录页面
                        startActivity(it);
                        getActivity().finish();
                    }
                    String node_url ="http://"+getResources().getString(R.string.server_ip)+"/api/getnodes?uname="+uname;//请求节点url
                    asyncExecutor.execute(new Request(node_url),new HttpModelHandler<Utils.API_Return_Nodes>() {
                        @Override
                        protected void onSuccess(Utils.API_Return_Nodes o, Response response) {
                            if(o.Authorize){ //数据正确
                                if(o.Nodes.length>0) {
                                    List<Map<String,String>> list = new ArrayList<Map<String, String>>();
                                    for(int i=0;i<o.Nodes.length;i++){ //填充数据
                                        Map<String,String> map = new HashMap<String,String>();
                                        map.put("nick",o.Nodes[i].Nick);
                                        map.put("mobile",o.Nodes[i].Mobile);
                                        map.put("note",o.Nodes[i].Note);
                                        list.add(map);
                                    }
                                    ListAdapter listAdapter = new SimpleAdapter(getActivity(),list,R.layout.node_list_current,new String[]{"nick","mobile","time","status"},new int[]{R.id.nick,R.id.mobile,R.id.time,R.id.status});
                                    listview.setAdapter(listAdapter);
                                }
                            } else {
                                Toast.makeText(getActivity(),"返回结果错误，请重新登录",Toast.LENGTH_LONG);
                            }
                        }
                        @Override
                        protected void onFailure(HttpException e, Response response) {
                            Toast.makeText(getActivity(),"网络访问错",Toast.LENGTH_LONG);
                        }
                    });

                    break;
                case Request_Mobile_Data:
                    break;
            }

        }
    };
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_current,container,false);
        listview = (ListView) view.findViewById(R.id.cur_listview);
        //建立异步请求实例
        client = LiteHttpClient.newApacheHttpClient(getActivity());
        asyncExecutor = HttpAsyncExecutor.newInstance(client);
        myHandler.sendEmptyMessage(Request_NODE_Data);
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
        return list;
    }

}

