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
import android.widget.Toast;

import com.lm2a.serverchecker.MainActivity;
import com.lm2a.serverchecker.R;
import com.lm2a.serverchecker.database.DatabaseHelper;
import com.lm2a.serverchecker.email.GMailSender;
import com.lm2a.serverchecker.model.Config;
import com.lm2a.serverchecker.model.Email;
import com.lm2a.serverchecker.model.Host;
import com.lm2a.serverchecker.utils.Util;

import java.util.Date;
import java.util.List;

public class PeriodicTaskReceiver extends BroadcastReceiver {

    private static final String TAG = "PeriodicTaskReceiver";
    private Context mContext;
    private Config mConfig;
    private BackgroundService mBackgroundService;




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
        checkStandard();
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


    private void checkStandard() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isAlive = Util.isReachable(mConfig.url, mConfig.port, TIME_OUT);
                Date now = new Date();
                if (isAlive) {
                    Log.i("TAG", "lm2a Alive");
                    sendErrorReport("Everything is bebay at "+now.toString(), mConfig.url + ":" + mConfig.port);
                } else {

                    showNotification(now.toString()+":"+mConfig.url + ":" + mConfig.port + " was not responding in 1'");
                }


                boolean b1 = getLastCheckResult();
                setLastCheckResult(isAlive);//save last checkStandard in preferences
                if(b1 != isAlive){//if current result is different from the last we should update Service to update Activity's UI
                    Intent i = new Intent();
                    i.setAction(Constants.INTENT_ACTION_UPDATE);
                    mContext.sendBroadcast(i);
                }
            }
        });
        thread.start();


    }

    private void checkPro() {
        DatabaseHelper db = new DatabaseHelper(mContext);
        final List<Host> hosts = db.getAllHosts();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Date now = new Date();
                for(Host h: hosts){
                    int r = Util.isServerReachable(mContext, h.getHost());
                    switch(r){
                        case Util.SERVER_OK:
                            Log.i("TAG", "lm2a Alive");
                            setLastCheckResult(true);
                            notifyState(h);
                            break;
                        case Util.SERVER_KO:
                            setLastCheckResult(false);
                            Log.i("TAG", "lm2a KO");
                            if(h.isEmails()){
                                sentReportToEverybody(h.getAllEmails(), h.getHost() +" was KO" + " at"+now.toString());
                            }
                            if(h.isNotification()){
                                showNotification(now.toString()+":"+h.getHost() + " was not responding in 1'");
                            }
                            notifyState(h);
                            break;
                        case Util.URL_MALFORMED:
                            setLastCheckResult(false);
                            Log.i("TAG", "URL malformed");
                            if(h.isEmails()){
                                sentReportToEverybody(h.getAllEmails(), h.getHost() +" had URL malformed" + " at"+now.toString());
                            }
                            if(h.isNotification()){
                                showNotification(now.toString()+":"+h.getHost() + " had URL malformed");
                            }
                            notifyState(h);
                            break;
                        case Util.IO_FAILURE:
                            setLastCheckResult(false);
                            Log.i("TAG", "I/O problem, please try again");
                            if(h.isEmails()){
                                sentReportToEverybody(h.getAllEmails(), h.getHost() +" had I/O problem" + " at"+now.toString());
                            }
                            if(h.isNotification()){
                                showNotification(now.toString()+":"+h.getHost() + " had I/O problem");
                            }
                            notifyState(h);
                            break;
                        case Util.DEVICE_NOT_CONNECTED:
                            setLastCheckResult(false);
                            Log.i("TAG", "Device' connection failure");
                            if(h.isEmails()){
                                sentReportToEverybody(h.getAllEmails(), h.getHost() +" had I/O problem for your device connection" + " at"+now.toString());
                            }
                            if(h.isNotification()){
                                showNotification(now.toString()+":"+h.getHost() + " had I/O problem for your device connection");
                            }
                            notifyState(h);
                            break;
                    }


                }
            }
        });
        thread.start();


    }

    private void notifyState(Host h){
        //TODO add states to show the right light
        //TODO add host id to update the right row
        //checkResult.setImageResource(R.mipmap.gray);
        Intent i = new Intent();
        i.setAction(Constants.INTENT_ACTION_UPDATE);
        mContext.sendBroadcast(i);
    }

    private void sentReportToEverybody(final List<Email> allEmails, String s) {
        final String report = Util.getDeviceData(mContext, s, null);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    GMailSender sender = new GMailSender("aadroidreports@gmail.com", "AAIr3l4nd");
                    sender.sendMail("ServerChecker Error Report",
                            report,
                            "aadroidreports@gmail.com",
                            Util.getStringFromList(allEmails));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    private void setLastCheckResult(boolean lastCheck) {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        sharedPrefs.edit().putBoolean(Constants.LAST_CHECK, lastCheck).apply();
    }

    private boolean getLastCheckResult() {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        return sharedPrefs.getBoolean(Constants.LAST_CHECK, true);
    }

    private Config getParametersFromPreferences(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        int i = sharedPrefs.getInt(Constants.INTERVAL, 1000);//1 seg default
        int t = sharedPrefs.getInt(Constants.TIME_UNIT, 0);//hour default
        String u = sharedPrefs.getString(Constants.SITE_URL, null);
        int p = sharedPrefs.getInt(Constants.SITE_PORT, 80);//default port
        String e = sharedPrefs.getString(Constants.EMAIL, null);
        if (u != null) {
            return new Config(i, t, u, p, e);
        } else {
            return null;
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
        if (mConfig == null) {
            mConfig = getParametersFromPreferences(context);
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

    //TODO connect with real email address for that url
    public void sendErrorReport(String txt, String url) {
        DatabaseHelper db = new DatabaseHelper(mContext);
        List<Email> emails = db.getEmailsByHost(url);

        final String report = Util.getDeviceData(mContext, txt, url);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    GMailSender sender = new GMailSender("aadroidreports@gmail.com", "AAIr3l4nd");
                    sender.sendMail("ServerChecker Error Report",
                            report,
                            "aadroidreports@gmail.com",
                            "lamenza@gmail.com, mariolamenza@gmail.com, mariolamenza@hotmail");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();


    }

    public void sendMassiveErrorReport(String txt, String url) {
        DatabaseHelper db = new DatabaseHelper(mContext);
        List<Email> emails = db.getEmailsByHost(url);

        final String report = Util.getDeviceData(mContext, txt, url);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    GMailSender sender = new GMailSender("aadroidreports@gmail.com", "AAIr3l4nd");
                    sender.sendMail("ServerChecker Error Report",
                            report,
                            "aadroidreports@gmail.com",
                            "lamenza@gmail.com, mariolamenza@gmail.com, mariolamenza@hotmail");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();


    }
}