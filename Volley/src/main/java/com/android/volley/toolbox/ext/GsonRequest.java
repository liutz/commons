package com.android.volley.toolbox.ext;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class GsonRequest<T> extends Request<T> {
	
	private final Gson gson = new Gson();
    private final Class<T> clazz;
    private Map<String, String> headers;
    private final Listener<T> listener;
    private Map<String, String> params;    
    
    public GsonRequest(int method,String url, Map<String, String> params,Class<T> clazz,
            Listener<T> listener, ErrorListener errorListener) {
        super(method, url, errorListener);
        this.clazz = clazz;
        this.params = params;
        this.listener = listener;
    }

	public GsonRequest(int method, String url, Class<T> clazz, Listener<T> listener, ErrorListener errorListener) {
        super(method, url, errorListener);
        this.clazz = clazz;
        this.listener = listener;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }
    
    public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}   
    
    @Override
	protected Map<String, String> getParams()throws AuthFailureError {		
        return params != null ? params : super.getParams();
	}
    
    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
    	
        try {
            String json = new String(response.data,HttpHeaderParser.parseCharset(response.headers));
            try{
            	if(VolleyLog.DEBUG){
                	VolleyLog.d(json);
                }
            }catch(Exception e){e.printStackTrace();}
            return Response.success(gson.fromJson(json, clazz),HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }
}