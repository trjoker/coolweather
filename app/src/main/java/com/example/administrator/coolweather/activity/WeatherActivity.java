package com.example.administrator.coolweather.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.administrator.coolweather.R;
import com.example.administrator.coolweather.util.HttpCallbackListener;
import com.example.administrator.coolweather.util.HttpUtil;
import com.example.administrator.coolweather.util.Utility;

/**
 * Created by Ryan on 2016/3/9 0009.
 */
public class WeatherActivity extends Activity implements View.OnClickListener {
    private LinearLayout weatherInfolayout;

    /**
     * 城市名
     */
    private TextView cityNameTv;

    /**
     * 发布时间
     */
    private TextView publishTimeTv;

    /**
     * 天气描述
     */
    private TextView weatherDespTv;

    /**
     * 气温1
     */
    private TextView temp1Tv;

    /**
     * 气温2
     */
    private TextView temp2Tv;

    /**
     * 当前日期
     */
    private TextView currentDateTv;

    /**
     * 切换城市按钮
     */
    private Button switchBtn;

    /**
     * 刷新按钮
     */
    private Button refreshBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);
        initView();
        initAction();
        queryWeather();
    }

    private void initAction() {
        switchBtn.setOnClickListener(this);
        refreshBtn.setOnClickListener(this);
    }

    private void queryWeather() {
        String countyCode = getIntent().getStringExtra("county_code");
        if (!TextUtils.isEmpty(countyCode)) {
            publishTimeTv.setText("同步中...");
            weatherInfolayout.setVisibility(View.INVISIBLE);
            cityNameTv.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);
        } else {
            showWeather();
        }
    }

    private void initView() {
        weatherInfolayout = (LinearLayout) findViewById(R.id.layout_weather_info);
        cityNameTv = (TextView) findViewById(R.id.tv_title);
        publishTimeTv = (TextView) findViewById(R.id.tv_publish_time);
        weatherDespTv = (TextView) findViewById(R.id.tv_weather_desp);
        temp1Tv = (TextView) findViewById(R.id.tv_temp1);
        temp2Tv = (TextView) findViewById(R.id.tv_temp2);
        currentDateTv = (TextView) findViewById(R.id.tv_current_date);
        switchBtn = (Button) findViewById(R.id.btn_switch);
        refreshBtn = (Button) findViewById(R.id.btn_refresh);
    }

    /**
     * 查询县级代号对应的天气代号
     *
     * @param countyCode
     */
    private void queryWeatherCode(String countyCode) {
        String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
        queryFromServer(address, "countyCode");
    }

    /**
     * 查询天气代号对应的天气
     *
     * @param weatherCode
     */
    private void queryWeatherInfo(String weatherCode) {
        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
        queryFromServer(address, "weatherCode");
    }

    private void queryFromServer(final String address, final String type) {
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if ("countyCode".equals(type)) {
                    if (!TextUtils.isEmpty(response)) {
                        //从服务器解析出天气代号
                        String[] array = response.split("\\|");
                        if (array != null && array.length == 2) {
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                } else if ("weatherCode".equals(type)) {
                    Utility.handleWeatherResponse(WeatherActivity.this, response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });

                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishTimeTv.setText("同步失败");
                    }
                });
            }
        });
    }

    private void showWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        cityNameTv.setText(prefs.getString("city_name", ""));
        temp1Tv.setText(prefs.getString("temp1", ""));
        temp2Tv.setText(prefs.getString("temp2", ""));
        weatherDespTv.setText(prefs.getString("weather_desp", ""));
        publishTimeTv.setText(prefs.getString("publish_time", ""));
        currentDateTv.setText(prefs.getString("current_date", ""));
        publishTimeTv.setText("今天" + prefs.getString("publish_time", "") + "发布");
        weatherInfolayout.setVisibility(View.VISIBLE);
        cityNameTv.setVisibility(View.VISIBLE);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_switch:
                Intent intent = new Intent(this, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                finish();
                break;
            case R.id.btn_refresh:
                publishTimeTv.setText("同步中...");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String weatherCode = prefs.getString("weather_code", "");
                if (!TextUtils.isEmpty(weatherCode)) {
                    queryWeatherInfo(weatherCode);
                }
                break;
            default:
                break;

        }
    }
}
