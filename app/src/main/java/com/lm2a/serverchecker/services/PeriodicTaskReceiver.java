package com.lm2a.serverchecker.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.lm2a.serverchecker.MainActivity;
import com.lm2a.serverchecker.R;
import com.lm2a.serverchecker.model.Config;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class PeriodicTaskReceiver extends BroadcastReceiver {

    private static final String TAG = "PeriodicTaskReceiver";
    private Context mContext;
    private Config mConfig;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        mConfig = getParametersFromPreferences(context);
        if (intent.getAction() != null && !intent.getAction().isEmpty()) {
            SharedPreferences sharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(context);

            if (intent.getAction().equals("android.intent.action.BATTERY_LOW")) {
                sharedPreferences.edit().putBoolean(Constants.BACKGROUND_SERVICE_BATTERY_CONTROL, false).apply();
                stopPeriodicTaskHeartBeat(context);
                Log.i(TAG, "BATTERY_LOW");
            } else if (intent.getAction().equals("android.intent.action.BATTERY_OKAY")) {
                sharedPreferences.edit().putBoolean(Constants.BACKGROUND_SERVICE_BATTERY_CONTROL, true).apply();
                restartPeriodicTaskHeartBeat(context);
                Log.i(TAG, "BATTERY_OKAY");
            } else if (intent.getAction().equals(Constants.INTENT_ACTION)) {
                Log.i(TAG, "Doing TASK");
                doPeriodicTask(context);
            }
        }
    }

    private void doPeriodicTask(Context context) {
        // Periodic task(s) go here ...
        check();
    }

    public void restartPeriodicTaskHeartBeat(Context context) {
        Log.i(TAG, "restartPeriodicTaskHeartBeat");
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        boolean isBatteryOk = sharedPreferences.getBoolean(Constants.BACKGROUND_SERVICE_BATTERY_CONTROL, true);
        Intent alarmIntent = new Intent(context, PeriodicTaskReceiver.class);
        boolean isAlarmUp = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_NO_CREATE) != null;


        if (isBatteryOk && !isAlarmUp) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmIntent.setAction(Constants.INTENT_ACTION);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), calculateInterval(context), pendingIntent);
        }
    }

    public void stopPeriodicTaskHeartBeat(Context context) {
        Log.i(TAG, "stopPeriodicTaskHeartBeat");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, PeriodicTaskReceiver.class);
        alarmIntent.setAction(Constants.INTENT_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        alarmManager.cancel(pendingIntent);
    }

    //---All the business logic---------------------------------------------------------------------------

    private static final int TIME_OUT = 60000;
    private static final int DEFAULT_PORT = 80;


    private void check() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isAlive = isReachable(mConfig.url, DEFAULT_PORT, TIME_OUT);
                if (isAlive) {
                    Log.i("TAG", "lm2a Alive");
                } else {
                    Log.i("TAG", "lm2a Dead");
                    showNotification("lm2a Dead");
                }

            }
        });
        thread.start();


    }

    private Config getParametersFromPreferences(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        int i = sharedPrefs.getInt(Constants.INTERVAL, 1000);//1 seg default
        int t = sharedPrefs.getInt(Constants.TIME_UNIT, 0);//hour default
        String u = sharedPrefs.getString(Constants.SITE_URL, null);
        String e = sharedPrefs.getString(Constants.EMAIL, null);
        if (u != null) {
            return new Config(i, t, u, e);
        } else {
            return null;
        }
    }


    private static boolean isReachable(String addr, int openPort, int timeOutMillis) {
        // Any Open port on other machine
        // openPort =  22 - ssh, 80 or 443 - webserver, 25 - mailserver etc.
        try {
            try (Socket soc = new Socket()) {
                soc.connect(new InetSocketAddress(addr, openPort), timeOutMillis);
            }
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public void showNotification(String report) {

        // define sound URI, the sound to be played when there's a notification
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // intent triggered, you can start other intent for other actions
        Intent intent = new Intent(mContext, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

        // this is it, we'll build the notification!
        // in the addAction method, if you don't want any icon, just set the first param to 0
        Notification mNotification = new Notification.Builder(mContext)

                .setContentTitle("Server Checker Report")
                .setContentText(report)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pIntent)
                .setSound(soundUri)

                .addAction(R.mipmap.ic_launcher, "View", pIntent)
                .addAction(0, "Remind", pIntent)

                .build();

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Service.NOTIFICATION_SERVICE);

        // If you want to hide the notification after it was selected, do the code below
        // myNotification.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, mNotification);
    }


    public long calculateInterval(Context context) {
        if(mConfig==null){
            mConfig=getParametersFromPreferences(context);
        }

        long timeInMiliseconds;
        if (mConfig.timeUnit == Constants.HOURS) {
            timeInMiliseconds = mConfig.interval * Constants.T_HOURS;

        } else if (mConfig.timeUnit == Constants.MINUTES) {
            timeInMiliseconds = mConfig.interval * Constants.T_MINUTES;
        } else {
            timeInMiliseconds = mConfig.interval * Constants.T_SECONDS;
        }
        return timeInMiliseconds;
    }
}