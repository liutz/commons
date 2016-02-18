package com.huami.android.commons.toolbox;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class TimeUitls {
	
	//毫秒转换为日期
	public static String msConvertDate(String pattern,long timeMillis){		
		DateFormat formatter = new SimpleDateFormat(pattern);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeMillis);
		return formatter.format(calendar.getTime());
	}	
	
	public static String formatTime(long seconds, String pattern) {
        if (seconds <= 0) {
            return null;
        }
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        calendar.setTimeInMillis(seconds * 1000);
        
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        format.setTimeZone(TimeZone.getDefault());

        return format.format(calendar.getTime());
    }
}
