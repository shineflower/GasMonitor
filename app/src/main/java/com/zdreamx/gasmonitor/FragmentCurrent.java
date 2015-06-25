package com.zdreamx.gasmonitor;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zdreamx on 2015/3/26.
 */
public class FragmentCurrent extends Fragment {
    private final int Request_NODE_Data = 0;
    private final int Request_Mobile_Data = 1;
    private ListView listview;
    private HttpAsyncExecutor asyncExecutor;
    private LiteHttpClient client;
    //private boolean isGetNodes=false;
    private ListAdapter listAdapter; //列表适配器
    private int cur_listIndex; //用于异步获取数据当前点的索引值
    private  String getlatest_url;
    static List<Map<String,String>> list = new ArrayList<Map<String, String>>();//列表数据map
    private ProgressDialog mProgressDialog;
    private Handler myHandler = new Handler(){
        @Override
        public void handleMessage(final Message msg) {
            //super.handleMessage(msg);
            switch (msg.what) {
                case Request_NODE_Data: //获取节点数据
                    SharedPreferences sp = getActivity().getSharedPreferences("setting", Context.MODE_PRIVATE);
                    String uname = sp.getString("uname","STRING_NOT_EXIST");
                    if(uname.equals("STRING_NOT_EXIST")) { //如果本地参数不存在则直接到登录页面
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
                                    for(int i=0;i<o.Nodes.length;i++){ //填充数据
                                        Map<String,String> map = new HashMap<String,String>();
                                        map.put("nick",o.Nodes[i].Nick+"  ");
                                        map.put("mobile",o.Nodes[i].Mobile);
                                        map.put("note",o.Nodes[i].Note);
                                        list.add(map);
                                    }
                                    listAdapter = new SimpleAdapter(getActivity(),list,R.layout.node_list_current,new String[]{"nick","mobile","time","status"},new int[]{R.id.nick,R.id.mobile,R.id.time,R.id.status});
                                    listview.setAdapter(listAdapter);
                                    //isGetNodes=true;
                                    Message msg1 = new Message();
                                    msg1.what=Request_Mobile_Data;
                                    msg1.arg1=0;
                                    myHandler.sendMessage(msg1); //继续获取具体数据
                                }
                            } else {
                                Toast.makeText(getActivity(),"返回结果错误，请重新登录",Toast.LENGTH_LONG).show();
                                mProgressDialog.dismiss();
                            }
                        }
                        @Override
                        protected void onFailure(HttpException e, Response response) {
                            Toast.makeText(getActivity(),"网络访问错",Toast.LENGTH_LONG).show();
                            mProgressDialog.dismiss();
                        }
                    });

                    break;
                case Request_Mobile_Data: //获取节点的具体数据
                    cur_listIndex=msg.arg1;
                    if(cur_listIndex<list.size()) {
                        Map<String,String> map = list.get(cur_listIndex); //获取列表中的map
                        asyncExecutor.execute(new Request(getlatest_url+map.get("mobile")),new HttpModelHandler<Utils.NodeDataReturnData>() {
                            @Override
                            protected void onSuccess(Utils.NodeDataReturnData o, Response response) {
                                if(o.Result) { //返回正确
                                    //重新获取map
                                    Map<String,String> new_map = list.get(cur_listIndex); //获取列表中的map

                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    long diff = 0;
                                    try {
                                        Date updateTime = sdf.parse(o.Mytime);
                                        diff = System.currentTimeMillis() - updateTime.getTime();

                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    new_map.put("time","更新时间 "+o.Mytime +"（" + getDuring(diff) + " )");
                                    //生成显示内容为html格式
                                    String html="<html><head></head><body>" +
                                            "<div><span>井底压力："+ String.valueOf(o.Data.Wellpress) +"</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+
                                            "<span style= \"float:right;padding-right:10px;\">套压："+String.valueOf(o.Data.Taopress)+"</span></div>"+
                                            "<div><span>气体流量："+ String.valueOf(o.Data.Gasstandflow) +"</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+
                                            "<span>工作频率："+ String.valueOf(o.Data.Speed) +"</span></div>"+
                                            "</body></html>";
                                    new_map.put("status", Html.fromHtml(html).toString());
                                    list.set(cur_listIndex,new_map);
                                    listview.setAdapter(listAdapter);
                                    listview.deferNotifyDataSetChanged();
                                    if((cur_listIndex+1) < list.size()){ //还没有到最后一口井
                                        Message msg2 = new Message();
                                        msg2.what=Request_Mobile_Data;
                                        msg2.arg1=cur_listIndex+1;
                                        myHandler.sendMessage(msg2);
                                    } else {
                                        mProgressDialog.dismiss();
                                    }
                                }
                            }

                            private String getDuring(long diff) {
                                diff /= 1000;
                                if (diff < 0) {
                                    return null;
                                } else if (diff < 60) {
                                    return "刚刚";
                                } else if (diff < 60 * 60) {
                                    return diff / 60 + "分钟前";
                                } else if (diff < 60 * 60 * 60) {
                                    return diff / 60 / 60 + "小时前";
                                } else  {
                                    return diff / 60 / 60 / 24 + "天前";
                                }


                            }

                            @Override
                            protected void onFailure(HttpException e, Response response) {
                                mProgressDialog.dismiss();
                            }
                        });
                    }
                    break;
            }

        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_current,container,false);
        listview = (ListView) view.findViewById(R.id.current_listview);
        //建立异步请求实例
        client = LiteHttpClient.newApacheHttpClient(getActivity());
        asyncExecutor = HttpAsyncExecutor.newInstance(client);
        getlatest_url = "http://"+getResources().getString(R.string.server_ip)+"/api/getnodedata/getlatest?mobile=";

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setTitle("加载中...");
        mProgressDialog.setMessage("正在加载，请等待...");
        mProgressDialog.show();
        myHandler.sendEmptyMessage(Request_NODE_Data);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String,String> l = (Map<String, String>) listAdapter.getItem(position);
                Intent intent = new Intent();
                intent.setClass(getActivity(),ViewCurrentActivity.class);
                intent.putExtra("nick",l.get("nick"));
                intent.putExtra("mobile",l.get("mobile"));
                startActivity(intent);
            }
        });
        return view;
    }
}