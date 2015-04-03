package com.zdreamx.gasmonitor;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.TextView;

import com.loveplusplus.update.UpdateChecker;


public class MainActivity extends FragmentActivity implements OnClickListener {

    private FragmentCurrent fgCurrent;
    private FragmentHistory fgHistory;
    private FragmentWarning fgWarning;
    private FragmentSet fgSet;
    private TextView current_text;
    private TextView history_text;
    private TextView warning_text;
    private TextView set_text;
    private RelativeLayout current_layout;
    private RelativeLayout history_layout;
    private RelativeLayout warning_layout;
    private RelativeLayout set_layout;
    private  FragmentManager fragmentmanager;
    //定义要用的颜色值
    private int white = 0xFFFFFFFF;
    private int gray = 0xFF7597B3;
    private int blue = 0xFF0AB2FB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentmanager = this.getFragmentManager();
        initViews();
        setChoiceItem(0); //直接显示实时数据
        //检查最新版本
        UpdateChecker.checkForDialog(MainActivity.this, "http://"+getResources().getString(R.string.server_ip)+getResources().getString(R.string.server_update_file));
        //UpdateChecker.checkForNotification(MainActivity.this, "http://"+getResources().getString(R.string.server_ip)+":8080/static/update.txt");
    }
    //完成组件初始化
    public void initViews() {
        current_text = (TextView) findViewById(R.id.current_text);
        history_text = (TextView) findViewById(R.id.history_text);
        warning_text  = (TextView) findViewById(R.id.warning_text);
        set_text = (TextView) findViewById(R.id.set_text);
        current_layout = (RelativeLayout) findViewById(R.id.current_layout);
        history_layout = (RelativeLayout) findViewById(R.id.history_layout);
        warning_layout = (RelativeLayout) findViewById(R.id.warning_layout);
        set_layout = (RelativeLayout) findViewById(R.id.set_layout);
        current_layout.setOnClickListener(this);
        history_layout.setOnClickListener(this);
        warning_layout.setOnClickListener(this);
        set_layout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.current_layout:
                setChoiceItem(0);
                break;
            case R.id.history_layout:
                setChoiceItem(1);
                break;
            case R.id.warning_layout:
                setChoiceItem(2);
                break;
            case R.id.set_layout:
                setChoiceItem(3);
                break;
        }

    }
    //选择指定的项目
    public void setChoiceItem(int index) {
        //重置碎片+隐藏所有的
        FragmentTransaction transaction = fragmentmanager.beginTransaction();
        clearChoice();//清除选项
        hideFragment(transaction);//隐藏碎片 避免混乱
        switch (index){
            case 0:
                current_text.setTextColor(blue);
                if(fgCurrent==null){ //如果fgCurrent为空 则创建一个并添加到界面上
                    fgCurrent = new FragmentCurrent();
                    transaction.add(R.id.content,fgCurrent);
                } else { //如果不为空 则直接显示出来
                    transaction.show(fgCurrent);
                }
                break;
            case 1:
                history_text.setTextColor(blue);
                if(fgHistory == null){
                    fgHistory = new FragmentHistory();
                    transaction.add(R.id.content,fgHistory);
                } else {
                    transaction.show(fgHistory);
                }
                break;
            case 2:
                warning_text.setTextColor(blue);
                if(fgWarning == null){
                    fgWarning = new FragmentWarning();
                    transaction.add(R.id.content,fgWarning);
                } else {
                    transaction.show(fgWarning);
                }
                break;
            case 3:
                set_text.setTextColor(blue);
                if(fgSet == null) {
                    fgSet = new FragmentSet();
                    transaction.add(R.id.content,fgSet);
                } else {
                    transaction.show(fgSet);
                }
        }
        transaction.commit();
    }
    //隐藏所有的碎片
    private void hideFragment(FragmentTransaction ft) {
        if(fgCurrent != null){
            ft.hide(fgCurrent);
        }
        if(fgHistory != null) {
            ft.hide(fgHistory);
        }
        if(fgWarning != null) {
            ft.hide(fgWarning);
        }
        if(fgSet != null) {
            ft.hide(fgSet);
        }
    }
    //重置所有选项的方法
    public void clearChoice(){
        current_layout.setBackgroundColor(white);
        history_layout.setBackgroundColor(white);
        warning_layout.setBackgroundColor(white);
        set_layout.setBackgroundColor(white);
        current_text.setTextColor(gray);
        history_text.setTextColor(gray);
        warning_text.setTextColor(gray);
        set_text.setTextColor(gray);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
