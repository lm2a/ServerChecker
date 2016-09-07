package com.lm2a.serverchecker.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.BatteryManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.lm2a.serverchecker.MainActivity;

public class BackgroundService extends Service {

    private static final String TAG = "BackgroundService";
    static final public String MESSAGE = "com.lm2a.serverchecker.services.MGS";

    PeriodicTaskReceiver mPeriodicTaskReceiver = new PeriodicTaskReceiver();
    LocalBroadcastManager mBroadcaster;

    public void sendResult() {
        if(MainActivity.active) {//if activity is running
            Intent intent = new Intent(MESSAGE);
            intent.putExtra(MESSAGE, "update");
            mBroadcaster.sendBroadcast(intent);
        }
    }


    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(Constants.INTENT_ACTION_UPDATE)){
                //action for sms received
                sendResult();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.INTENT_ACTION_UPDATE); //further more
        registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mBroadcaster = LocalBroadcastManager.getInstance(this);

        Log.e(TAG, "BackgroundService starting...");
        if((intent!=null)&&(intent.getExtras()!=null)) {

            int x = intent.getExtras().getInt("Messenger");
            if(x== Constants.MSG_STOP){
                Log.e(TAG, "Stopping checks...");
                mPeriodicTaskReceiver.stopPeriodicTaskHeartBeat(this);
                this.stopSelf();
            }
        }else {
            Log.e(TAG, "Starting checks...");
            SharedPreferences sharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(this);
            IntentFilter batteryStatusIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatusIntent = registerReceiver(null, batteryStatusIntentFilter);

            if (batteryStatusIntent != null) {
                int level = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                float batteryPercentage = level / (float) scale;
                float lowBatteryPercentageLevel = 0.14f;

                try {
                    int lowBatteryLevel = Resources.getSystem().getInteger(Resources.getSystem().getIdentifier("config_lowBatteryWarningLevel", "integer", "android"));
                    lowBatteryPercentageLevel = lowBatteryLevel / (float) scale;
                } catch (Resources.NotFoundException e) {
                    Log.e(TAG, "Missing low battery threshold resource");
                }

                sharedPreferences.edit().putBoolean(Constants.BACKGROUND_SERVICE_BATTERY_CONTROL, batteryPercentage >= lowBatteryPercentageLevel).apply();
            } else {
                sharedPreferences.edit().putBoolean(Constants.BACKGROUND_SERVICE_BATTERY_CONTROL, true).apply();
            }

            mPeriodicTaskReceiver.restartPeriodicTaskHeartBeat(this);
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind (Intent intent)
    {
        return null;
    }

//    @Override
//    public void onReceiveMessage(Message msg) {
//        if (msg.what == MSG_STOP) {
//            mPeriodicTaskReceiver.stopPeriodicTaskHeartBeat(this);
//        }
//    }


}