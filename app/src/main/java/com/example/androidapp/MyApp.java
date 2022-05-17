package com.example.androidapp;

import android.app.Activity ;
import android.app.Application ;
public class MyApp extends Application {
    private Activity mCurrentActivity = null;
    @Override
    public void onCreate () {
        super .onCreate() ;
    }
    public Activity getCurrentActivity () {
        return mCurrentActivity ;
    }
    public void setCurrentActivity (Activity mCurrentActivity) {
        this . mCurrentActivity = mCurrentActivity ;
    }
}
