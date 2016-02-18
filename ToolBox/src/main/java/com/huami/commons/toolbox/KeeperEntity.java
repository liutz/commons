package com.huami.android.commons.toolbox;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class KeeperEntity<T> {
	
	private String mPreferencesName = "_Keeper_";
	private Context mContext;
	
	public KeeperEntity(Context context,String preferencesName){
		if(context == null){
			throw new IllegalArgumentException("context should not be null");
		}
		this.mPreferencesName = preferencesName;
		this.mContext = context.getApplicationContext();
	}
	
	public KeeperEntity(Context context){
		if(context == null){
			throw new IllegalArgumentException("context should not be null");
		}
		this.mContext = context.getApplicationContext();
	}
	
	public boolean writeKeeper(String key,T entity) {
		if (null == entity) {
			return false;
	    }	        
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(entity);
        SharedPreferences pref = mContext.getSharedPreferences(mPreferencesName, Context.MODE_APPEND);
        Editor editor = pref.edit();
        editor.putString(key,json);        
        return editor.commit();
	 }
	
	public T readKeeper(String key,Class<T> clazz) {
        if (null == key) {
            return null;
        }
        
        SharedPreferences pref = mContext.getSharedPreferences(mPreferencesName, Context.MODE_APPEND);
        String json = pref.getString(key, "");
        
        if(TextUtils.isEmpty(json)){
        	return null;
        }
        
        T  entity = null;
        try{
        	Gson gson = new GsonBuilder().create();            
            entity = gson.fromJson(json, clazz);
        }catch(Exception e){e.printStackTrace();}
               
        return entity;
	 }
	
	public boolean clear(String key){
		try{
			SharedPreferences pref = mContext.getSharedPreferences(mPreferencesName, Context.MODE_APPEND);
			return pref.edit().remove(key).commit();
			
		}catch(Exception e){}
		return false;
	}
}
