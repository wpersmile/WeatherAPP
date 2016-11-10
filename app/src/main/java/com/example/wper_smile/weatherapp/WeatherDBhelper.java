package com.example.wper_smile.weatherapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by wper-smile on 2016/11/8.
 */

public class WeatherDBhelper extends SQLiteOpenHelper {


    private final static String DATABASE_NAME = "weatherdb";//数据库名字
    private final static int DATABASE_VERSION = 1;//数据库版本

    //建表SQL
    private final static String SQL_CREATE_DATABASE = "CREATE TABLE " + Weather.WeatherData.TABLE_NAME + " (" +
            Weather.WeatherData._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            Weather.WeatherData.COLUMN_NAME_WEATHER + " TEXT" + "," +
            Weather.WeatherData.COLUMN_NAME_ADVICE + " TEXT" + "," +
            Weather.WeatherData.COLUMN_NAME_CITY + " TEXT" + "," +
            Weather.WeatherData.COLUMN_NAME_DATE_Y + " TEXT" + "," +
            Weather.WeatherData.COLUMN_NAME_WIND + " TEXT" + ","
            + Weather.WeatherData.COLUMN_NAME_TEMPERATURE + " TEXT" + " )";

    //删表SQL
    private final static String SQL_DELETE_DATABASE = "DROP TABLE IF EXISTS " + Weather.WeatherData.TABLE_NAME;

    public WeatherDBhelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //创建数据库
        sqLiteDatabase.execSQL(SQL_CREATE_DATABASE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        //当数据库升级时被调用，首先删除旧表，然后调用OnCreate()创建新表
        sqLiteDatabase.execSQL(SQL_DELETE_DATABASE);
        onCreate(sqLiteDatabase);
    }
}
