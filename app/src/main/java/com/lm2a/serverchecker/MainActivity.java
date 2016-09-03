package com.lm2a.serverchecker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.lm2a.serverchecker.model.Config;
import com.lm2a.serverchecker.services.BackgroundService;
import com.lm2a.serverchecker.services.Constants;

public class MainActivity extends AppCompatActivity {

    private RadioGroup mRadioGroup;
    private NumberPicker mNumberPicker;
    private Button mStart;


    private long timeInMiliseconds;
    private int timeUnit = -1;
    private int timeChoosed=-1;
    private boolean notification, email;
    private PendingIntent pendingIntent;
    private AlarmManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Config c = getParametersFromPreferences();
        setUI(c);
    }


    private void setUI(final Config config) {
        mRadioGroup = (RadioGroup) findViewById(R.id.radioButtonTimeUnit);
        mNumberPicker = (NumberPicker) findViewById(R.id.numberPickerTime);
        mNumberPicker.setMinValue(0);
        mNumberPicker.setMaxValue(10);
        mNumberPicker.setWrapSelectorWheel(false);

        final EditText editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        final EditText site = (EditText) findViewById(R.id.url);

        mStart = (Button) findViewById(R.id.start);

        mNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                // TODO Auto-generated method stub
                timeChoosed = newVal;
            }
        });

        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String e = editTextEmail.getText().toString();
                String u = site.getText().toString();

                if(timeChoosed<0){
                    timeChoosed=config.interval;
                    timeUnit=config.timeUnit;
                }
                Log.i("TAG", "" + timeChoosed + " - " + timeUnit + " - " + notification + " - " + email);
                setParametersOnPreferences(timeChoosed, timeUnit, u, e);
                //process();

                Intent startServiceIntent = new Intent(MainActivity.this, BackgroundService.class);
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
        CheckBox chkEmail = (CheckBox) findViewById(R.id.checkBoxEmail);

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

        chkEmail.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //is chkIos checked?
                if (((CheckBox) v).isChecked()) {
                    email = true;
                } else {
                    email = false;
                }

            }
        });


        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if (checkedId == R.id.radioButtonHour) {
                    timeUnit = Constants.HOURS;
                    timeInMiliseconds = timeChoosed * Constants.T_HOURS;

                } else if (checkedId == R.id.radioButtonMinute) {
                    timeUnit = Constants.MINUTES;
                    timeInMiliseconds = timeChoosed * Constants.T_MINUTES;
                } else {
                    timeUnit = Constants.SECONDS;
                    timeInMiliseconds = timeChoosed * Constants.T_SECONDS;
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
            editTextEmail.setText(config.email);
            site.setText(config.url);
        }

    }




    private void setParametersOnPreferences(int interval, int timeUnit, String url, String email){
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        sharedPrefs.edit().putInt(Constants.INTERVAL, interval).apply();
        sharedPrefs.edit().putInt(Constants.TIME_UNIT, timeUnit).apply();
        sharedPrefs.edit().putString(Constants.SITE_URL, url).apply();
        sharedPrefs.edit().putString(Constants.EMAIL, email).apply();
    }

    private Config getParametersFromPreferences(){
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        int i = sharedPrefs.getInt(Constants.INTERVAL, 1000);//1 seg default
        int t = sharedPrefs.getInt(Constants.TIME_UNIT, 0);//hour default
        String u = sharedPrefs.getString(Constants.SITE_URL, null);
        String e = sharedPrefs.getString(Constants.EMAIL, null);
        if(u!=null){
            return new Config(i, t, u, e);
        }else{
            return null;
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
                Intent intent = new Intent(MainActivity.this, BackgroundService.class);
                intent.putExtra("Messenger", Constants.STOP_SERVICE);
                startService(intent);
                break;
        }
        return true;
    }


}
