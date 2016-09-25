package com.lm2a.serverchecker.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lemenzm on 08/09/2016.
 */
public class Host {
    long id;
    String host;
    boolean notification;
    boolean emails;
    boolean lastCheck;
    List<String> allEmails;
    public Host() {
    }

    public List<String> getAllEmails() {
        return allEmails;
    }

    public void setAllEmails(List<String> allEmails) {
        this.allEmails = allEmails;
    }

    public Host(boolean emails, String host, long id, boolean notification, boolean lastCheck, List<String> allEmails) {
        this.emails = emails;
        this.host = host;
        this.id = id;
        this.notification = notification;
        this.lastCheck = lastCheck;
        this.allEmails = allEmails;

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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isNotification() {
        return notification;
    }

    public void setNotification(boolean notification) {
        this.notification = notification;
    }

    public boolean isLastCheck() {
        return lastCheck;
    }

    public void setLastCheck(boolean lastCheck) {
        this.lastCheck = lastCheck;
    }
}
