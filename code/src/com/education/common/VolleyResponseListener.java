package com.education.common;

import android.content.Context;
import android.text.TextUtils;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.Response;

/**
 * Created by su on 15-3-30.
 */
public abstract class VolleyResponseListener implements Response.Listener<JSONObject> {

    private Context mContext;
    private String mUrl;

    public VolleyResponseListener(Context context) {
        this.mContext = context;
    }

    public VolleyResponseListener(String url, Context context) {
        this.mContext = context;
        this.mUrl = url;
    }

    /*
        * 302 重新登录
        * 699 系统维护
        * */
    public void onResponse(JSONObject response) {
        String error = response.getString("error");
        onSuccessfulResponse(response, TextUtils.isEmpty(error));
    }

    public abstract void onSuccessfulResponse(JSONObject response, boolean success);
}
