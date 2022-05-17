package com.example.androidapp;

import android.app.Activity ;
import android.os.Bundle ;

import androidx.appcompat.app.AppCompatActivity;

//import android.support.v7.app.AppCompatActivity ;
public class MyBaseActivity extends AppCompatActivity {
    protected MyApp mMyApp ;
    public void onCreate (Bundle savedInstanceState) {
        super .onCreate(savedInstanceState) ;
        mMyApp = (MyApp) this .getApplicationContext() ;
    }
    protected void onResume () {
        super .onResume() ;
        mMyApp .setCurrentActivity( this ) ;
    }
    protected void onPause () {
        clearReferences() ;
        super .onPause() ;
    }
    protected void onDestroy () {
        clearReferences() ;
        super .onDestroy() ;
    }
    private void clearReferences () {
        Activity currActivity = mMyApp .getCurrentActivity() ;
        if ( this .equals(currActivity))
            mMyApp .setCurrentActivity( null ) ;
    }
}