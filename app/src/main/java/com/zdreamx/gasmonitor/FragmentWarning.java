package com.zdreamx.gasmonitor;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.litesuits.http.LiteHttpClient;
import com.litesuits.http.async.HttpAsyncExecutor;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.request.Request;
import com.litesuits.http.response.Response;
import com.litesuits.http.response.handler.HttpModelHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private Utils.ApiJsonWarnLog[] info;

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

                                    info = o.Info;

                                    ListAdapter adapter = new WarnLogAdapter();
                                    mListView.setAdapter(adapter);
                                    mProgressDialog.dismiss();

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_view_current, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_refresh:
                // 刷新
                mHandler.sendEmptyMessage(REQUEST_WARNING_DATA);
                mProgressDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

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

    public class WarnLogAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return info.length;
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            if (info[i].Logs == 0) {
                view = View.inflate(mContext, R.layout.node_no_warn_data, null);
                TextView tv_nick = (TextView) view.findViewById(R.id.nick);
                TextView tv_mobile = (TextView) view.findViewById(R.id.mobile);

                tv_nick.setText(info[i].Nick);
                tv_mobile.setText(info[i].Mobile);
            } else {
                view = View.inflate(mContext, R.layout.node_warn_data, null);

                TextView tv_nick = (TextView) view.findViewById(R.id.nick);
                TextView tv_mobile = (TextView) view.findViewById(R.id.mobile);
                TextView tv_log = (TextView) view.findViewById(R.id.logs);
                TextView tv_trigger = (TextView) view.findViewById(R.id.trigger);
                TextView tv_message = (TextView) view.findViewById(R.id.message);

                tv_nick.setText(info[i].Nick);
                tv_mobile.setText(info[i].Mobile);
                tv_log.setText("报警次数：" + info[i].Logs);
                tv_trigger.setText("触发条件：" + info[i].LatestLog.Trigger);
                tv_message.setText(info[i].LatestLog.Message);

            }
            return view;
        }
    }
}
