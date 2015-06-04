package com.education.common;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.Response;
import com.education.EduApp;
import com.education.R;
import com.education.EduApp;

/**
 * Created by su on 15-3-30.
 */
public abstract class SydVolleyResponseListener implements Response.Listener<JSONObject> {

    private Context mContext;
    private String mUrl;

    public SydVolleyResponseListener(Context context) {
        this.mContext = context;
    }

    public SydVolleyResponseListener(String url, Context context) {
        this.mContext = context;
        this.mUrl = url;
    }

    /*
        * 302 重新登录
        * 699 系统维护
        * */
    public void onResponse(JSONObject response) {
        onSuccessfulResponse(response);
    }

    public abstract void onSuccessfulResponse(JSONObject response);
}
