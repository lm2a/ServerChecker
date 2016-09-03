package com.lm2a.serverchecker.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;



public class BootAndUpdateReceiver extends BroadcastReceiver {

    private static final String TAG = "BootAndUpdateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED") || //after boot
                intent.getAction().equals("android.intent.action.LM2A")) {//when my app is updated
            Intent startServiceIntent = new Intent(context, BackgroundService.class);
            context.startService(startServiceIntent);
        }
    }
}