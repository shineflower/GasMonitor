package com.zdreamx.gasmonitor;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import com.litesuits.http.LiteHttpClient;
import com.litesuits.http.async.HttpAsyncExecutor;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.request.Request;
import com.litesuits.http.response.Response;
import com.litesuits.http.response.handler.HttpModelHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

/**
 * Created by zdreamx on 2015/3/26.
 */
public class FragmentWarning extends Fragment {
    private ListView mListView;
    private LiteHttpClient mClient;
    private HttpAsyncExecutor mAsyncExecutor;
    private Context mContext;
    private ProgressDialog mProgressDialog;
    private List<Map<String, String>> mList;
    private Set<String> tags;

    private static final int REQUEST_WARNING_DATA = 0;
    private static final String TAG = "FragmentWarning";

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REQUEST_WARNING_DATA:
                    SharedPreferences sp = getActivity().getSharedPreferences("setting", Context.MODE_PRIVATE);
                    String uname = sp.getString("uname", null);
                    String warning_url = "http://" + getResources().getString(R.string.server_ip) + "/api/getwarnlogs?user=" + uname;
                    mAsyncExecutor.execute(new Request(warning_url), new HttpModelHandler<Utils.ApiJsonWarnLogReturn>() {
                        @Override
                        protected void onSuccess(Utils.ApiJsonWarnLogReturn o, Response response) {
                            if (o.Result) {   //有数据
                                if (o.Numbers > 0) {
                                    mList = new ArrayList<Map<String, String>>();
                                    tags = new HashSet<String>();
                                    for (Utils.ApiJsonWarnLog info : o.Info) {
                                        Map<String, String> map = new HashMap<String, String>();
                                        map.put("报警数据", "报警数据: " + info.Logs);
                                        map.put("昵称", "昵称: " + info.Nick);
                                        map.put("号码", "号码: " + info.Mobile);
                                        map.put("Id", "Id: " + info.LatestLog.Id);
                                        map.put("井号", "井号: " + info.LatestLog.Mobile);
                                        map.put("触发参数", "触发参数: " + info.LatestLog.Trigger);
                                        map.put("详细消息", "详细消息: " + info.LatestLog.Message);
                                        map.put("是否被查看", "是否被查看: " + (info.LatestLog.View ? "是" : "否"));
                                        map.put("创建时间", "创建时间: " + info.LatestLog.Createtime);
                                        mList.add(map);

                                        ListAdapter adapter = new SimpleAdapter(mContext, mList, R.layout.node_list_warning, new String[]{"报警数据", "昵称", "号码", "Id", "井号", "触发参数", "详细消息", "是否被查看", "创建时间"}, new int[]{R.id.Logs, R.id.Nick, R.id.Mobile, R.id.Id, R.id.Latest_Mobile, R.id.Trigger, R.id.Message, R.id.View, R.id.Createtime});
                                        mListView.setAdapter(adapter);
                                        mProgressDialog.dismiss();

                                        //推送
                                        tags.add(info.Mobile);  //将每个口井的名称打上tag
                                    }

                                    JPushInterface.setTags(mContext, tags, new TagAliasCallback() {
                                        @Override
                                        public void gotResult(int requestCode, String alias, Set<String> set) {
                                            if (requestCode == 0) {  //设置成功
                                                Log.i(TAG, "设置成功");
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(mContext, "返回结果错误，请重新登录", Toast.LENGTH_LONG);
                                    mProgressDialog.dismiss();
                                }
                            }
                        }

                        @Override
                        protected void onFailure(HttpException e, Response response) {
                            Toast.makeText(mContext, "请求数据发送错误", Toast.LENGTH_LONG).show();
                            mProgressDialog.dismiss();
                        }
                    });
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_warning, container, false);

        mListView = (ListView) view.findViewById(R.id.warning_listview);

        mContext = getActivity();

        mClient = LiteHttpClient.newApacheHttpClient(mContext);
        mAsyncExecutor = HttpAsyncExecutor.newInstance(mClient);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setTitle("加载中...");
        mProgressDialog.setMessage("正在加载，请等待...");
        mProgressDialog.show();
        mHandler.sendEmptyMessage(REQUEST_WARNING_DATA);

        return view;
    }
}
