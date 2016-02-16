package com.huami.passport.sample.wxapi;

import com.huami.passport.WXEntryProxyActivity;
import android.content.Intent;
import android.os.Bundle;
/**
 * 配置登录Loading态UI，实现规范如下：
 * 实现类全名：应用包名[packageName].wxapi.WXEntryActivity
 * WXEntryActivity必须继承WXEntryProxyActivity
 * WXEntryActivity需要做成Loading态，每个APP根据自身主题进行融合
 */
public class WXEntryActivity extends WXEntryProxyActivity {
	
	private static final String TAG = "WXEntryActivity";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }  
}
