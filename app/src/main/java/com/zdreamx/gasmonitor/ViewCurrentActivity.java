package com.zdreamx.gasmonitor;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.litesuits.http.LiteHttpClient;
import com.litesuits.http.async.HttpAsyncExecutor;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.request.Request;
import com.litesuits.http.response.Response;
import com.litesuits.http.response.handler.HttpModelHandler;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

public class ViewCurrentActivity extends ActionBarActivity implements View.OnClickListener {
    private String mobile; //号码
    private String nick; //名称
    private LiteHttpClient client;
    private HttpAsyncExecutor asyncExecutor;
    private String apiUrl; //api请求url
    private Context context;
    private TextView mMytime, mWellpress, mTaopress, mLiquidhigh, mGasstandflow, mGastotalflow, mGaspress, mSpeed, mMotortemp, mMotordcbus;
    private LineChartView mLineChartView;
    private Utils.NodeDatasReturnDatas  mNodeDatasReturnDatas;
    List<PointValue> mPointValues = new ArrayList<PointValue>();
    List<AxisValue> mAxisValues = new ArrayList<AxisValue>();
    private int blue = 0xFF0AB2FB;
    private int mDefaultTextColor;
    private ProgressDialog mProgressDialog;
    private boolean mIsRefreshing = false;  //是否刷新
    private static final  int WELLPRESS = 0;
    private static final  int TAOPRESS = 1;
    private static final  int LIQUIDHIGH = 2;
    private static final  int GASSTANDFLOW = 3;
    private static final  int GASTOTALFLOW = 4;
    private static final  int GASPRESS = 5;
    private static final  int SPEED = 6;
    private static final  int MOTORTEMP = 7;
    private static final  int MOTORDCBUS = 8;
    private int mCurrentChartIndex = WELLPRESS;  //标记当前的折线图

    private Handler myHandler = new Handler(){ //这种方式可能会导致内存泄露，暂时不处理，以后统一解决
        @Override
        public void handleMessage(Message msg) {
            asyncExecutor.execute(new Request(apiUrl),new HttpModelHandler<Utils.NodeDatasReturnDatas>() {
                @Override
                protected void onSuccess(final Utils.NodeDatasReturnDatas o, Response response) {
                    if (o.Result) { //返回结果正确
                        if (o.Count > 0) { //有数据

                            mProgressDialog.dismiss();

                            mNodeDatasReturnDatas = o;

                            showLatestStatus();

                            if (mIsRefreshing) {
                                refreshCurrentLineChartView();  //更新当前折线图
                                return;
                            }

                            //填充折线图数据
                            //开始默认只显示井底压力折线
                            for (int i = 0; i < o.Count; i++) {
                                mAxisValues.add(new AxisValue(i).setLabel(o.Datas[o.Count - 1 - i].Mytime)); //为每个对应的i（X轴显示的）设置相应的label
                                mPointValues.add(new PointValue(i, o.Datas[o.Count - 1 - i].Data.Wellpress));
                                showLineChartView();
                                mWellpress.setTextColor(blue);
                            }
                        }
                    }
                }

                @Override
                protected void onFailure(HttpException e, Response response) {
                    mProgressDialog.dismiss();
                    Toast.makeText(context, "请求数据发送错误", Toast.LENGTH_LONG).show();
                }
            });
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_current);
        Intent intent = getIntent();
        mobile = intent.getStringExtra("mobile"); //提前号码和昵称
        nick = intent.getStringExtra("nick");
        setTitle(nick);
        client = LiteHttpClient.newApacheHttpClient(this);
        asyncExecutor = HttpAsyncExecutor.newInstance(client);
        apiUrl = "http://"+getResources().getString(R.string.server_ip)+"/api/getnodedata/getdatas?mobile="+mobile;
        context = this;

        mMytime = (TextView) findViewById(R.id.Mytime);
        mWellpress = (TextView) findViewById(R.id.Wellpress);
        mTaopress = (TextView) findViewById(R.id.Taopress);
        mLiquidhigh = (TextView) findViewById(R.id.Liquidhigh);
        mGasstandflow = (TextView) findViewById(R.id.Gasstandflow);
        mGastotalflow = (TextView) findViewById(R.id.Gastotalflow);
        mGaspress = (TextView) findViewById(R.id.Gaspress);
        mSpeed = (TextView) findViewById(R.id.Speed);
        mMotortemp = (TextView) findViewById(R.id.Motortemp);
        mMotordcbus = (TextView) findViewById(R.id.Motordcbus);
        mLineChartView = (LineChartView) findViewById(R.id.chart);

        mWellpress.setOnClickListener(this);
        mTaopress.setOnClickListener(this);
        mLiquidhigh.setOnClickListener(this);
        mGasstandflow.setOnClickListener(this);
        mGastotalflow.setOnClickListener(this);
        mGaspress.setOnClickListener(this);
        mSpeed.setOnClickListener(this);
        mMotortemp.setOnClickListener(this);
        mMotordcbus.setOnClickListener(this);

        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setTitle("加载中...");
        mProgressDialog.setMessage("正在加载，请等待...");
        mProgressDialog.show();

        myHandler.sendEmptyMessage(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_current, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            //刷新
            mProgressDialog.show();

            mIsRefreshing = true;

            myHandler.sendEmptyMessage(0);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void refreshCurrentLineChartView() {
        mLineChartView.setVisibility(View.GONE);
        mPointValues.clear();
        mAxisValues.clear();
        for (int i = 0; i < mNodeDatasReturnDatas.Count; i++) {
            mAxisValues.add(new AxisValue(i).setLabel(mNodeDatasReturnDatas.Datas[mNodeDatasReturnDatas.Count - 1 -i].Mytime));
        }
        switch (mCurrentChartIndex) {
            case WELLPRESS:
                for (int i = 0; i < mNodeDatasReturnDatas.Count; i++) {
                    mPointValues.add(new PointValue(i, mNodeDatasReturnDatas.Datas[mNodeDatasReturnDatas.Count - 1 - i].Data.Wellpress));
                }
                showLineChartView();
                cleanAllTextColor();
                mWellpress.setTextColor(blue);
                break;
            case TAOPRESS:
                for (int i = 0; i < mNodeDatasReturnDatas.Count; i++) {
                    mPointValues.add(new PointValue(i, mNodeDatasReturnDatas.Datas[mNodeDatasReturnDatas.Count - 1 - i].Data.Taopress));
                }
                showLineChartView();
                cleanAllTextColor();
                mTaopress.setTextColor(blue);
                break;
            case LIQUIDHIGH:
                for (int i = 0; i < mNodeDatasReturnDatas.Count; i++) {
                    mPointValues.add(new PointValue(i, mNodeDatasReturnDatas.Datas[mNodeDatasReturnDatas.Count - 1 - i].Data.Liquidhigh));
                }
                showLineChartView();
                cleanAllTextColor();
                mLiquidhigh.setTextColor(blue);
                break;
            case GASSTANDFLOW:
                for (int i = 0; i < mNodeDatasReturnDatas.Count; i++) {
                    mPointValues.add(new PointValue(i, mNodeDatasReturnDatas.Datas[mNodeDatasReturnDatas.Count -1 - i].Data.Gasstandflow));
                }
                showLineChartView();
                cleanAllTextColor();
                mGasstandflow.setTextColor(blue);
                break;
            case GASTOTALFLOW:
                for (int i = 0; i < mNodeDatasReturnDatas.Count; i++) {
                    mPointValues.add(new PointValue(i, mNodeDatasReturnDatas.Datas[mNodeDatasReturnDatas.Count -1 - i].Data.Gastotalflow));
                }
                showLineChartView();
                cleanAllTextColor();
                mGastotalflow.setTextColor(blue);
                break;
            case GASPRESS:
                for (int i = 0; i < mNodeDatasReturnDatas.Count; i++) {
                    mPointValues.add(new PointValue(i, mNodeDatasReturnDatas.Datas[mNodeDatasReturnDatas.Count -1 - i].Data.Gaspress));
                }
                showLineChartView();
                cleanAllTextColor();
                mGaspress.setTextColor(blue);
                break;
            case SPEED:
                for (int i = 0; i < mNodeDatasReturnDatas.Count; i++) {
                    mPointValues.add(new PointValue(i, mNodeDatasReturnDatas.Datas[mNodeDatasReturnDatas.Count -1 - i].Data.Speed));
                }
                showLineChartView();
                cleanAllTextColor();
                mSpeed.setTextColor(blue);
                break;
            case MOTORTEMP:
                for (int i = 0; i < mNodeDatasReturnDatas.Count; i++) {
                    mPointValues.add(new PointValue(i, mNodeDatasReturnDatas.Datas[mNodeDatasReturnDatas.Count -1 - i].Data.Motortemp));
                }
                showLineChartView();
                cleanAllTextColor();
                mMotortemp.setTextColor(blue);
                break;
            case MOTORDCBUS:
                for (int i = 0; i < mNodeDatasReturnDatas.Count; i++) {
                    mPointValues.add(new PointValue(i, mNodeDatasReturnDatas.Datas[mNodeDatasReturnDatas.Count -1 - i].Data.Motordcbus));
                }
                showLineChartView();
                cleanAllTextColor();
                mMotordcbus.setTextColor(blue);
                break;
        }

        mIsRefreshing = false;
    }

    //显示最新的状态
    private void showLatestStatus() {
        mMytime.setText("更新时间：" + String.valueOf(mNodeDatasReturnDatas.Datas[0].Mytime));
        mWellpress.setText("井底压力：" + String.valueOf(mNodeDatasReturnDatas.Datas[0].Data.Wellpress));
        mTaopress.setText("套压：" + String.valueOf(mNodeDatasReturnDatas.Datas[0].Data.Taopress));
        mLiquidhigh.setText("液柱高度：" + String.valueOf(mNodeDatasReturnDatas.Datas[0].Data.Liquidhigh));
        mGasstandflow.setText("气体流量：" + String.valueOf(mNodeDatasReturnDatas.Datas[0].Data.Gasstandflow));
        mGastotalflow.setText("气体总量：" + String.valueOf(mNodeDatasReturnDatas.Datas[0].Data.Gastotalflow));
        mGaspress.setText("气体压力：" + String.valueOf(mNodeDatasReturnDatas.Datas[0].Data.Gaspress));
        mSpeed.setText("运行频率：" + String.valueOf(mNodeDatasReturnDatas.Datas[0].Data.Speed));
        mMotortemp.setText("设定频率：" + String.valueOf(mNodeDatasReturnDatas.Datas[0].Data.Motortemp));
        mMotordcbus.setText("母线电压：" + String.valueOf(mNodeDatasReturnDatas.Datas[0].Data.Motordcbus));

        //获取文本的默认颜色
        if (!mIsRefreshing) {
            mDefaultTextColor = mWellpress .getCurrentTextColor();
        }

    }

    private void showLineChartView() {
        Line line = new Line(mPointValues).setColor(blue).setCubic(false);
        List<Line> lines = new ArrayList<Line>();
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);

        //坐标轴
        Axis axisX = new Axis(); //X轴
        axisX.setHasTiltedLabels(true);
        axisX.setTextColor(blue);
        axisX.setName("采集时间");
        axisX.setMaxLabelChars(10);
        axisX.setValues(mAxisValues);
        data.setAxisXBottom(axisX);

        Axis axisY = new Axis();  //Y轴
        axisY.setMaxLabelChars(7); //默认是3，只能看最后三个数字
        axisY.setTextColor(blue); //跟折线的颜色保持一致
        data.setAxisYLeft(axisY);

        //设置行为属性，支持缩放、滑动以及平移
        mLineChartView.setInteractive(true);
        mLineChartView.setZoomType(ZoomType.HORIZONTAL);
        mLineChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);

        mLineChartView.setLineChartData(data);
        mLineChartView.setVisibility(View.VISIBLE);
    }

    //清除文本的颜色
    private void cleanAllTextColor() {
        mWellpress.setTextColor(mDefaultTextColor);
        mTaopress.setTextColor(mDefaultTextColor);
        mLiquidhigh.setTextColor(mDefaultTextColor);
        mGasstandflow.setTextColor(mDefaultTextColor);
        mGastotalflow.setTextColor(mDefaultTextColor);
        mGaspress.setTextColor(mDefaultTextColor);
        mSpeed.setTextColor(mDefaultTextColor);
        mMotortemp.setTextColor(mDefaultTextColor);
        mMotordcbus.setTextColor(mDefaultTextColor);
    }

    @Override
    public void onClick(View v) {
        mLineChartView.setVisibility(View.GONE);
        mPointValues.clear();
        switch (v.getId()) {
            case R.id.Wellpress:
                for (int i = 0; i < mNodeDatasReturnDatas.Count; i++) {
                    mPointValues.add(new PointValue(i, mNodeDatasReturnDatas.Datas[mNodeDatasReturnDatas.Count - 1 - i].Data.Wellpress));
                }
                mCurrentChartIndex = WELLPRESS;
                break;
            case R.id.Taopress:
                for (int i = 0; i < mNodeDatasReturnDatas.Count; i++) {
                    mPointValues.add(new PointValue(i, mNodeDatasReturnDatas.Datas[mNodeDatasReturnDatas.Count - 1 - i].Data.Taopress));
                }
                mCurrentChartIndex = TAOPRESS;
                break;
            case R.id.Liquidhigh:
                for (int i = 0; i < mNodeDatasReturnDatas.Count; i++) {
                    mPointValues.add(new PointValue(i, mNodeDatasReturnDatas.Datas[mNodeDatasReturnDatas.Count - 1 - i].Data.Liquidhigh));
                }
                mCurrentChartIndex = LIQUIDHIGH;
                break;
            case R.id.Gasstandflow:
                for (int i = 0; i < mNodeDatasReturnDatas.Count; i++) {
                    mPointValues.add(new PointValue(i, mNodeDatasReturnDatas.Datas[mNodeDatasReturnDatas.Count - 1 - i].Data.Gasstandflow));
                }
                mCurrentChartIndex = GASSTANDFLOW;
                break;
            case R.id.Gastotalflow:
                for (int i = 0; i < mNodeDatasReturnDatas.Count; i++) {
                    mPointValues.add(new PointValue(i, mNodeDatasReturnDatas.Datas[mNodeDatasReturnDatas.Count - 1 - i].Data.Gastotalflow));
                }
                mCurrentChartIndex = GASTOTALFLOW;
                break;
            case R.id.Gaspress:
                for (int i = 0; i < mNodeDatasReturnDatas.Count; i++) {
                    mPointValues.add(new PointValue(i, mNodeDatasReturnDatas.Datas[mNodeDatasReturnDatas.Count - 1 - i].Data.Gaspress));
                }
                mCurrentChartIndex = GASPRESS;
                break;
            case R.id.Speed:
                for (int i = 0; i < mNodeDatasReturnDatas.Count; i++) {
                    mPointValues.add(new PointValue(i, mNodeDatasReturnDatas.Datas[mNodeDatasReturnDatas.Count - 1 - i].Data.Speed));
                }
                mCurrentChartIndex = SPEED;
                break;
            case R.id.Motortemp:
                for (int i = 0; i < mNodeDatasReturnDatas.Count; i++) {
                    mPointValues.add(new PointValue(i, mNodeDatasReturnDatas.Datas[mNodeDatasReturnDatas.Count - 1 - i].Data.Motortemp));
                }
                mCurrentChartIndex = MOTORTEMP;
                break;
            case R.id.Motordcbus:
                for (int i = 0; i < mNodeDatasReturnDatas.Count; i++) {
                    mPointValues.add(new PointValue(i, mNodeDatasReturnDatas.Datas[mNodeDatasReturnDatas.Count - 1 - i].Data.Motordcbus));
                }
                mCurrentChartIndex = MOTORDCBUS;
                break;
        }

        updateLineChartView(v.getId());
    }

    private void updateLineChartView(int id) {
        showLineChartView();
        cleanAllTextColor();
        ((TextView)findViewById(id)).setTextColor(blue);
    }
}
