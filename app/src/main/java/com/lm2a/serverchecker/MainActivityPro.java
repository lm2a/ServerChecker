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

public class MainActivityPro extends AppCompatActivity implements IabBroadcastReceiver.IabBroadcastListener,
        View.OnClickListener {

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
    private int timeChoosed=-1;
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

        //-----------------------------------------
        Eula.show(this);

        //-----------------------------------------

        //--------UI update------------------------

        mUpdateBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //String s = intent.getStringExtra(BackgroundService.MESSAGE);
                // do something here.
                if(started) {
                    updateUIWithLastCheck();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.INTENT_ACTION__SERVICE_UPDATE); //further more
        registerReceiver(mUpdateBroadcastReceiver, filter);
        //-----------------------------------------

        //------billing----------------------------
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAk9JyxmgtZSgXlEyXFV6HiYj1cI0KoXh+OLbeqQxn/DVFcn4ZLglFF6LqFO1H4lb2DMTfVYuiS5gK6LpOaFKC71SDdbsx0eFf76xmQQAEPjsDVa0kGjC2OHl11MOuyiy9AkWLi90lFPIbJxns/ir9amC6gsOpqndpCRnqgYBIAlXf480pmg/StHTHQeehTDeTVnXk8R44ibQt0d8rIApaqXPbHY4je6v8Jxsnm9EiUiP7RmysA00WBCzASGZll+R4NHPPS4i8DN3NOcWytvzPpfTbHiNoeJnctmQnyapJzp0zDDOHJ6xxrddfg6ZJbqERSYvEUBHKq3s717w5Izt22QIDAQAB";
        // Create the helper, passing it our context and the public key to verify signatures with
        Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(true);
        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    complain("Problem setting up in-app billing: " + result);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

                // Important: Dynamically register for broadcast messages about updated purchases.
                // We register the receiver here instead of as a <receiver> in the Manifest
                // because we always call getPurchases() at startup, so therefore we can ignore
                // any broadcasts sent while the app isn't running.
                // Note: registering this listener in an Activity is a bad idea, but is done here
                // because this is a SAMPLE. Regardless, the receiver must be registered after
                // IabHelper is setup, but before first call to getPurchases().
                mBroadcastReceiver = new IabBroadcastReceiver(MainActivityPro.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                registerReceiver(mBroadcastReceiver, broadcastFilter);

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "Setup successful. Querying inventory.");
                try {
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    complain("Error querying inventory. Another async operation in progress.");
                }
            }
        });
        //----------------------------------------------------------------------------------------

    }

    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                return;
            }

            Log.d(TAG, "Query inventory was successful.");

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

            // Do we have the premium upgrade?
            Purchase premiumPurchase = inventory.getPurchase(SKU_PREMIUM);
            mIsPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
            Log.d(TAG, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));
            updateUi();
            setWaitScreen(false);
            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };

    /** Verifies the developer payload of a purchase. */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }

    // updates UI to reflect model
    public void updateUi() {
        //TODO the real one if(mIsPremium){
        Config c = getParametersFromPreferences();

        if(Constants.MODE_PRO){
            setContentView(R.layout.activity_pro_main);
            setUiPro(c);
        }else{
            setContentView(R.layout.activity_main);
            //------admob----------------------------
            AdView mAdView = (AdView) findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
            setUI(c);
        }


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

    // User clicked the "Upgrade to Premium" button.
    public void onUpgradeAppButtonClicked() {
        Log.d(TAG, "Upgrade button clicked; launching purchase flow for upgrade.");
        setWaitScreen(true);

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
        String payload = "";

        try {
            mHelper.launchPurchaseFlow(this, SKU_PREMIUM, RC_REQUEST,
                    mPurchaseFinishedListener, payload);
        } catch (IabHelper.IabAsyncInProgressException e) {
            complain("Error launching purchase flow. Another async operation in progress.");
            setWaitScreen(false);
        }
    }

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                complain("Error purchasing: " + result);
                setWaitScreen(false);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");
                setWaitScreen(false);
                return;
            }

            Log.d(TAG, "Purchase successful.");

            if (purchase.getSku().equals(SKU_PREMIUM)) {
                // bought the premium upgrade!
                Log.d(TAG, "Purchase is premium upgrade. Congratulating user.");
                alert("Thank you for upgrading to premium!");
                mIsPremium = true;
                updateUi();
                setWaitScreen(false);
            }
        }
    };

    ListView mHostsList;
    CustomAdapter mHostAdapter;
    List<Host> mHosts;

    private void setUiPro(final Config config){
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

    private void setUI(final Config config) {
        mRadioGroup = (RadioGroup) findViewById(R.id.radioButtonTimeUnit);
        mNumberPicker = (NumberPicker) findViewById(R.id.numberPickerTime);
        mNumberPicker.setMinValue(0);
        mNumberPicker.setMaxValue(30);
        mNumberPicker.setWrapSelectorWheel(false);

        mLastCheck = (ImageView) findViewById(R.id.lastCheck);
        updateUIWithLastCheck();
        LinearLayout linearButton = (LinearLayout) findViewById(R.id.linearButton);
        linearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onUpgradeAppButtonClicked();
            }
        });


        //final EditText editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        final EditText site = (EditText) findViewById(R.id.site);
        final EditText port = (EditText) findViewById(R.id.port);

        mStart = (Button) findViewById(R.id.start);

        mNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                // TODO Auto-generated method stub
                timeChoosed = newVal;
            }
        });

        mLastCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent i = new Intent(MainActivityPro.this, DataBaseActivity.class);
//                startActivity(i);
            }
        });

        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //String e = editTextEmail.getText().toString();
                String u = site.getText().toString();
                int p = new Integer(port.getText().toString());

                if(timeChoosed<0){
                    timeChoosed=config.interval;
                    timeUnit=config.timeUnit;
                }
                Log.i("TAG", "" + timeChoosed + " - " + timeUnit + " - " + notification + " - " + email);
                setParametersOnPreferences(timeChoosed, timeUnit, u, p, null);
                //process();

                Intent startServiceIntent = new Intent(MainActivityPro.this, BackgroundService.class);
                if(startService(startServiceIntent) != null){
                    Toast.makeText(getBaseContext(), "Service is already running, restarting", Toast.LENGTH_SHORT).show();
                    stopService(startServiceIntent);
                    startService(startServiceIntent);
                }else{
                    Toast.makeText(getBaseContext(), "There is no service running, starting service..", Toast.LENGTH_SHORT).show();
                }

            }
        });

        CheckBox chkNotification = (CheckBox) findViewById(R.id.checkBoxNotification);
        //CheckBox chkEmail = (CheckBox) findViewById(R.id.checkBoxEmail);

        chkNotification.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //is chkIos checked?
                if (((CheckBox) v).isChecked()) {
                    notification = true;
                } else {
                    notification = false;
                }

            }
        });

//        chkEmail.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                //is chkIos checked?
//                if (((CheckBox) v).isChecked()) {
//                    email = true;
//                } else {
//                    email = false;
//                }
//
//            }
//        });


        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if (checkedId == R.id.radioButtonHour) {
                    timeUnit = Constants.HOURS;
                } else if (checkedId == R.id.radioButtonMinute) {
                    timeUnit = Constants.MINUTES;
                } else {
                    timeUnit = Constants.SECONDS;
                }
            }

        });

        //fill the form if there are values on storage
        if(config!=null){
            mNumberPicker.setValue(config.interval);//picker

            if(config.timeUnit== Constants.SECONDS){//options
                mRadioGroup.check(R.id.radioButtonSecond);
            }else if(config.timeUnit== Constants.MINUTES){
                mRadioGroup.check(R.id.radioButtonMinute);
            }else{//HOURS by default
                mRadioGroup.check(R.id.radioButtonHour);
            }
            //editTextEmail.setText(config.email);
            site.setText(config.url);
            StringBuffer sb = new StringBuffer();
            sb.append(config.port);
            String x = sb.toString();
            port.setText(new String(x));
        }

    }


    public void updateUI(boolean lastCheck){

    }


    private void setParametersOnPreferences(int interval, int timeUnit, String url, int p, String email){
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        sharedPrefs.edit().putInt(Constants.INTERVAL, interval).apply();
        sharedPrefs.edit().putInt(Constants.TIME_UNIT, timeUnit).apply();
        sharedPrefs.edit().putString(Constants.SITE_URL, url).apply();
        sharedPrefs.edit().putInt(Constants.SITE_PORT, p).apply();
        sharedPrefs.edit().putString(Constants.EMAIL, email).apply();
    }

    private Config getParametersFromPreferences(){
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        int i = sharedPrefs.getInt(Constants.INTERVAL, 1000);//1 seg default
        int t = sharedPrefs.getInt(Constants.TIME_UNIT, 0);//hour default
        String u = sharedPrefs.getString(Constants.SITE_URL, null);
        int p = sharedPrefs.getInt(Constants.SITE_PORT, 80);//default port
        String e = sharedPrefs.getString(Constants.EMAIL, null);
        boolean l = sharedPrefs.getBoolean(Constants.LAST_CHECK, true);

        if(u!=null){
            return new Config(i, t, u, p, e);
        }else{
            return null;
        }
    }

    private void updateUIWithLastCheck(){
        if(started) {
            SharedPreferences sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(this);
            boolean l = sharedPrefs.getBoolean(Constants.LAST_CHECK, true);
            if(mLastCheck!=null) {
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
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
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
    public void receivedBroadcast() {
        // Received a broadcast notification that the inventory of items has changed
        Log.d(TAG, "Received broadcast notification. Querying inventory.");
        try {
            mHelper.queryInventoryAsync(mGotInventoryListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            complain("Error querying inventory. Another async operation in progress.");
        }
    }

    @Override
    public void onClick(View view) {

    }
    void complain(String message) {
        Log.e(TAG, "**** ServerChecker Error: " + message);
        alert("Error: " + message);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUIWithLastCheck();
    }

    boolean mNotificationOk, mEmailOk;



    private void showHostsDialog(final Host host){
        //Preparing views
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.hosts, null);
        //layout_root should be the name of the "top-level" layout node in the dialog_layout.xml file.
        final EditText site = (EditText) layout.findViewById(R.id.site);
        //final EditText port = (EditText) layout.findViewById(R.id.port);
        final CheckBox notification = (CheckBox)layout.findViewById(R.id.notification);
        final CheckBox email = (CheckBox)layout.findViewById(R.id.email);
        final Button checkNow = (Button)layout.findViewById(R.id.checknow);
        final EditText emailsArea = (EditText) layout.findViewById(R.id.emails_area);
        final Button add = (Button) layout.findViewById(R.id.add_site);
        final ImageView checkResult = (ImageView)layout.findViewById(R.id.check_result);
        final Button delete = (Button) layout.findViewById(R.id.delete_site);
        final Button update = (Button) layout.findViewById(R.id.update_site);
        if(host != null){
            delete.setVisibility(View.VISIBLE);
            update.setVisibility(View.VISIBLE);
            add.setVisibility(View.GONE);
            site.setText(host.getHost());
            notification.setChecked(host.isNotification());
            email.setChecked(host.isEmails());
            if(host.isEmails()){
               emailsArea.setVisibility(View.VISIBLE);
            }else{
                emailsArea.setVisibility(View.GONE);
            }
            emailsArea.setText(Util.getStringFromList(host.getAllEmails()));
        }else{
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

                if((url!=null)&&(!url.isEmpty())){
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //mCheckOk = Util.isReachable(url, p, 30000);
                            mConnectionCheckResult = Util.isServerReachable(MainActivityPro.this, url);
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    switch(mConnectionCheckResult){
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
                if((url!=null)&&(!url.isEmpty())){
                    //save host and emails
                    Host host = new Host();
                    host.setHost(url);
                    if(mNotificationOk){
                        host.setNotification(true);
                    }else{
                        host.setNotification(false);
                    }
                    if(mEmailOk){
                        host.setEmails(true);
                        List<Email> emails = Util.getEmailsFromTextArea(emailsArea.getText().toString());
                        host.setAllEmails(emails);
                    }else{
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
                if((url!=null)&&(!url.isEmpty())){
                    DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                    //save host and emails
                    host.setHost(url);
                    if(notification.isChecked()){
                        host.setNotification(true);
                    }else{
                        host.setNotification(false);
                    }
                    if(email.isChecked()){
                        host.setEmails(true);
                        List<Email> emails = Util.getEmailsFromTextArea(emailsArea.getText().toString());
                        host.setAllEmails(emails);
                    }else{
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
                    if(mEmailOk){
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

    int mConnectionCheckResult=-1;




}
