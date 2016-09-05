package com.lm2a.serverchecker.services;

public class Constants {
    public static final String BACKGROUND_SERVICE_BATTERY_CONTROL = new String("battery_service");
    public static final int MSG_STOP = 1;
    public static final String TIME_UNIT = "time_unit";
    public static final int SECONDS = 2;
    public static final int HOURS = 0;
    public static final int MINUTES = 1;
    public static final String INTERVAL = "interval";
    public static final String SITE_URL = "site_url";
    public static final String EMAIL = "email";
    public static final String LAST_CHECK = "last_check";
    public static final long T_HOURS = 60*60*1000;
    public static final long T_MINUTES = 60*1000;
    public static final long T_SECONDS = 1000;
    public static final int STOP_SERVICE = 1;
    static final String INTENT_ACTION = "com.lm2a.serverchecker.PERIODIC_TASK_HEART_BEAT";
}