package com.android.volley.toolbox.ext;

import java.util.Map;
import com.android.volley.AuthFailureError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;

public class NormalPostStringRequest extends StringRequest {

	private Map<String, String> params;
	
	public NormalPostStringRequest(int method, String url,Map<String, String> params, Listener<String> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
		this.params = params;
	}
		    
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
    	 return params != null ? params : super.getParams();
    }
}
