package com.android.volley.toolbox.ext;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import android.content.Context;

public class RequestManager {

	private static volatile RequestManager mDefaultInstance;
	private final Context mContext;
	private RequestQueue mRequestQueue;
	
	private RequestManager(Context context){
		mContext = context;
    	mRequestQueue = getRequestQueue();
    }
	
	public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext);
        }
        return mRequestQueue;
    }
	
	public static RequestManager getDefault(Context context){
		if(mDefaultInstance == null){
   		 synchronized (RequestManager.class) {
                if (mDefaultInstance == null) {
                	context.getApplicationContext();
                	mDefaultInstance = new RequestManager(context);
                }
            }
		}
       return mDefaultInstance;
	}
	
	public <T> void add(Request<T> req) {
        getRequestQueue().add(req);
    }
}
