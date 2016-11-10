package com.example.wper_smile.weatherapp;

import java.text.SimpleDateFormat;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
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

    String CityFileName="CitylData";
    String WeatherFileName="WeatherAllData";
    String WeatherTypeFileName="WeatherTypeData";
    String PhoneNumFileName="PhoneNumData";

    Runnable getWeather;
    TextView tex_show;
    TextView tex_top;
    String Name=null;
    String PhoneNum=null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tex_top= (TextView) findViewById(R.id.tex_top);
        tex_show= (TextView) findViewById(R.id.tex_show);

        show_picture();//显示背景图片

        show_City(); //显示Top城市名字

        show_data();//显示 WeatherAllData 内容

        //是否成功获取数据判断
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
        //将获取到的City 设置到tex_top中
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
        //处理传来的now_Weather数据
        final Handler handlerWeather = new Handler() {
            @Override
            public void handleMessage(Message msgWeather) {

                String now_Weather = msgWeather.obj.toString();

                change_picture(now_Weather);

                /****************************短信提醒*********************************/
                if (now_Weather.equals("小雨") || now_Weather.equals("中雨") || now_Weather.equals("大雨") ||
                        now_Weather.equals("爆雨") || now_Weather.equals("阵雨"))
                    send_message("近期可能下雨，出门记得带雨具");

                if (now_Weather.equals("浮尘") || now_Weather.equals("扬尘") ||
                        now_Weather.equals("强沙尘暴")||now_Weather.equals("霾"))

                    send_message("近期空气质量较差，请减少户外活动");
                if (now_Weather.equals("中雪") ||now_Weather.equals("暴雪") || now_Weather.equals("大雪"))
                    send_message("近期雪有点大，减少外出吧");

                //储存当前天气信息

                save_data(now_Weather, WeatherTypeFileName);
            }
        };



                getWeather = new Runnable() {
                    @Override
                    public void run() {

                        try {
                            final String url = "http://v.juhe.cn/weather/index?format=1&cityname=" + Name + "&key=bf3abfec4dff0cd1f2dd3d0a396f1712";
                            URL httpUrl = new URL(url);
                            try {
                                HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
                                conn.setReadTimeout(3000);//超时处理
                                conn.setRequestMethod("GET");//GET获取

                                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                                StringBuffer stringBuffer = new StringBuffer();
                                String str;

                                str = bufferedReader.readLine();
                                //一以下为解析获取的JSON数据
                                try {
                                    JSONObject object = new JSONObject(str);
                                    int result = object.getInt("resultcode");
                                    if (result == 200) {
                                        JSONObject resultJson = object.getJSONObject("result");
                                        /**********以下为解析当前时间JSON********/

                                        JSONObject skJson = resultJson.getJSONObject("sk");
                                        String time = skJson.getString("time");//数据获取时间
                                        String humidity = skJson.getString("humidity");//获取湿度
                                        String wind_direction = skJson.getString("wind_direction");//风向
                                        String wind_strength = skJson.getString("wind_strength");//风力等级


                                        /***************以下为解析今天天气的JSON数据*************************/

                                        JSONObject todayJson = resultJson.getJSONObject("today");
                                        String temperature = todayJson.getString("temperature"); //获取温度
                                        String weather = todayJson.getString("weather");//获取天气状态
                                        String wind = todayJson.getString("wind");//获取风的状态
                                        String city = todayJson.getString("city");//获取当前城市
                                        String date_y = todayJson.getString("date_y");//获取当前日期
                                        String dressing_index = todayJson.getString("dressing_index");//获取当前体感温度
                                        String dressing_advice = todayJson.getString("dressing_advice");//今日天气推荐
                                        String week = todayJson.getString("week");//获取星期几


                                        /*********向handler传递当前天气数据***********/
                                        Message msgWeather = new Message();
                                        msgWeather.obj = weather;
                                        handlerWeather.sendMessage(msgWeather);


                                        /************以下为解析未来两天天气JSON数据*****************************/


                                        /********************未来天气的JSON格式**********************************/

                                        /**
                                         "future":{
                                         "day_20161107":
                                         {
                                         "temperature":"1℃~11℃",
                                         "weather":"晴",
                                         "weather_id":
                                         {
                                         "fa":"00",
                                         "fb":"00"
                                         },
                                         "wind":"微风",
                                         "week":"星期一",
                                         "date":"20161107"
                                         },
                                         "day_20161108":{
                                         "temperature":"-1℃~10℃",
                                         "weather":"晴",
                                         "weather_id":{
                                         "fa":"00",
                                         "fb":"00"
                                         },
                                         "wind":"微风",
                                         "week":"星期二",
                                         "date":"20161108"
                                         },
                                         .....
                                         }

                                         */

                                        /****************或许当前系统时间************************/

                                        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
                                        Date curDate = new Date(System.currentTimeMillis());
                                        String str_date = formatter.format(curDate);


                                        /*********获取明天日期***********/

                                        int one = Integer.parseInt(str_date);
                                        one = one + 1;

                                        /*****日期格式转JSON数据对应格式*********/

                                        String one_day = Integer.toString(one);
                                        one_day = "day_" + one_day;

                                        /**************明日天气状态************************/

                                        JSONObject futureJson = resultJson.getJSONObject("future");

                                        JSONObject one_dayJson = futureJson.getJSONObject(one_day);
                                        String one_week = one_dayJson.getString("week");
                                        String one_wind = one_dayJson.getString("wind");
                                        String one_date = one_dayJson.getString("date");
                                        String one_temperature = one_dayJson.optString("temperature");
                                        String one_weather = one_dayJson.optString("weather");


                                        /*************获取后天日期******************/

                                        int two = Integer.parseInt(str_date);
                                        two = two + 2;


                                        /*****日期格式转JSON数据对应格式*********/

                                        String two_day = Integer.toString(two);
                                        two_day = "day_" + two_day;


                                        Log.v("One_DAY", one_day);
                                        Log.v("tow", two_day);

                                        /**************后天天气状态********************/

                                        JSONObject two_dayJson = futureJson.getJSONObject(two_day);

                                        String two_week = two_dayJson.getString("week");
                                        String two_wind = two_dayJson.getString("wind");
                                        String two_date = two_dayJson.getString("date");
                                        String two_temperature = two_dayJson.getString("temperature");
                                        String two_weather = two_dayJson.getString("weather");


                                        /***************    设置 tex_show 显示的内容     ******************/
                                        String wdata = "\n\n\n" + "获取时间：" + time + "\n\n当前湿度：" + humidity + "\n\n当前风况：" +
                                                wind_direction + wind_strength + "\n\n\n当前日期：" + date_y + "\n\n" + week + "\n\n" +
                                                "天气状况：" + weather +
                                                "\n\n当前气温：" + temperature
                                                + "\n\n体感温度:  " + dressing_index + "\n\n当前风速："
                                                + wind + "\n\n\n温馨提示:  " + dressing_advice +
                                                "\n\n\n\n" + "未来几天天气预报\n\n\n" + "" +
                                                "当前日期：" + one_date + "\n\n" + one_week + "\n\n当前风速：" + one_wind + "\n\n当天气温：" + one_temperature +
                                                "\n\n天气状况：" + one_weather + "\n\n\n当前日期：" + two_date + "" +
                                                "\n\n" + two_week + "\n\n当天风速：" + two_wind + "\n\n当天气温：" + two_temperature +
                                                "\n\n天气状况：" + two_weather;


                                        /*********以下为储存tex_show信息内容******************/

                                        save_data(wdata, WeatherFileName);


                                        /*********以下为储存当前天气城市名字****************/

                                        save_data(city, CityFileName);

                                        /*********向handler传递全部天气数据***********/

                                        Message msg = new Message();
                                        msg.obj = wdata;
                                        msg.what = 1;
                                        handler.sendMessage(msg);


                                        /*********向handler传递当前城市名字***********/

                                        Message msgCity = new Message();
                                        msgCity.obj = city;
                                        msgCity.what = 0;
                                        handlerCity.sendMessage(msgCity);
                                        //Insert(temperature,weather,city,dressing_advice,date_y,wind);
                                    } else {
                                        /*********获取数据失败时提示*********/
                                        Message msg = new Message();
                                        msg.what = 0;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_about:
            {
                /*********************弹出一个对话框，版本说明***************************/

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("版本2.0\n" +
                        "编译日期：2016.11.10\n" +
                        "制作人：Wper_Smile\n"+
                        "email：wpersmile@163.com\n"+
                        "主页：www.wpersmile.com")//显示对话框消息内容
                        .setTitle("关于WP天气");//对话框标题
                builder.show();
            }
            break;
            case R.id.action_update:
            {
                /*********************弹出一个对话框，版本更新说明***************************/

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(
                        "修复第一版界面变白及程序偶尔崩溃等BUG，修改部分操作逻辑,增加程序稳定性\n\n" +
                        "增加短信预警接口，长按MENU即可调出菜单选择。\n")//显示对话框消息内容
                        .setTitle("WP天气  版本2.0\n");//对话框标题
                builder.show();
            }
            break;
            case R.id.action_query:
            {
                /*********************弹出一个对话框，搜索天气***************************/
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

            case R.id.action_sms:
            {
                /********************* 弹出一个对话框，设置天气预警号码 ***************************/
                AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater=getLayoutInflater();
                final View view=inflater.inflate(R.layout.sms_activity,null);
                builder.setView(view)
                        .setTitle("灾害预警号码设置")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                EditText editText= (EditText) view.findViewById(R.id.edt_Adg_getPhoneNum);
                               PhoneNum=editText.getText().toString();
                                Toast.makeText(MainActivity.this, "信息输入成功", Toast.LENGTH_SHORT).show();
                                save_data(PhoneNum,PhoneNumFileName);
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


    /*********************  当启动APP时，调用此方法显示天气数据   ***************************/

    public void show_data()
    {
        try {
            String show_data=get_data(WeatherFileName);
            if (show_data.equals("")||show_data==null)
                tex_show.setText("长按MENU键可以调出操作菜单哦");
            else
                tex_show.setText(show_data);
        }
        catch (Exception e){
            tex_show.setText("长按MENU键可以调出操作菜单哦");
        }

    }

    /*********************  当启动APP时，调用此方法显示城市数据   ***************************/

    public void show_City()
    {

        try {
            String show_data=get_data(CityFileName);
            if (show_data.equals("")||show_data==null)
                tex_top.setText("WP"+"天气预报");
            else
                tex_top.setText(show_data);
        }
        catch (Exception e){
            tex_top.setText("WP"+"天气预报");
        }


    }
    /*********************  当启动APP时，调用此方法显示天气背景   ***************************/
    public void show_picture()
    {
        try {
            String show_data=get_data(WeatherTypeFileName);
            change_picture(show_data);
        }
        catch (Exception e){
            change_picture("晴");
        }

    }

    /*********************   根据获取的天气类型不同   调用此方法设置不同的天气背景  PS:天气种类不全 待扩充 else 为待扩充 **********************/
    public void change_picture(String now_Weather){

        if (now_Weather.equals("霾")||now_Weather.equals("浮尘")||now_Weather.equals("扬尘")||
                now_Weather.equals("强沙尘暴"))
        {
            RelativeLayout relativeLayout= (RelativeLayout) findViewById(R.id.activity_main);
            Resources resources = getResources();
            Drawable drawable=resources.getDrawable(R.drawable.mai);
            relativeLayout.setBackgroundDrawable(drawable);
        }
        else if (now_Weather.equals("晴"))
        {
            RelativeLayout relativeLayout= (RelativeLayout) findViewById(R.id.activity_main);
            Resources resources = getResources();
            Drawable drawable=resources.getDrawable(R.drawable.sun);
            relativeLayout.setBackgroundDrawable(drawable);
        }
        else if (now_Weather.equals("多云")||now_Weather.equals("阴天"))
        {
            RelativeLayout relativeLayout= (RelativeLayout) findViewById(R.id.activity_main);
            Resources resources = getResources();
            Drawable drawable=resources.getDrawable(R.drawable.cloudy);
            relativeLayout.setBackgroundDrawable(drawable);
        }
        else if (now_Weather.equals("小雨")||now_Weather.equals("中雨")||now_Weather.equals("大雨")||
                now_Weather.equals("爆雨")||now_Weather.equals("阵雨"))
        {
            RelativeLayout relativeLayout= (RelativeLayout) findViewById(R.id.activity_main);
            Resources resources = getResources();
            Drawable drawable=resources.getDrawable(R.drawable.rain);
            relativeLayout.setBackgroundDrawable(drawable);
        }
        else
        {
            RelativeLayout relativeLayout= (RelativeLayout) findViewById(R.id.activity_main);
            Resources resources = getResources();
            Drawable drawable=resources.getDrawable(R.drawable.cloudy);
            relativeLayout.setBackgroundDrawable(drawable);
        }
    }



    /****************  天气预警  根据天气情况，向用户发送预警短信  PS：网络接口需要付费   这里只实现功能  ********/
    public void send_message(String message){

        try {
            String sms_phoneNum = get_data(PhoneNumFileName);
            if (sms_phoneNum!=null || sms_phoneNum.equals(""))
            {
                PendingIntent pendingIntent=PendingIntent.getBroadcast(this,0,new Intent(),0);
                SmsManager smsManager=SmsManager.getDefault();
                smsManager.sendTextMessage(sms_phoneNum,"null",message,pendingIntent,null);
            }
        }
        catch (Exception e){}


    }

    /*********************  存储数据    方式：文件 ***************************/
    public void save_data(String object,String fileNane) {
        OutputStream out = null;
        try {
            FileOutputStream fileOutputStream = openFileOutput(fileNane, MODE_PRIVATE);
            out = new BufferedOutputStream(fileOutputStream);
            try {
                out.write(object.getBytes(StandardCharsets.UTF_8));
            } finally {
                if (out != null)
                    out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*********************  获取数据  方式：文件   ***************************/
    public String get_data(String fileName)
    {
        String data_res=null;
        try {
            FileInputStream fis = openFileInput(fileName);
            BufferedInputStream bis = new BufferedInputStream(fis);
            BufferedReader reader = new BufferedReader (new InputStreamReader(bis));

            StringBuilder stringBuilder=new StringBuilder("");
            try{
                while (reader.ready()) {
                    stringBuilder.append((char)reader.read());
                }
               data_res=stringBuilder.toString();
            }
            finally {
                if(reader!=null)
                    reader.close();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return data_res;
    }






























/*
    //使用Sql语句插入天气
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



