package com.zdreamx.gasmonitor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

import com.litesuits.http.LiteHttpClient;
import com.litesuits.http.async.HttpAsyncExecutor;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.request.Request;
import com.litesuits.http.response.Response;
import com.litesuits.http.response.handler.HttpModelHandler;
import com.zdreamx.gasmonitor.util.PreferenceUtil;

import cn.jpush.android.api.JPushInterface;

public class LogoActivity extends Activity {
    private LiteHttpClient client;
    private Context context;
    private HttpAsyncExecutor asyncExecutor;
    private Handler myCheckHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            //验证用户名密码
            checkUserAndPassword();
        };
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);
        TextView ver = (TextView) findViewById(R.id.textVersion);
        ver.setText("版本号:"+ Utils.getVersionName(this));

        //设置网络请求参数
        context=this;
        client = LiteHttpClient.newApacheHttpClient(context);
        asyncExecutor = HttpAsyncExecutor.newInstance(client);
        //验证用户
        myCheckHandler.sendEmptyMessageDelayed(0,1000);//1s后验证
    }
    //检查用户名和密码
    private void checkUserAndPassword() {
        //获取本地存在的用户名和密码 密码是明文方式保存 由于程序不对外公开 基于方便性考虑 暂时这样操作
        SharedPreferences sp = context.getSharedPreferences("setting",MODE_PRIVATE);
        String uname = sp.getString("uname","STRING_NOT_EXIST");
        String passwd = sp.getString("passwd","STRING_NOT_EXIST");
        if(uname == "STRING_NOT_EXIST" || passwd == "STRING_NOT_EXIST") { //如果本地参数不存在则直接到登录页面
            Intent it = new Intent(LogoActivity.this,LoginActivity.class); //需要跳转到登录页面
            startActivity(it);
            finish();
            return;
        }
        String loginUrl="http://"+getResources().getString(R.string.server_ip)+"/api/login?uname=" + uname + "&passwd=" + passwd;
        asyncExecutor.execute(new Request(loginUrl),new HttpModelHandler<apiLoginReturn>() {
            @Override
            protected void onSuccess(apiLoginReturn o, Response response) {
                if(o.Authorize){
                    Toast.makeText(LogoActivity.this,"验证通过",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(LogoActivity.this,MainActivity.class);

                    if (PreferenceUtil.isUserExist(context) && PreferenceUtil.getPushSettings(context)) {
                        JPushInterface.resumePush(getApplicationContext());
                    } else {
                        JPushInterface.stopPush(getApplicationContext());
                    }

                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LogoActivity.this,"验证失败",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(LogoActivity.this,LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
            @Override
            protected void onFailure(HttpException e, Response response) {
                Toast.makeText(LogoActivity.this,"验证连接失败,请检查网络连接情况",Toast.LENGTH_LONG).show();
                //也到登录页面
                Intent intent = new Intent(LogoActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    public static class apiLoginReturn {
        public boolean Authorize; //验证结果
        public String Version; //版本号
    }

    @Override
    protected void onResume() {
        super.onResume();
        JPushInterface.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        JPushInterface.onPause(this);
    }
}
