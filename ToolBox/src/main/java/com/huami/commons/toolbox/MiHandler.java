package com.huami.android.commons.toolbox;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

public class MiHandler extends Handler {

    private static MiHandler mInstance = new MiHandler(Looper.getMainLooper());

    private Context mContext;

    public MiHandler(Looper mainLooper) {
        super(mainLooper);
    }

    public static MiHandler getInstance() {
        return mInstance;
    }

    public static void onDestroy() {
        mInstance = null;
    }

    public static Context getActivityContext() {
        return getInstance().mContext;
    }

    public static void setActivityContext(Context context) {
        setActivityContext(context, false);
    }

    public static void setActivityContext(Context context, boolean force) {
        if (force || getInstance().mContext == null || !(getInstance().mContext instanceof Activity)) {
            getInstance().mContext = context;
        }
    }

};
