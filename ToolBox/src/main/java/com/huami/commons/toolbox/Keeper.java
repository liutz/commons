
package com.huami.android.commons.toolbox;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * 使用Preference来管理配置
 */
public class Keeper {
	
	/**
	 * 设置Key对应的String值 
	 * @param fileName 指定配置保存文件
	 */
	public static boolean setStringValue(Context context, String key,String value) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);		
		Editor editor = prefs.edit();
		editor.putString(key, value);
		return editor.commit();
	}
	
	/**
	 * 设置Key对应的Boolean值 
	 * @param fileName 指定配置保存文件
	 */
	public static boolean setBooleanValue(Context context, String key,boolean value) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);		
		Editor editor = prefs.edit();
		editor.putBoolean(key, value);
		return editor.commit();
	}
	
	/**
	 * 设置Key对应的Long值 
	 * @param fileName 指定配置保存文件
	 */
	public static boolean setLongValue(Context context, String key,long value) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);		
		Editor editor = prefs.edit();
		editor.putLong(key, value);
		return editor.commit();
	}
	
	/**
	 * 设置Key对应的int值 
	 * @param fileName 指定配置保存文件
	 */
	public static boolean setIntValue(Context context, String key,int value) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);		
		Editor editor = prefs.edit();
		editor.putInt(key, value);
		return editor.commit();
	}
	
	/**
	 * 获得Key对应的String值
	 * 默认返回空
	 */
	public static String getStringValue(Context context, String key) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getString(key, "");
	}
	
	/**
	 * 获得Key对应的Long值
	 * @param context
	 * @param key
	 * @return
	 */
	public static Long getLongValue(Context context,String key){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getLong(key, 0);
	}
	
	/**
	 * 获得Key对应的int值
	 * @param context
	 * @param key
	 * @return
	 */
	public static int getIntValue(Context context,String key){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getInt(key, 0);
	}
	
	/**
	 * 获得Key对应的String值
	 * 默认返false
	 */
	public static boolean getBooleanValue(Context context, String key) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(key, false);
	}

	/**
	 * 删除Key对应的值
	 * 默认返回空
	 */
	public static boolean removeValue(Context context, String key) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = prefs.edit();
		editor.remove(key);
		return editor.commit();
	}

}
