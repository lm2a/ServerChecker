package com.lm2a.serverchecker.model;

/**
 * Created by lemenzm on 08/09/2016.
 */
public class Host {
    int id;
    String host;
    String port;
    boolean notification;
    boolean emails;

    public Host(boolean emails, String host, int id, boolean notification, String port) {
        this.emails = emails;
        this.host = host;
        this.id = id;
        this.notification = notification;
        this.port = port;
    }

    public boolean isEmails() {
        return emails;
    }

    public void setEmails(boolean emails) {
        this.emails = emails;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isNotification() {
        return notification;
    }

    public void setNotification(boolean notification) {
        this.notification = notification;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
