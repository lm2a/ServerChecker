package com.lm2a.serverchecker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.lm2a.serverchecker.model.Config;
import com.lm2a.serverchecker.model.Email;
import com.lm2a.serverchecker.services.Constants;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lemenzm on 10/09/2016.
 */
public class Util {

    public static final int SERVER_OK = 0;
    public static final int SERVER_KO = 1;
    public static final int URL_MALFORMED = 2;
    public static final int IO_FAILURE = 3;
    public static final int DEVICE_NOT_CONNECTED = 4;

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
                .append("ServerChecker App Version: "+versionName+"\n")
                .append("Connection State: "+connectionState+"\n");
        if(url!=null){
            stringBuffer.append("Tech Message: "+techMessage+" - problem raise trying to reach "+url+"\n");
        }else{
            stringBuffer.append("Tech Message: "+techMessage+"\n");
        }

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


    public static boolean isReachable(String addr, int timeOutMillis) {
        // Any Open port on other machine
        // openPort =  22 - ssh, 80 or 443 - webserver, 25 - mailserver etc.
        boolean b = false;
        try {
            Socket soc = new Socket();
            URL  site = new URL(addr);
            soc.connect(new InetSocketAddress(addr, site.getPort()), timeOutMillis);
            //}
            b = true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException ex) {
            b = false;
        }
        return b;
    }




    static public int isServerReachable(Context context, String url) {
        ConnectivityManager connMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMan.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            try {
                URL urlServer = new URL(url);
                HttpURLConnection urlConn = (HttpURLConnection) urlServer.openConnection();
                urlConn.setConnectTimeout(3000); //<- 3Seconds Timeout
                urlConn.connect();
                if (urlConn.getResponseCode() == 200) {
                    return SERVER_OK;
                } else {
                    return SERVER_KO;
                }
            } catch (MalformedURLException e1) {
                return URL_MALFORMED;
            } catch (IOException e) {
                return IO_FAILURE;
            }
        }
        return DEVICE_NOT_CONNECTED;
    }



    public static String getStringFromList(List<Email> emails){
        StringBuffer sb = new StringBuffer();
        for(Email e: emails){
            sb.append(e.getEmail() + ", ");
        }
        String x = sb.toString();
        String r = x.substring(0, x.length()-2);
        return r;
    }

    public static List<Email> getEmailsFromTextArea(String allTogether){
        List<String> emails = Arrays.asList(allTogether.split("\\s*,\\s*"));
        List<Email> es = new ArrayList<>();
        for(String e: emails){
            Email email = new Email();
            email.setEmail(e);
            es.add(email);
        }
        return es;
    }

    public static void setParametersOnPreferences(Context context, int interval, int timeUnit, String url, String email){
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        sharedPrefs.edit().putInt(Constants.INTERVAL, interval).apply();
        sharedPrefs.edit().putInt(Constants.TIME_UNIT, timeUnit).apply();
        sharedPrefs.edit().putString(Constants.SITE_URL, url).apply();
        sharedPrefs.edit().putString(Constants.EMAIL, email).apply();
    }

    public static Config getParametersFromPreferences(Context context){
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        int i = sharedPrefs.getInt(Constants.INTERVAL, 1000);//1 seg default
        int t = sharedPrefs.getInt(Constants.TIME_UNIT, 0);//hour default
        String u = sharedPrefs.getString(Constants.SITE_URL, null);
        String e = sharedPrefs.getString(Constants.EMAIL, null);
        boolean l = sharedPrefs.getBoolean(Constants.LAST_CHECK, true);

        if(u!=null){
            return new Config(i, t, u, e);
        }else{
            return null;
        }
    }

    public static void setUserTypePro(Context context, boolean pro){
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        sharedPrefs.edit().putBoolean(Constants.USER_TYPE, pro).apply();
    }

    public static boolean getUserTypePro(Context context){
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        boolean u = sharedPrefs.getBoolean(Constants.USER_TYPE, false);
        return u;
    }
}
