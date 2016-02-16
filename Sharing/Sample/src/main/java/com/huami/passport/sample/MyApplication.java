package com.huami.passport.sample;

import android.app.Application;
import com.facebook.FacebookSdk;
import com.huami.passport.AccountManager;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
        AccountManager.getDefault(this).setTestMode(true);
    }
}
