package com.lm2a.serverchecker.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;

/**
 * Created by lemenzm on 10/09/2016.
 */
public class Util {

    public static String getDeviceData(Context context, String techMessage, String url) {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = tm.getSimCountryIso();
        String versionName = null;
        try {
            versionName = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String connectionState;
        if(isOnline(context)){
            connectionState="Connection OK";
        }else{
            connectionState="Connection NOK";
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("\n"+"Manufacturer: "+manufacturer+"\n")
                .append("Model: "+model+"\n")
                .append("Current Api Version: "+currentapiVersion+"\n")
                .append("Model: "+model+"\n")
                .append("Country Code: "+countryCode+"\n")
                .append("AA App Version: "+versionName+"\n")
                .append("Connection State: "+connectionState+"\n")
                .append("Tech Message: "+techMessage+" - problem raise trying to reach "+url+"\n");
        return stringBuffer.toString();
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }
}
