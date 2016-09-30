package com.lm2a.serverchecker;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.lm2a.serverchecker.billing.util.IabBroadcastReceiver;
import com.lm2a.serverchecker.billing.util.IabHelper;
import com.lm2a.serverchecker.billing.util.IabResult;
import com.lm2a.serverchecker.billing.util.Inventory;
import com.lm2a.serverchecker.billing.util.Purchase;
import com.lm2a.serverchecker.database.DatabaseHelper;
import com.lm2a.serverchecker.model.Config;
import com.lm2a.serverchecker.model.Email;
import com.lm2a.serverchecker.model.Host;
import com.lm2a.serverchecker.services.BackgroundService;
import com.lm2a.serverchecker.services.Constants;
import com.lm2a.serverchecker.utils.CustomAdapter;
import com.lm2a.serverchecker.utils.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivityPro extends AppCompatActivity {

    public static boolean active = false;


    private static final String TAG = "MainActivity";
    private RadioGroup mRadioGroup;
    private NumberPicker mNumberPicker;
    private Button mStart;
    private ImageView mLastCheck;

    // Does the user have the premium upgrade?
    boolean mIsPremium = false;
    // SKUs for our products: the premium upgrade (non-consumable)
    static final String SKU_PREMIUM = "serverchecker.sku.premium";
    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;
    // The helper object
    IabHelper mHelper;
    // Provides purchase notification while this app is running
    IabBroadcastReceiver mBroadcastReceiver;
    BroadcastReceiver mUpdateBroadcastReceiver;

    private int timeUnit = -1;
    private int timeChoosed = -1;
    private boolean notification, email;

    @Override
    public void onStart() {
        super.onStart();
        active = true;
        //registering to receive updates from Service
        LocalBroadcastManager.getInstance(this).registerReceiver((mUpdateBroadcastReceiver),
                new IntentFilter(BackgroundService.MESSAGE));
    }

    @Override
    public void onStop() {
        active = false;
        //un-registering to receive updates from Service
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mUpdateBroadcastReceiver);
        super.onStop();
    }

    boolean started = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //--------UI update------------------------

        mUpdateBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //String s = intent.getStringExtra(BackgroundService.MESSAGE);
                // do something here.
                if (started) {
                    updateUIWithLastCheck();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.INTENT_ACTION__SERVICE_UPDATE); //further more
        registerReceiver(mUpdateBroadcastReceiver, filter);
        //-----------------------------------------
        updateUi();
    }

    // updates UI to reflect model
    public void updateUi() {
        //TODO the real one if(mIsPremium){
        Config c = getParametersFromPreferences();

        setContentView(R.layout.activity_pro_main);
        setUiPro(c);

        started = true;
        // update the car color to reflect premium status or lack thereof
        //TODO ((ImageView)findViewById(R.id.free_or_premium)).setImageResource(mIsPremium ? R.drawable.premium : R.drawable.free);

        // "Upgrade" button is only visible if the user is not premium
        //TODO  findViewById(R.id.upgrade_button).setVisibility(mIsPremium ? View.GONE : View.VISIBLE);

    }

    // Enables or disables the "please wait" screen.
    void setWaitScreen(boolean set) {
        //TODO findViewById(R.id.screen_main).setVisibility(set ? View.GONE : View.VISIBLE);
        //TODO findViewById(R.id.screen_wait).setVisibility(set ? View.VISIBLE : View.GONE);
    }

    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();
    }


    ListView mHostsList;
    CustomAdapter mHostAdapter;
    List<Host> mHosts;

    private void setUiPro(final Config config) {
        mRadioGroup = (RadioGroup) findViewById(R.id.radioButtonTimeUnit);
        mNumberPicker = (NumberPicker) findViewById(R.id.numberPickerTime);
        mNumberPicker.setMinValue(0);
        mNumberPicker.setMaxValue(30);
        mNumberPicker.setWrapSelectorWheel(false);
        mHostsList = (ListView) findViewById(R.id.servers);

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        mHosts = databaseHelper.getAllHosts();

        mHostAdapter = new CustomAdapter(this, mHosts, getResources());
        mHostsList.setAdapter(mHostAdapter);
        mHostsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showHostsDialog(mHosts.get(i));
            }

        });
        Button add = (Button) findViewById(R.id.server);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHostsDialog(null);
            }
        });

    }


    private Config getParametersFromPreferences() {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        int i = sharedPrefs.getInt(Constants.INTERVAL, 1000);//1 seg default
        int t = sharedPrefs.getInt(Constants.TIME_UNIT, 0);//hour default
        String u = sharedPrefs.getString(Constants.SITE_URL, null);
        String e = sharedPrefs.getString(Constants.EMAIL, null);
        boolean l = sharedPrefs.getBoolean(Constants.LAST_CHECK, true);

        if (u != null) {
            return new Config(i, t, u, e);
        } else {
            return null;
        }
    }

    private void updateUIWithLastCheck() {
        if (started) {
            SharedPreferences sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(this);
            boolean l = sharedPrefs.getBoolean(Constants.LAST_CHECK, true);
            if (mLastCheck != null) {
                if (l) {
                    mLastCheck.setImageResource(R.mipmap.green);
                } else {
                    mLastCheck.setImageResource(R.mipmap.red);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.stop:
                Intent intent = new Intent(MainActivityPro.this, BackgroundService.class);
                intent.putExtra("Messenger", Constants.STOP_SERVICE);
                startService(intent);
                break;
        }
        return true;
    }


    // We're being destroyed. It's important to dispose of the helper here!
    @Override
    public void onDestroy() {
        super.onDestroy();

        // very important:
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }

        // very important:
        Log.d(TAG, "Destroying helper.");
        if (mHelper != null) {
            mHelper.disposeWhenFinished();
            mHelper = null;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        updateUIWithLastCheck();
    }

    boolean mNotificationOk, mEmailOk;


    private void showHostsDialog(final Host host) {
        //Preparing views
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.hosts, null);
        //layout_root should be the name of the "top-level" layout node in the dialog_layout.xml file.
        final EditText site = (EditText) layout.findViewById(R.id.site);
        //final EditText port = (EditText) layout.findViewById(R.id.port);
        final CheckBox notification = (CheckBox) layout.findViewById(R.id.notification);
        final CheckBox email = (CheckBox) layout.findViewById(R.id.email);
        final Button checkNow = (Button) layout.findViewById(R.id.checknow);
        final EditText emailsArea = (EditText) layout.findViewById(R.id.emails_area);
        final Button add = (Button) layout.findViewById(R.id.add_site);
        final ImageView checkResult = (ImageView) layout.findViewById(R.id.check_result);
        final Button delete = (Button) layout.findViewById(R.id.delete_site);
        final Button update = (Button) layout.findViewById(R.id.update_site);
        if (host != null) {
            delete.setVisibility(View.VISIBLE);
            update.setVisibility(View.VISIBLE);
            add.setVisibility(View.GONE);
            site.setText(host.getHost());
            notification.setChecked(host.isNotification());
            email.setChecked(host.isEmails());
            if (host.isEmails()) {
                emailsArea.setVisibility(View.VISIBLE);
            } else {
                emailsArea.setVisibility(View.GONE);
            }
            emailsArea.setText(Util.getStringFromList(host.getAllEmails()));
        } else {
            delete.setVisibility(View.GONE);
            update.setVisibility(View.GONE);
            add.setVisibility(View.VISIBLE);
        }
        //Building dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(layout);


        notification.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //is chkIos checked?
                if (((CheckBox) v).isChecked()) {
                    mNotificationOk = true;
                } else {
                    mNotificationOk = false;
                    emailsArea.setVisibility(View.GONE);
                }
            }
        });

        email.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //is chkIos checked?
                if (((CheckBox) v).isChecked()) {
                    mEmailOk = true;
                    emailsArea.setVisibility(View.VISIBLE);
                } else {
                    mEmailOk = false;
                    emailsArea.setVisibility(View.GONE);
                }
            }
        });

        checkNow.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final String url = site.getText().toString();

                if ((url != null) && (!url.isEmpty())) {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //mCheckOk = Util.isReachable(url, p, 30000);
                            mConnectionCheckResult = Util.isServerReachable(MainActivityPro.this, url);
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    switch (mConnectionCheckResult) {
                                        case Util.SERVER_OK:
                                            checkResult.setImageResource(R.mipmap.green);
                                            break;
                                        case Util.SERVER_KO:
                                            checkResult.setImageResource(R.mipmap.red);
                                            break;
                                        case Util.URL_MALFORMED:
                                            checkResult.setImageResource(R.mipmap.gray);
                                            site.setError("URL Malformed, check it, please");
                                            break;
                                        case Util.IO_FAILURE:
                                            Toast.makeText(MainActivityPro.this, "I/O problem, please try again", Toast.LENGTH_SHORT).show();
                                            checkResult.setImageResource(R.mipmap.gray);
                                            break;
                                        case Util.DEVICE_NOT_CONNECTED:
                                            Toast.makeText(MainActivityPro.this, "Check your device' connection, please", Toast.LENGTH_SHORT).show();
                                            checkResult.setImageResource(R.mipmap.gray);
                                            break;
                                    }
                                }
                            });

                        }
                    });
                    t.start();

                }
            }
        });

        add.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final String url = site.getText().toString();
                if ((url != null) && (!url.isEmpty())) {
                    //save host and emails
                    Host host = new Host();
                    host.setHost(url);
                    if (mNotificationOk) {
                        host.setNotification(true);
                    } else {
                        host.setNotification(false);
                    }
                    if (mEmailOk) {
                        host.setEmails(true);
                        List<Email> emails = Util.getEmailsFromTextArea(emailsArea.getText().toString());
                        host.setAllEmails(emails);
                    } else {
                        host.setEmails(false);
                    }
                    DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                    db.createHost(host);
                    mHosts = db.getAllHosts();
                    mHostAdapter.notifyDataSetChanged();
                    mAlertDialog.cancel();
                }
            }
        });
        update.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final String url = site.getText().toString();
                if ((url != null) && (!url.isEmpty())) {
                    DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                    //save host and emails
                    host.setHost(url);
                    if (notification.isChecked()) {
                        host.setNotification(true);
                    } else {
                        host.setNotification(false);
                    }
                    if (email.isChecked()) {
                        host.setEmails(true);
                        List<Email> emails = Util.getEmailsFromTextArea(emailsArea.getText().toString());
                        host.setAllEmails(emails);
                    } else {
                        host.setEmails(false);
                        host.setAllEmails(null);
                    }

                    db.deleteHostEmail(host.getId());
                    db.updateHost(host);
                    mHosts = db.getAllHosts();
                    mHostAdapter.notifyDataSetChanged();
                    mAlertDialog.cancel();
                }
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                //save host and emails
                if (mEmailOk) {
                    db.deleteHostEmail(host.getId());
                }
                db.deleteHost(host.getId());
                mHosts = db.getAllHosts();
                mHostAdapter.notifyDataSetChanged();
                mAlertDialog.cancel();
            }
        });
        mAlertDialog = builder.create();
        mAlertDialog.show();
    }

    AlertDialog mAlertDialog;// = builder.create();

    int mConnectionCheckResult = -1;


}
