package com.example.administrator.coolweather.util;

/**
 * Created by Ryan on 2016/3/9 0009.
 */
public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);

}
