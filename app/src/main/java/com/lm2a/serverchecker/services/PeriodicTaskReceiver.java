package com.lm2a.serverchecker.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.SyncStateContract;

import com.lm2a.serverchecker.MyApplication;

public class PeriodicTaskReceiver extends BroadcastReceiver {

    private static final String TAG = "PeriodicTaskReceiver";
    private static final String INTENT_ACTION = "com.lm2a.serverchecker.PERIODIC_TASK_HEART_BEAT";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction() != null && !intent.getAction().isEmpty()) {
            MyApplication myApplication = (MyApplication) context.getApplicationContext();
            SharedPreferences sharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(context);

            if (intent.getAction().equals("android.intent.action.BATTERY_LOW")) {
                sharedPreferences.edit().putBoolean(Constants.BACKGROUND_SERVICE_BATTERY_CONTROL, false).apply();
                stopPeriodicTaskHeartBeat(context);
            } else if (intent.getAction().equals("android.intent.action.BATTERY_OKAY")) {
                sharedPreferences.edit().putBoolean(Constants.BACKGROUND_SERVICE_BATTERY_CONTROL, true).apply();
                restartPeriodicTaskHeartBeat(context, myApplication);
            } else if (intent.getAction().equals(INTENT_ACTION)) {
                doPeriodicTask(context, myApplication);
            }
        }
    }

    private void doPeriodicTask(Context context, MyApplication myApplication) {
        // Periodic task(s) go here ...
    }

    public void restartPeriodicTaskHeartBeat(Context context, MyApplication myApplication) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        boolean isBatteryOk = sharedPreferences.getBoolean(Constants.BACKGROUND_SERVICE_BATTERY_CONTROL, true);
        Intent alarmIntent = new Intent(context, PeriodicTaskReceiver.class);
        boolean isAlarmUp = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_NO_CREATE) != null;

        if (isBatteryOk && !isAlarmUp) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmIntent.setAction(INTENT_ACTION);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
       }
    }

    public void stopPeriodicTaskHeartBeat(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, PeriodicTaskReceiver.class);
        alarmIntent.setAction(INTENT_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        alarmManager.cancel(pendingIntent);
    }
}