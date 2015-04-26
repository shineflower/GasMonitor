package com.zdreamx.gasmonitor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.litesuits.http.LiteHttpClient;
import com.litesuits.http.async.HttpAsyncExecutor;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.request.Request;
import com.litesuits.http.response.Response;
import com.litesuits.http.response.handler.HttpModelHandler;
import com.zdreamx.gasmonitor.view.OnWheelScrollListener;
import com.zdreamx.gasmonitor.view.ScreenInfo;
import com.zdreamx.gasmonitor.view.WheelMain;
import com.zdreamx.gasmonitor.view.WheelView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Ashia on 2015/4/20.
 */
public class ChooseItemActivity extends ActionBarActivity implements OnWheelScrollListener, View.OnClickListener {
    private Context context;
    private Spinner spinner;
    private EditText startTime, endTime;

    private View timePickerView;
    private WheelMain wheelMain;

    private String spinnerText, mode, mobile;

    private HttpAsyncExecutor asyncExecutor;
    private LiteHttpClient client;

    private static final String DEFAULT_SPINNER_STRING = "点击选择";
    private static final int REQUEST_HISTORY_DATA = 0;

    private int defaultTextColor, second;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REQUEST_HISTORY_DATA:
                    SharedPreferences sp = getSharedPreferences("setting", Context.MODE_PRIVATE);
                    String uname = sp.getString("uname","STRING_NOT_EXIST");
                    if(uname.equals("STRING_NOT_EXIST")) { //如果本地参数不存在则直接到登录页面
                        Intent it = new Intent(context, LoginActivity.class); //需要跳转到登录页面
                        startActivity(it);
                        finish();
                    }
                    String start = startTime.getText().toString();
                    String end = endTime.getText().toString();
                    String history_url = Uri.decode("http://" + getResources().getString(R.string.server_ip) + "/api/getnodedata/gethistorydata?start=" + start + "&end=" + end + "&mobile=" + mobile + "&mode=" + mode);
                    asyncExecutor.execute(new Request(history_url), new HttpModelHandler<Utils.NodeDatasReturnDatas>() {

                        @Override
                        protected void onSuccess(Utils.NodeDatasReturnDatas o, Response response) {
                            if (o.Result) { //返回结果
                                MainApp mainApp = (MainApp) getApplication();
                                mainApp.setDatas(o.Datas);
                                mainApp.setSpinnerText(spinnerText);
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                Toast.makeText(context, "返回结果错误，请重新登录", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        protected void onFailure(HttpException e, Response response) {
                            Toast.makeText(context, "网络访问错",Toast.LENGTH_LONG).show();
                        }
                    });
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_choose_item);
        setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = this;

        spinner = (Spinner) findViewById(R.id.spinner);

        startTime = (EditText) findViewById(R.id.start_time);
        endTime = (EditText) findViewById(R.id.end_time);

        List<String> spinnerList = new ArrayList<String>();
        spinnerList.add(DEFAULT_SPINNER_STRING);
        for (int i = 0; i < FragmentCurrent.list.size(); i++) {
            spinnerList.add(FragmentCurrent.list.get(i).get("nick") + "(" + FragmentCurrent.list.get(i).get("mobile") + ")");
        }

        //适配器
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerList);
        //设置样式
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //加载适配器
        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerText = parent.getItemAtPosition(position).toString();
                if (DEFAULT_SPINNER_STRING.equals(spinnerText)) {
                    mobile = null;
                } else {
                    mobile = spinnerText.substring(spinnerText.indexOf("(") + 1, spinnerText.length() - 1);  //获取手机号
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //设置默认查询类型
        RadioGroup typeRadioGroup = (RadioGroup) findViewById(R.id.type);
        typeRadioGroup.check(R.id.hour);
        mode = "hour";
        typeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId) {
                    case R.id.hour:
                        mode = "hour";
                        break;
                    case R.id.minute:
                        mode = "minute";
                        break;
                    case R.id.second:
                        mode = "second";
                        break;
                }
            }
        });

        View timePickerView = LayoutInflater.from(context).inflate(R.layout.time_picker, null);
        ScreenInfo screenInfo = new ScreenInfo(this);
        wheelMain = new WheelMain(timePickerView, true);
        wheelMain.screenheight = screenInfo.getHeight();
        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        second = calendar.get(Calendar.SECOND);
        wheelMain.initDateTimePicker(year, month, day, hour, minute);

        WheelView wv_year = (WheelView) wheelMain.getView().findViewById(R.id.year);
        WheelView wv_month = (WheelView) wheelMain.getView().findViewById(R.id.month);
        WheelView wv_day = (WheelView) wheelMain.getView().findViewById(R.id.day);
        WheelView wv_hour = (WheelView) wheelMain.getView().findViewById(R.id.hour);
        WheelView wv_min = (WheelView) wheelMain.getView().findViewById(R.id.min);
        wv_year.addScrollingListener(this);
        wv_month.addScrollingListener(this);
        wv_day.addScrollingListener(this);
        wv_hour.addScrollingListener(this);
        wv_min.addScrollingListener(this);

        LinearLayout parent = (LinearLayout) findViewById(R.id.choose_item_parent);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = 150;
        parent.addView(timePickerView, params);

        //默认值设置为当前时间
        startTime.setText(wheelMain.getTime() + ":" + (second < 10 ? "0" + second : second));
        endTime.setText(wheelMain.getTime() + ":" + (second < 10 ? "0" + second : second));

        defaultTextColor = startTime.getCurrentTextColor();

        client = LiteHttpClient.newApacheHttpClient(this);
        asyncExecutor = HttpAsyncExecutor.newInstance(client);

        startTime.setOnClickListener(this);
        endTime.setOnClickListener(this);
    }

    //将字符串类型转化成Date类型
    private long parseStringToDate(String time) {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return sdf.parse(time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_choose_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_search:
                if(mobile == null) {
                    Toast.makeText(this, "请选择查询井口", Toast.LENGTH_LONG).show();
                } else if (parseStringToDate(startTime.getText().toString()) > parseStringToDate(endTime.getText().toString())) {
                    Toast.makeText(this, "开始日期不能晚于截止日期", Toast.LENGTH_LONG).show();
                } else {
                    mHandler.sendEmptyMessage(REQUEST_HISTORY_DATA);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onScrollingStarted(WheelView wheel) {

    }

    @Override
    public void onScrollingFinished(WheelView wheel) {
        if (startTime.getCurrentTextColor() == ViewCurrentActivity.BLUE) {
            startTime.setText(wheelMain.getTime() + ":" + (second < 10 ? "0" + second : second));
        } else if (endTime.getCurrentTextColor() == ViewCurrentActivity.BLUE) {
            endTime.setText(wheelMain.getTime() + ":" + (second < 10 ? "0" + second : second));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_time:
                startTime.setTextColor(ViewCurrentActivity.BLUE);
                endTime.setTextColor(defaultTextColor);
                break;
            case R.id.end_time:
                endTime.setTextColor(ViewCurrentActivity.BLUE);
                startTime.setTextColor(defaultTextColor);
                break;
        }
    }
}
