package com.lm2a.serverchecker.model;

public class Config {


    public int interval;
    public int timeUnit;
    public String url;
    public String email;

        public Config(int interval, int timeUnit, String url, String email) {
            this.interval = interval;
            this.timeUnit = timeUnit;
            this.url = url;
            this.email = email;
        }
    }
