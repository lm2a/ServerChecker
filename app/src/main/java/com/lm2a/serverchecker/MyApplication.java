package com.lm2a.serverchecker;

import android.app.Application;
import android.content.Intent;

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";



    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize the singletons so their instances
        // are bound to the application process.

         Intent startServiceIntent = new Intent(this, com.lm2a.serverchecker.services.BackgroundService.class);
         startService(startServiceIntent);
    }



}