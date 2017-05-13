package com.techgig.wallet;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by GokulSiva on 09-05-2017.
 */

public class VolleyRequest {

    private Context context;
    private String URL;
    private HashMap<String, String> map;
    private String json;

    public VolleyRequest(Context context, String URL, HashMap<String, String> map) {
        this.context = context;
        this.URL = URL;
        this.map = map;
    }

    public void jsonStringRequest(final VolleyCallback callback){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        json = response;
                        Log.v("Response : ",response);
                        callback.onSuccess(json);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            json = context.getString(R.string.Timeout_Error);
                            callback.onSuccess(json);
                        } else if (error instanceof AuthFailureError) {
                            json = context.getString(R.string.Auth_Error);
                            callback.onSuccess(json);
                        } else if (error instanceof ServerError) {
                            json = context.getString(R.string.Server_Error);
                            callback.onSuccess(json);
                        } else if (error instanceof NetworkError) {
                            json = context.getString(R.string.Network_Error);
                            callback.onSuccess(json);
                        } else if (error instanceof ParseError) {
                            json = context.getString(R.string.Parse_Error);
                            callback.onSuccess(json);
                        }

                    }
                }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = map;
                return params;
            }
        };


        RequestQueue requestQueue = Volley.newRequestQueue(context, new HurlStack());
        requestQueue.add(stringRequest);
    }

}
