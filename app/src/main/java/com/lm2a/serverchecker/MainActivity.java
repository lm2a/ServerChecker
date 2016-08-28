package com.lm2a.serverchecker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private RadioGroup mRadioGroup;
    private NumberPicker mNumberPicker;
    private Button mAdd;
    public static final long HOURS = 0;
    public static final long MINUTES = 1;
    public static final long SECONDS = 2;

    public static final long T_HOURS = 60*60*1000;
    public static final long T_MINUTES = 60*1000;
    public static final long T_SECONDS = 1000;

    private long timeInMiliseconds;
    private long timeUnit = -1;
    private long timeChoosed=-1;
    private boolean notification, email;
    private PendingIntent pendingIntent;
    private AlarmManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Retrieve a PendingIntent that will perform a broadcast
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        setUI();
    }


    private void setUI() {
        mRadioGroup = (RadioGroup) findViewById(R.id.radioButtonTimeUnit);
        mNumberPicker = (NumberPicker) findViewById(R.id.numberPickerTime);
        mNumberPicker.setMinValue(0);
        mNumberPicker.setMaxValue(10);
        mNumberPicker.setWrapSelectorWheel(false);

        mAdd = (Button) findViewById(R.id.add);

        mNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                // TODO Auto-generated method stub
                timeChoosed = newVal;
            }
        });

        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("TAG", "" + timeChoosed + " - " + timeUnit + " - " + notification + " - " + email);
                process();
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
                    timeUnit = HOURS;
                    timeInMiliseconds = timeChoosed * T_HOURS;

                } else if (checkedId == R.id.radioButtonMinute) {
                    timeUnit = MINUTES;
                    timeInMiliseconds = timeChoosed * T_MINUTES;
                } else {
                    timeUnit = SECONDS;
                    timeInMiliseconds = timeChoosed * T_SECONDS;
                }
            }

        });
    }



    private void process(){
        cancelAlarm();
        startAlarm();
    }

    public void startAlarm() {
        manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), timeInMiliseconds, pendingIntent);
        Toast.makeText(this, "Alarm Set: ", Toast.LENGTH_SHORT).show();
    }

    public void cancelAlarm() {
        if (manager != null) {
            manager.cancel(pendingIntent);
            Toast.makeText(this, "Alarm Canceled", Toast.LENGTH_SHORT).show();
        }

    }

}
