package com.android.volley.toolbox.ext;

import android.text.TextUtils;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RestfulRequest extends Request<NetworkResponse> {

    private final Response.Listener<NetworkResponse> mListener;
    private Map<String, String> headers;
    private Map<String, String> params;
    private MultiValueMap<String, Object> mParamKeyValues;

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setParams(Map<String, String> params) {
        if(mParamKeyValues != null && !mParamKeyValues.isEmpty()){
            throw new IllegalArgumentException("AddParams method has been used, " +
                    "please do not use the setParams method");
        }
        this.params = params;
    }

    public void addParams(String key,String value){
        if(mParamKeyValues == null){
            mParamKeyValues = new LinkedMultiValueMap<>();
        }
        if (!TextUtils.isEmpty(key)) {
            mParamKeyValues.add(key, value == null ? "" : value);
        }
    }

    public byte[] getBody() throws AuthFailureError {
        if(mParamKeyValues == null || mParamKeyValues.isEmpty()){
            byte[] data = super.getBody();
            if(VolleyLog.DEBUG){
                try{
                    String body = new String(data,getParamsEncoding());
                    VolleyLog.d("getBody："+body);
                }catch (Exception e){}
            }
            return data;
        }
        StringBuilder paramBuilder = buildCommonParams(mParamKeyValues, getParamsEncoding());
        try{
            String data = paramBuilder.toString();
            if(VolleyLog.DEBUG){
                VolleyLog.d("getBody:"+data);
            }
            return data.getBytes(getParamsEncoding());
        }catch (UnsupportedEncodingException uee){
            uee.printStackTrace();
        }

        return null;
    }

    /**
     * Split joint non form data.
     *
     * @param paramMap      param map.
     * @param encodeCharset charset.
     * @return string parameter combination, each key value on nails with {@code "&"} space.
     */
    public static StringBuilder buildCommonParams(MultiValueMap<String, Object> paramMap, String encodeCharset) {
        StringBuilder paramBuilder = new StringBuilder();
        Set<String> keySet = paramMap.keySet();
        for (String key : keySet) {
            List<Object> values = paramMap.getValues(key);
            for (Object value : values) {
                if (value != null && value instanceof CharSequence) {
                    paramBuilder.append("&");
                    try {
                        paramBuilder.append(URLEncoder.encode(key, encodeCharset));
                        paramBuilder.append("=");
                        paramBuilder.append(URLEncoder.encode(value.toString(), encodeCharset));
                    } catch (UnsupportedEncodingException e) {
                        VolleyLog.e("Encoding " + encodeCharset + " format is not supported by the system");
                        paramBuilder.append(key);
                        paramBuilder.append("=");
                        paramBuilder.append(value.toString());
                    }
                }
            }
        }
        if (paramBuilder.length() > 0)
            paramBuilder.deleteCharAt(0);
        return paramBuilder;
    }

    @Override
    protected Map<String, String> getParams()throws AuthFailureError {
        return params != null ? params : super.getParams();
    }

    public RestfulRequest(int method, String url, Response.Listener<NetworkResponse> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        // 成功状态码范围：statusCode>= 200 && statusCode <= 299
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
    }
}