package com.example.wper_smile.weatherapp;

import java.text.SimpleDateFormat;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;



public class MainActivity extends AppCompatActivity {

    private final static String MyFileName="myfile";
    Runnable getWeather;
    TextView tex_show;
    TextView tex_top;
    WeatherDBhelper weatherDBhelper;
    String Name=null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        Button FDX= (Button) findViewById(R.id.FDX);
        FDX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                 /*Intent intent=new Intent();
                intent.setAction(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("smsto:"+"18811127360"));
                intent.putExtra("sms_body", "The SMS text");
                startActivity(intent);*/

            }
        });


        //创建SQLiteOpenHelper对象，注意第一次运行时，此时数据库并没有被创建
       weatherDBhelper = new WeatherDBhelper(this);


        SimpleDateFormat formatter = new SimpleDateFormat ("yyyyMMdd");
        Date curDate = new Date(System.currentTimeMillis());
        String str = formatter.format(curDate);
        //获取当前时间
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();



         tex_top= (TextView) findViewById(R.id.tex_top);
         tex_show= (TextView) findViewById(R.id.tex_show);
        //显示数据
        show_picture();
        show_City();
        show_data();

        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {

                switch (msg.what) {
                    case 0:
                        Toast.makeText(MainActivity.this, "没有该数据源信息", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        Toast.makeText(MainActivity.this, "获取成功", Toast.LENGTH_SHORT).show();
                        tex_show.setText(msg.obj.toString());
                        break;

                }
            }
        };
        final Handler handlerCity = new Handler(){
            @Override
            public void handleMessage(Message msgCity) {

                switch (msgCity.what) {
                    case 0:
                        Toast.makeText(MainActivity.this, "获取成功", Toast.LENGTH_SHORT).show();

                        tex_top.setText(msgCity.obj.toString()+"天气预报");
                        break;

                }
            }
        };
        final Handler handlerWeather = new Handler(){
            @Override
            public void handleMessage(Message msgWeather) {

                String now_Weather=msgWeather.obj.toString();

                //储存城市信息

                OutputStream out=null;
                try {
                    FileOutputStream fileOutputStream=openFileOutput("WeatherType",MODE_PRIVATE);
                    out=new BufferedOutputStream(fileOutputStream);
                    try {
                        out.write(now_Weather.getBytes(StandardCharsets.UTF_8));
                    }
                    finally {
                        if(out!=null)
                            out.close();
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
               change_picture(now_Weather);
            }
        };


           getWeather=new Runnable() {
                @Override
                public void run() {

                    try {
                        final String url="http://v.juhe.cn/weather/index?format=1&cityname="+Name+"&key=bf3abfec4dff0cd1f2dd3d0a396f1712";
                        URL httpUrl=new URL(url);
                        Log.v("url",url);
                        try {
                            HttpURLConnection conn= (HttpURLConnection) httpUrl.openConnection();
                            conn.setReadTimeout(3000);//超时处理
                            conn.setRequestMethod("GET");//GET获取

                            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            StringBuffer stringBuffer=new StringBuffer();
                            String str;
                           /* while((str=bufferedReader.readLine())!=null)
                            {
                                stringBuffer.append(str);
                            }*/
                            str=bufferedReader.readLine();
                            try {
                                JSONObject object=new JSONObject(str);
                                int result=object.getInt("resultcode");
                                if (result==200)
                                {
                                    JSONObject resultJson=object.getJSONObject("result");
                                    JSONObject todayJson=resultJson.getJSONObject("today");
                                    String temperature=todayJson.getString("temperature"); //获取温度
                                    String weather=todayJson.getString("weather");//获取天气状态
                                    Log.v("A",weather);



                                    Message msgWeather=new Message();
                                    msgWeather.obj=weather;
                                    handlerWeather.sendMessage(msgWeather);



                                    JSONObject skJson=resultJson.getJSONObject("sk");
                                    String time=skJson.getString("time");//数据获取时间
                                    String humidity=skJson.getString("humidity");//获取湿度
                                    String wind_direction=skJson.getString("wind_direction");//风向
                                    String wind_strength=skJson.getString("wind_strength");//风力等级
                                    //today
                                    String wind=todayJson.getString("wind");//获取风的状态
                                    String city=todayJson.getString("city");//获取当前城市
                                    String date_y=todayJson.getString("date_y");//获取当前日期
                                    String dressing_index=todayJson.getString("dressing_index");//获取当前体感温度
                                    String dressing_advice=todayJson.getString("dressing_advice");//今日天气推荐
                                    String week=todayJson.getString("week");//获取星期几

                                    Log.v("A",date_y+city+"天气预报"+weather+"当前气温"+temperature
                                            +"当前体感温度"+dressing_index+wind+dressing_advice);




                                    //future


                                    //day_20161108
                                    SimpleDateFormat formatter = new SimpleDateFormat ("yyyyMMdd");
                                    Date curDate = new Date(System.currentTimeMillis());
                                    String str_date = formatter.format(curDate);
                                    //获取当前时间


                                    //明天数据
                                    int one=Integer.parseInt(str_date);
                                    one=one+1;


                                    String one_day=Integer.toString(one);
                                    one_day="day_"+one_day;


                                    JSONObject futureJson=resultJson.getJSONObject("future");

                                    JSONObject one_dayJson=futureJson.getJSONObject(one_day);
                                    String one_week=one_dayJson.getString("week");
                                    String one_wind=one_dayJson.getString("wind");
                                    String one_date=one_dayJson.getString("date");
                                    String one_temperature=one_dayJson.optString("temperature");
                                    String one_weather=one_dayJson.optString("weather");



                                    //后天数据
                                    int two=Integer.parseInt(str_date);
                                    two=two+2;

                                    String two_day=Integer.toString(two);
                                    two_day="day_"+two_day;


                                    Log.v("One_DAY",one_day);
                                    Log.v("tow",two_day);
                                    JSONObject two_dayJson=futureJson.getJSONObject(two_day);

                                    String two_week=two_dayJson.getString("week");
                                    String two_wind=two_dayJson.getString("wind");
                                    String two_date=two_dayJson.getString("date");

                                    String two_temperature=two_dayJson.getString("temperature");
                                    String two_weather=two_dayJson.getString("weather");



                                    String wdata="\n\n\n"+"获取时间："+time+"\n\n当前湿度："+humidity+"\n\n当前风况："+
                                            wind_direction+wind_strength+"\n\n\n当前日期："+date_y+"\n\n"+week+"\n\n"+
                                            "天气状况："+weather+
                                            "\n\n当前气温："+temperature
                                            +"\n\n体感温度:  "+dressing_index+"\n\n当前风速："
                                            +wind+"\n\n\n温馨提示:  "+dressing_advice+
                                            "\n\n\n\n"+"未来几天天气预报\n\n\n"+"" +
                                            "当前日期："+one_date+"\n\n"+one_week+"\n\n当前风速："+one_wind+"\n\n当天气温："+one_temperature+
                                            "\n\n天气状况："+one_weather+ "\n\n\n当前日期："+two_date+"" +
                                            "\n\n"+two_week+"\n\n当天风速："+two_wind+"\n\n当天气温："+two_temperature+
                                            "\n\n天气状况："+two_weather;





                                    //储存天气信息
                                    OutputStream out=null;
                                    try {
                                        FileOutputStream fileOutputStream=openFileOutput(MyFileName,MODE_PRIVATE);
                                        out=new BufferedOutputStream(fileOutputStream);
                                        try {
                                            out.write(wdata.getBytes(StandardCharsets.UTF_8));
                                        }
                                        finally {
                                            if(out!=null)
                                                out.close();
                                        }
                                    }
                                    catch (Exception e){
                                        e.printStackTrace();
                                    }

                                    //储存城市信息
                                    try {
                                        FileOutputStream fileOutputStream=openFileOutput("CityName",MODE_PRIVATE);
                                        out=new BufferedOutputStream(fileOutputStream);
                                        try {
                                            out.write(city.getBytes(StandardCharsets.UTF_8));
                                        }
                                        finally {
                                            if(out!=null)
                                                out.close();
                                        }
                                    }
                                    catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    Message msg=new Message();
                                    msg.obj=wdata;
                                    msg.what=1;
                                    handler.sendMessage(msg);

                                    Message msgCity=new Message();
                                    msgCity.obj=city;
                                    msgCity.what=0;
                                    handlerCity.sendMessage(msgCity);
                                    //Insert(temperature,weather,city,dressing_advice,date_y,wind);
                                }
                                else {
                                    Message msg=new Message();
                                    msg.what=0;
                                    handler.sendMessage(msg);

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                }
            };

    }
    //创建一个菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.item, menu);//获取自定义菜单
        return true;
    }

    //菜单点击事件
//弹出一个对话框，版本说明
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_about:
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("版本1.0\n" +
                        "编译日期：2016.11.01\n" +
                        "制作人：Wper_Smile\n"+
                        "email：wpersmile@163.com\n"+
                        "主页：www.wpersmile.com")//显示对话框消息内容
                        .setTitle("关于WP天气");//对话框标题
                builder.show();
            }
            break;
            case R.id.action_query:
            {
               AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater=getLayoutInflater();
                final View view=inflater.inflate(R.layout.query_activity,null);
                builder.setView(view)
                        .setTitle("查询天气")
                        .setPositiveButton("查询", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                EditText editText= (EditText) view.findViewById(R.id.edt_Adg_getCity);
                                Name=editText.getText().toString();
                                Log.v("TEST",Name);
                                Toast.makeText(MainActivity.this,Name, Toast.LENGTH_SHORT).show();
                                Thread thread=new Thread(null,getWeather,"thread");
                                thread.start();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(MainActivity.this, "点击了取消", Toast.LENGTH_SHORT).show();
                            }
                        });
                builder.show();
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }


    public void show_data()
    {
        try {
            FileInputStream fis = openFileInput(MyFileName);
            BufferedInputStream bis = new BufferedInputStream(fis);
            BufferedReader reader = new BufferedReader (new InputStreamReader(bis));

            StringBuilder stringBuilder=new StringBuilder("");
            try{
                while (reader.ready()) {
                    stringBuilder.append((char)reader.read());
                }
                String show=stringBuilder.toString();
                Log.v("log",show);
                tex_show.setText(show);
                //Toast.makeText(MainActivity.this, stringBuilder.toString(),Toast.LENGTH_LONG).show();
            }
            finally {
                if(reader!=null)
                    reader.close();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
    public void show_City()
    {
        try {
            FileInputStream fis = openFileInput("CityName");
            BufferedInputStream bis = new BufferedInputStream(fis);
            BufferedReader reader = new BufferedReader (new InputStreamReader(bis));

            StringBuilder stringBuilder=new StringBuilder("");
            try{
                while (reader.ready()) {
                    stringBuilder.append((char)reader.read());
                }
                String show=stringBuilder.toString();
                tex_top.setText(show+"天气预报");
            }
            finally {
                if(reader!=null)
                    reader.close();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void show_picture()
    {
        try {
            FileInputStream fis = openFileInput("WeatherType");
            BufferedInputStream bis = new BufferedInputStream(fis);
            BufferedReader reader = new BufferedReader (new InputStreamReader(bis));

            StringBuilder stringBuilder=new StringBuilder("");
            try{
                while (reader.ready()) {
                    stringBuilder.append((char)reader.read());
                }
                String now_Weather=stringBuilder.toString();
                change_picture(now_Weather);
            }
            finally {
                if(reader!=null)
                    reader.close();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void change_picture(String now_Weather){

        if (now_Weather.equals("霾")||now_Weather.equals("浮尘")||now_Weather.equals("扬尘")||
                now_Weather.equals("强沙尘暴"))
        {
            RelativeLayout relativeLayout= (RelativeLayout) findViewById(R.id.activity_main);
            Resources resources = getResources();
            Drawable drawable=resources.getDrawable(R.drawable.mai);
            relativeLayout.setBackgroundDrawable(drawable);
        }
        if (now_Weather.equals("晴"))
        {
            RelativeLayout relativeLayout= (RelativeLayout) findViewById(R.id.activity_main);
            Resources resources = getResources();
            Drawable drawable=resources.getDrawable(R.drawable.sun);
            relativeLayout.setBackgroundDrawable(drawable);
        }
        if (now_Weather.equals("多云")||now_Weather.equals("阴天"))
        {
            RelativeLayout relativeLayout= (RelativeLayout) findViewById(R.id.activity_main);
            Resources resources = getResources();
            Drawable drawable=resources.getDrawable(R.drawable.cloudy);
            relativeLayout.setBackgroundDrawable(drawable);
        }
        if (now_Weather.equals("小雨")||now_Weather.equals("中雨")||now_Weather.equals("大雨")||
                now_Weather.equals("爆雨")||now_Weather.equals("阵雨"))
        {
            RelativeLayout relativeLayout= (RelativeLayout) findViewById(R.id.activity_main);
            Resources resources = getResources();
            Drawable drawable=resources.getDrawable(R.drawable.rain);
            relativeLayout.setBackgroundDrawable(drawable);
        }

    }
    public void send_message(String message){
        PendingIntent pendingIntent=PendingIntent.getBroadcast(this,0,new Intent(),0);
        SmsManager smsManager=SmsManager.getDefault();
        smsManager.sendTextMessage("10086","null",message,pendingIntent,null);

    }






























/*

    private  void showWeather()
    {
        SQLiteDatabase db = weatherDBhelper.getReadableDatabase();
        String sql="select top 1 weather from weather order desc";
        db.query("weather","wind",null,null,null,null,"desc","top1");

    }




    //使用Sql语句插入单词
    private void InsertUserSql(String strWord, String strMeaning, String strSample){
        String sql="insert into  words(word,meaning,sample) values(?,?,?)";

        //Gets the data repository in write mode

        SQLiteDatabase db = weatherDBhelper.getWritableDatabase();
        db.execSQL(sql,new String[]{strWord,strMeaning,strSample});
    }

    //使用insert方法增加
    private void Insert(String strTemperature, String strWeather, String strCity,String strAdvice,String strDate_y,String strWind) {

        //Gets the data repository in write mode
        SQLiteDatabase db = weatherDBhelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(Weather.WeatherData.COLUMN_NAME_TEMPERATURE, strTemperature);
        values.put(Weather.WeatherData.COLUMN_NAME_ADVICE, strAdvice);
        values.put(Weather.WeatherData.COLUMN_NAME_CITY, strCity);
        values.put(Weather.WeatherData.COLUMN_NAME_DATE_Y, strDate_y);
        values.put(Weather.WeatherData.COLUMN_NAME_WEATHER, strWeather);
        values.put(Weather.WeatherData.COLUMN_NAME_WIND, strWind);

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                Weather.WeatherData.TABLE_NAME,
                null,
                values);
    }
*/
}



