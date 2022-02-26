package com.example.a1stapp;

import android.app.Application;
import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Locale;

//application close runs before your launcher activity
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    //created a static method to convert timestamp to proper date format so we can use it everywhere in project no repeating..
    public static final String formatTimestamp(long timestamp){
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);
        //format timsestamp to dd/mm/yyyy
        String date = DateFormat.format("dd/mm/yyyy",cal).toString();

        return date;
    }
}
