package com.huami.passport.sample;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.huami.passport.AccountManager;
import com.huami.passport.IAccount;
import com.huami.passport.entity.Token;
import com.huami.passport.entity.TokenInfo;
import com.huami.passport.entity.UserInfo;
import android.text.TextUtils;

public class MainActivity extends AppCompatActivity implements IAccount.Callback<String,String>{

    private static final String TAG = "MainActivity";
    private AccountManager mAccountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mAccountManager = AccountManager.getDefault(this);
        // 登录业务系统,首先检查登录接口
        mAccountManager.checkLogin(new IAccount.Callback<String, String>() {

            @Override
            public void onSuccess(String result) {
                // 登录成功，直接进入业务系统
                Log.i(TAG, "check login " + result);
                Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String code) {
                // 登录错误，请回到登录界面重新登录
                Log.i(TAG, "check login " + code);
                Toast.makeText(MainActivity.this, "用户未登录", Toast.LENGTH_LONG).show();
            }

        });

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

    private String mProvider;

    /** 微信登录 **/
    public void onWeChatLogin(View view){
        mProvider = IAccount.PROVIDER_WECHAT;
        mAccountManager.login(this, mProvider, this);
    }

    /** 小米登录 **/
    public void onXiaoMiLogin(View view){
        mProvider = IAccount.PROVIDER_MI;
        mAccountManager.login(this, false,mProvider, this);
    }

    /** Facebook登录 **/
    public void onFacebookLogin(View view){
        mProvider = IAccount.PROVIDER_FACEBOOK;
        // 演示控制loading态不自动关闭，有时候SDK登录完还有其他业务必要接口请求
        mAccountManager.login(this, mProvider, false, this);
    }

    /** 获取Token **/
    public void onGetToken(View view){
        // Token信息由SDK维护（注意，APP无需维护）
        // 发起业务请求时，务必从SDK中获取token信息(请不要在UI线程执行)

        new Thread(new Runnable(){

            @Override
            public void run() {
                Token token = mAccountManager.getToken();
                final String info = token!= null ? token.toString():"token is null";
                Log.i(TAG,"Get "+info);
                runOnUiThread(new Runnable(){

                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, info, Toast.LENGTH_LONG).show();
                    }
                });
            }

        }).start();


    }

    /** 用户信息 **/
    public void onUserProfile(View view){
        // 获取用户基本信息
        UserInfo userInfo = mAccountManager.getUserInfo();
        String info = userInfo!= null ? userInfo.toString():"userInfo is null";
        Toast.makeText(this, info, Toast.LENGTH_LONG).show();
        Log.i(TAG, info);
    }

    /** 登出接口 **/
    public void onLogout(View view){
        mAccountManager.logout(new IAccount.Callback<String,String>() {

            @Override
            public void onSuccess(String result) {
                // 登出成功
                Log.i(TAG, "logout " + result);
                Toast.makeText(MainActivity.this, "登出成功", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String errorCode) {
                // 登出失败
                Log.e(TAG, "logout " + errorCode);
                Toast.makeText(MainActivity.this, "登出失败 " + errorCode, Toast.LENGTH_LONG).show();
            }

        });
    }

    public void onCloseLoading(View view){
        mAccountManager.closeLoadingUI();
    }

    @Override
    public void onSuccess(String result) {
        // 登录成功
        Log.i(TAG, "login " + result);
        Toast.makeText(this, "登录成功", Toast.LENGTH_LONG).show();

        // Facebook登录渠道演示了，自控制loading态关闭
        if(TextUtils.equals(mProvider,IAccount.PROVIDER_FACEBOOK)){
            // ...很多其他业务请求等
            mAccountManager.closeLoadingUI();
        }
    }

    @Override
    public void onError(String errorCode) {
        Log.e(TAG, "login " + errorCode);
        Toast.makeText(this, "登录失败 "+errorCode, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
    }

    public void onCheckToken(View view){

        // 客户端校验token，可以获取踢下时间
        mAccountManager.getToken(new IAccount.TokenListener<String>(){

            @Override
            public void reslut(String token) {
                Log.d(TAG, "Check token " + token);

                mAccountManager.verifyAccessToken(token,new IAccount.Callback<String,TokenInfo>(){
                    @Override
                    public void onSuccess(String result) {
                        Log.i(TAG,result);
                        Toast.makeText(MainActivity.this,"token ok",Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(TokenInfo error) {
                        Log.e(TAG,String.valueOf(error.mutimeLong));
                        Toast.makeText(MainActivity.this,"offline time "+error.mutimeLong,Toast.LENGTH_LONG).show();
                    }
                });

            }
        });

    }

    public void onRelogin(View view){
        Log.i(TAG, "relogin");
        mAccountManager.relogin(new IAccount.Callback<String,String>(){

            @Override
            public void onSuccess(String result) {
                Log.i(TAG,"relogin "+result);
                Toast.makeText(MainActivity.this, "重新登录成功", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String code) {
                Log.e(TAG,"relogin error "+code);
                Toast.makeText(MainActivity.this, "重新登录error "+code, Toast.LENGTH_LONG).show();
            }

        });
    }
}
