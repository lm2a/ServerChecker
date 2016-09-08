package com.lm2a.serverchecker.model;

/**
 * Created by lemenzm on 08/09/2016.
 */
public class Email {
    int id;
    String email;

    public Email() {
    }

    public Email(String email) {
        this.email = email;
    }

    public Email(String email, int id) {
        this.email = email;
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
