package com.example.wper_smile.weatherapp;

import android.content.Context;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Handler;

/**
 * Created by wper-smile on 2016/11/7.
 */

public class HttpThread  extends Thread {

    private Context context;
    private  String url;
    private Handler handler;

    public HttpThread(String url, TextView tex_show, Button get_city,TextView tex_city,Handler handler)
    {
        this.url=url;
        this.handler=handler;
    }
    @Override
    public void run() {

        try {
            URL httpUrl=new URL(url);
            try {
                HttpURLConnection conn= (HttpURLConnection) httpUrl.openConnection();
                conn.setReadTimeout(3000);//超时处理
                conn.setRequestMethod("GET");//GET获取


                StringBuffer stringBuffer=new StringBuffer();
                BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(conn.getInputStream()));

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        super.run();
    }

    private void parseJson(String json)
    {
        try {
            JSONObject object=new JSONObject(json);
            int result=object.getInt("resultcode");
            if (result==200)
            {
                JSONObject todayJson=new JSONObject("today");
                String temperature=todayJson.getString("temperature"); //获取温度
                String weather=todayJson.getString("weather");//获取天气状态
                String wind=todayJson.getString("wind");//获取风的状态
                String city=todayJson.getString("city");//获取当前城市
                String date_y=todayJson.getString("date_y");//获取当前日期
                String dressing_index=todayJson.getString("dressing_index");//获取当前体感温度
                String dressing_advice=todayJson.getString("dressing_advice");//今日天气推荐
            }
            else {
                Toast.makeText(context, "获取数据失败", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
