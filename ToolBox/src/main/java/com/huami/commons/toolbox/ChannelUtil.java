package com.huami.android.commons.toolbox;

import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.text.TextUtils;

public class ChannelUtil {
	
	private static final String CHANNEL_KEY = "hmchannel";
	private static final String DEFAULT_CHANNEL = "inner";
	private static String mChannel;
	
	public static String getChannel(Context context){
		return getChannel(context, "");
	}
	
	public static String getChannel(Context context, String defaultChannel) {
		
		if(!TextUtils.isEmpty(mChannel)){
			return mChannel;
		}		
		
		mChannel = getChannelFromApk(context, CHANNEL_KEY);
		if(!TextUtils.isEmpty(mChannel)){			
			return mChannel;
		}
		
		if(TextUtils.isEmpty(defaultChannel)){
			return DEFAULT_CHANNEL;
		}
		
		return defaultChannel;
    }
	
	/**
	 * ../META-INF/hmchannel_Normal
	 * @param context
	 * @param channelKey
	 * @return
	 */
	private static String getChannelFromApk(Context context, String channelKey) {
        ApplicationInfo appinfo = context.getApplicationInfo();
        String sourceDir = appinfo.sourceDir;
        String key = "META-INF/" + channelKey;
        String ret = "";
        ZipFile zipfile = null;
        try {
            zipfile = new ZipFile(sourceDir);
            Enumeration<?> entries = zipfile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = ((ZipEntry) entries.nextElement());
                String entryName = entry.getName();
                if (entryName.startsWith(key)) {
                    ret = entryName;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (zipfile != null) {
                try {
                    zipfile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        String[] split = ret.split("_");
        String channel = "";
        if (split != null && split.length >= 2) {
        	channel = ret.substring(split[0].length() + 1);
        }
        return channel;
	}
}
