package com.example.wper_smile.weatherapp;

import android.provider.BaseColumns;

/**
 * Created by wper-smile on 2016/11/8.
 */

public class Weather {

    public  Weather(){}

    public static abstract class WeatherData implements BaseColumns{
        public static final String TABLE_NAME="weather";
        public static final String COLUMN_NAME_WEATHER="weather";//列：天气状况
        public static final String COLUMN_NAME_TEMPERATURE="temperature";//列：气温范围
        public static final String COLUMN_NAME_WIND="wind";//列：风
        public static final String COLUMN_NAME_CITY="city";//列：城市
        public static final String COLUMN_NAME_DATE_Y="date_y";//列：日期
        public static final String COLUMN_NAME_ADVICE="dressing_advice";//列：建议穿着

    }


}
