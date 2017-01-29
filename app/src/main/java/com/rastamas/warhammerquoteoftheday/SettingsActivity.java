package com.rastamas.warhammerquoteoftheday;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences mPreferences;

    private Switch mNotificationSwitch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mPreferences = getSharedPreferences("WarhammerQuotePreferences", 0);
        mNotificationSwitch = (Switch) findViewById(R.id.switch_notifications);

        setupControls();
    }

    private void setupControls() {
        setupToolBar();
        setupDateFormatControl();
        setupAdControl();
        setupNotificationControl();
    }

    private void setupToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        toolbar.setTitle("Settings");
        setSupportActionBar(toolbar);
    }

    private void setupNotificationControl() {

        boolean notificationsEnabled = mPreferences.getBoolean("notifications", false);
        mNotificationSwitch.setChecked(notificationsEnabled);
        if(notificationsEnabled){
            updateNotificationTextView(mPreferences.getInt("notificationHour", 9),
                    mPreferences.getInt("notificationMinute", 0));
        }
        mNotificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                AlarmManager alarmManager = (AlarmManager)
                        SettingsActivity.this.getSystemService(ALARM_SERVICE);
                PendingIntent pendingIntent = createPendingIntentForNotification();

                if(isChecked){
                    saveDesiredTimeToPreferences();
                    Calendar calendar = setTimeForNotification();
                    if(shouldHaveAlreadyNotified(calendar))
                        calendar.add(Calendar.DATE, 1);
                    alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);
                }else{
                    alarmManager.cancel(pendingIntent);
                    updateNotificationTextView(-1, -1);
                    mPreferences.edit()
                            .remove("notificationHour")
                            .remove("notificationMinute")
                            .apply();
                }
            mPreferences.edit().putBoolean("notifications", !isChecked).apply();
            }
        });

    }

    private PendingIntent createPendingIntentForNotification() {
        Intent intent = new Intent(SettingsActivity.this, AlarmReceiver.class);
        return PendingIntent.getBroadcast(SettingsActivity.this,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private boolean shouldHaveAlreadyNotified(Calendar then) {
        Calendar now = Calendar.getInstance();
        return now.after(then);
    }

    private Calendar setTimeForNotification() {
        int desiredHour = mPreferences.getInt("notificationHour", 9);
        int desiredMinute = mPreferences.getInt("notificationMinute", 0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, desiredHour);
        calendar.set(Calendar.MINUTE, desiredMinute);
        calendar.set(Calendar.SECOND, 0);
        return calendar;
    }

    @SuppressLint("SetTextI18n")
    private void updateNotificationTextView(int hour, int minute) {
        TextView desiredTimeTextView = (TextView) findViewById(R.id.textview_when);
        if(hour < 0 || minute < 0){
            desiredTimeTextView.setText("At this time: ");
            return;
        }
        String prettyHour = (hour < 10 ? "0" : "") + hour;
        String prettyMinute = (minute < 10 ? "0" : "") + minute;
        desiredTimeTextView.setText("At this time: " + prettyHour + ":" + prettyMinute);

    }

    private void saveDesiredTimeToPreferences() {
        TimePickerDialog mDialog;
        int currentHour = mPreferences.getInt("notificationHour", 9);
        int currentMinute = mPreferences.getInt("notificationMinute", 0);
        mDialog = new TimePickerDialog(SettingsActivity.this,
                new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                mPreferences.edit()
                        .putInt("notificationHour", hour)
                        .putInt("notificationMinute", minute)
                        .apply();
                updateNotificationTextView(hour, minute);
            }
        }, currentHour, currentMinute, true);
        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                mNotificationSwitch.setChecked(false);
            }
        });
        mDialog.setTitle("Set time of notification");
        mDialog.show();
    }



    private void setupAdControl() {
        Switch adSwitch = (Switch) findViewById(R.id.switch_ads);
        adSwitch.setChecked(mPreferences.getBoolean("showAds", true));
        adSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mPreferences.edit().putBoolean("showAds", isChecked).apply();
            }
        });
    }

    private void setupDateFormatControl() {
        Switch dateFormatSwitch = (Switch) findViewById(R.id.switch_dateformat);
        dateFormatSwitch.setChecked(mPreferences.getString("dateFormat", "").equals("imperial"));
        dateFormatSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                String dateFormat = isChecked ? "imperial" : "standard";
                if(mPreferences.getString("dateFormat", "").equals(""))
                    mPreferences.edit().putString("dateFormat", dateFormat).apply();
                else
                    mPreferences.edit().remove("dateFormat").putString("dateFormat", dateFormat).apply();

            }
        });
    }

    @Override
    public void onBackPressed(){
        setResult(RESULT_OK);
        finish();
    }
}
