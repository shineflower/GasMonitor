package com.zdreamx.gasmonitor;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.litesuits.http.LiteHttpClient;
import com.litesuits.http.async.HttpAsyncExecutor;
import com.litesuits.http.exception.HttpException;
import com.litesuits.http.request.Request;
import com.litesuits.http.response.Response;
import com.litesuits.http.response.handler.HttpModelHandler;
import com.zdreamx.gasmonitor.util.PreferenceUtil;

import cn.jpush.android.api.JPushInterface;

/**
 * 登录页面
 * Created by zdreamx on 2015/3/29.
 */
public class LoginActivity extends Activity {
    private LiteHttpClient client;
    private Context mContext;
    private HttpAsyncExecutor asyncExecutor;
    private EditText mUsernameEditText;
    private EditText mPwdEditText;

    private String mUsername;
    private String mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mContext = this;
        mUsernameEditText = (EditText)findViewById(R.id.et_username);
        mPwdEditText = (EditText) findViewById(R.id.et_password);

        client = LiteHttpClient.newApacheHttpClient(mContext);
        asyncExecutor = HttpAsyncExecutor.newInstance(client);

    }



    public void login(View view) {
        mUsername = mUsernameEditText.getText().toString().trim();
        mPassword = mPwdEditText.getText().toString().trim();
        if (TextUtils.isEmpty(mUsername) || TextUtils.isEmpty(mPassword)) {
            Toast.makeText(mContext, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
            return;
        }

        String loginUrl = buildLoginUrl(mContext,mUsername, mPassword);
        asyncExecutor.execute(new Request(loginUrl),new HttpModelHandler<apiLoginReturn>() {
            @Override
            protected void onSuccess(apiLoginReturn o, Response response) {
                if(o.Authorize){
                    Toast.makeText(mContext,"验证通过",Toast.LENGTH_LONG).show();

                    SharedPreferences sp = mContext.getSharedPreferences("setting", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("uname",mUsername);
                    editor.putString("passwd",mPassword);
                    editor.commit();
                    Intent intent = new Intent(mContext,MainActivity.class);

                    if (PreferenceUtil.isUserExist(mContext) && PreferenceUtil.getPushSettings(mContext)) {
                        JPushInterface.resumePush(getApplicationContext());
                    } else {
                        JPushInterface.stopPush(getApplicationContext());
                    }

                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(mContext,"验证失败",Toast.LENGTH_LONG).show();
                }
            }
            @Override
            protected void onFailure(HttpException e, Response response) {
                Toast.makeText(mContext,"验证失败",Toast.LENGTH_LONG).show();
            }
        });
    }

    public static class apiLoginReturn {
        public boolean Authorize; //验证结果
        public String Version; //版本号
    }

    public static String buildLoginUrl(Context context, String username, String password) {
        return "http://" + context.getResources().getString(R.string.server_ip) + "/api/login?uname=" + username + "&passwd=" + password;
    }

}
