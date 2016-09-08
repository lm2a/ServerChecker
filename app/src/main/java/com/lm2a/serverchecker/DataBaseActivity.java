package com.lm2a.serverchecker;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.lm2a.serverchecker.database.DatabaseHelper;
import com.lm2a.serverchecker.model.Email;
import com.lm2a.serverchecker.model.Host;

import java.util.List;

/**
 * Created by lemenzm on 08/09/2016.
 */
public class DataBaseActivity extends Activity {

    // Database Helper
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(getApplicationContext());

/*
        Email tag1 = new Email("bebay1@krebay.com");
        Email tag2 = new Email("bebay2@krebay.com");
        Email tag3 = new Email("bebay3@krebay.com");
        Email tag4 = new Email("bebay4@krebay.com");
        Email tag5 = new Email("bebay5@krebay.com");
        Email tag6 = new Email("bebay6@krebay.com");
        Email tag7 = new Email("bebay7@krebay.com");
        Email tag8 = new Email("bebay8@krebay.com");

        // Inserting tags in db
        long tag1_id = db.createEmail(tag1);
        long tag2_id = db.createEmail(tag2);
        long tag3_id = db.createEmail(tag3);
        long tag4_id = db.createEmail(tag4);
        long tag5_id = db.createEmail(tag5);
        long tag6_id = db.createEmail(tag6);
        long tag7_id = db.createEmail(tag7);
        long tag8_id = db.createEmail(tag8);

        Log.d("Email Count", "Email Count: " + db.getAllEmails().size());

        Host todo1 = new Host(true, "www.lm2a1.com", 0, true, "80");
        Host todo2 = new Host(true, "www.lm2a2.com", 0, true, "80");
        Host todo3 = new Host(true, "www.lm2a3.com", 0, true, "80");

        long todo11_id = db.createHost(todo1);
        long todo12_id = db.createHost(todo2);
        long todo13_id = db.createHost(todo3);

        db.createHostEmail(todo11_id, tag1_id);//long host_id, long email_id)
        db.createHostEmail(todo11_id, tag2_id);
        db.createHostEmail(todo11_id, tag3_id);
        db.createHostEmail(todo12_id, tag1_id);
        db.createHostEmail(todo12_id, tag4_id);
        db.createHostEmail(todo13_id, tag6_id);
        db.createHostEmail(todo13_id, tag7_id);
        db.createHostEmail(todo13_id, tag5_id);
        db.createHostEmail(todo13_id, tag8_id);*/

        List<Email> emails = db.getEmailsByHost("www.lm2a3.com");
        Log.i("Bebay: ", emails.toString());
        // Don't forget to close database connection
        db.closeDB();

    }
}