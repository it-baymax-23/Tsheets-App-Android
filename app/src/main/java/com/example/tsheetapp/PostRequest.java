package com.example.tsheetapp;

import com.android.volley.Response;
import com.android.volley.request.StringRequest;


import java.util.Map;

public class PostRequest extends StringRequest {

    private Map<String,String> parameter;
    public PostRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener, Map<String,String> params) {
        super(method, url, listener, errorListener);
        parameter = params;

    }

    @Override
    public Map<String,String> getParams()
    {
        return parameter;
    }
}
