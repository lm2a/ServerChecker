package com.lm2a.serverchecker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.lm2a.serverchecker.database.DatabaseHelper;
import com.lm2a.serverchecker.database.HostsAdapter;
import com.lm2a.serverchecker.model.Email;
import com.lm2a.serverchecker.model.Host;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lemenzm on 08/09/2016.
 */
public class DataBaseActivity extends Activity {

    // Database Helper
    DatabaseHelper db;
    Host mHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hosts);

        ListView listView = (ListView) findViewById(R.id.listView);

        Button add = (Button) findViewById(R.id.btnAddHost);
        final EditText url = (EditText) findViewById(R.id.url);
        final EditText port = (EditText) findViewById(R.id.port);
        final CheckBox notification = (CheckBox)findViewById(R.id.notification);
        final CheckBox email = (CheckBox)findViewById(R.id.email);

        final ImageButton addEmail = (ImageButton) findViewById(R.id.addButton);
        final TextView label =  (TextView) findViewById(R.id.textView);

        db = new DatabaseHelper(getApplicationContext());
        List<Host> hosts = db.getAllHosts();

        addEmail.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                List<Email> emails = db.getEmailsByHost(mHost.getHost());
                String[] stringArray = getArrayFromList(emails);
/*              Custom View
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
// ...Irrelevant code for customizing the buttons and title
                LayoutInflater inflater = this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.alert_label_editor, null);
                dialogBuilder.setView(dialogView);

                EditText editText = (EditText) dialogView.findViewById(R.id.label_field);
                editText.setText("test label");
                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();*/


                AlertDialog.Builder builderSingle = new AlertDialog.Builder(DataBaseActivity.this);
                builderSingle.setIcon(R.drawable.cast_ic_notification_small_icon);
                builderSingle.setTitle("Select One Name:-");

                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                        DataBaseActivity.this,
                        android.R.layout.select_dialog_singlechoice);
                arrayAdapter.addAll(stringArray);
                db.closeDB();
                builderSingle.setNegativeButton(
                        "cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                builderSingle.setAdapter(
                        arrayAdapter,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String strName = arrayAdapter.getItem(which);
                                AlertDialog.Builder builderInner = new AlertDialog.Builder(
                                        DataBaseActivity.this);
                                builderInner.setMessage(strName);
                                builderInner.setTitle("Your Selected Item is");
                                builderInner.setPositiveButton(
                                        "Ok",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                builderInner.show();
                            }
                        });
                builderSingle.show();            }
        });

        email.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //is chkIos checked?
                if (((CheckBox) v).isChecked()) {
                    addEmail.setVisibility(View.VISIBLE);
                    label.setVisibility(View.VISIBLE);
                } else {
                    addEmail.setVisibility(View.INVISIBLE);
                    label.setVisibility(View.INVISIBLE);
                }
            }
        });



// Create the adapter to convert the array to views
        HostsAdapter adapter = new HostsAdapter(this, (ArrayList<Host>) hosts);


        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3)
            {
                mHost = (Host)adapter.getItemAtPosition(position);
                url.setText(mHost.getHost());
                port.setText(mHost.getPort());
                notification.setChecked(mHost.isNotification());
                email.setChecked(mHost.isEmails());
                // assuming string and if you want to get the value on click of list item
                // do what you intend to do on click of listview row
            }
        });
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

//        List<Email> emails = db.getEmailsByHost("www.lm2a3.com");
//        Log.i("Bebay: ", emails.toString());
//        // Don't forget to close database connection
//        db.closeDB();

    }

    public String[] getArrayFromList(List<Email> emails){
        String[] a = new String[emails.size()];

        for (int i = 0; i < emails.size(); i++) {
            Email e = emails.get(i);
            a[i] = e.getEmail();
        }

        return a;
    }
}