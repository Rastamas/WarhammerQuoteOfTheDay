package com.rastamas.warhammerquoteoftheday;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mPreferences = getSharedPreferences("WarhammerQuotePreferences", 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        toolbar.setTitle("Settings");
        setSupportActionBar(toolbar);
        setupDateFormatControl();
        setupAdControl();
        setupNotificationControl();
    }

    private void setupNotificationControl() {
        Switch notificationSwitch = (Switch) findViewById(R.id.switch_notifications);
        boolean notificationsEnabled = mPreferences.getBoolean("notifications", false);
        notificationSwitch.setChecked(notificationsEnabled);
        if(notificationsEnabled){
            updateTextView(mPreferences.getInt("notificationHour", 9),
                    mPreferences.getInt("notificationMinute", 0));
        }
        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mPreferences.edit().putBoolean("notifications", isChecked).apply();
                AlarmManager alarmManager = (AlarmManager)
                        SettingsActivity.this.getSystemService(SettingsActivity.this.ALARM_SERVICE);
                Intent intent = new Intent(SettingsActivity.this, AlarmReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(SettingsActivity.this,
                        0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                if(isChecked){
                    saveDesiredTimeToPreferences();
                    Calendar calendar = setCalendarForNotification();
                    if(shouldHaveAlreadyNotified(calendar))
                        calendar.add(Calendar.DATE, 1);
                    alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);
                }else{
                    alarmManager.cancel(pendingIntent);
                    updateTextView(-1, -1);
                    mPreferences.edit()
                            .remove("notificationHour")
                            .remove("notificationMinute")
                            .apply();
                }
            }
        });
    }

    private boolean shouldHaveAlreadyNotified(Calendar then) {
        Calendar now = Calendar.getInstance();
        return now.before(then);
    }

    private Calendar setCalendarForNotification() {
        int desiredHour = mPreferences.getInt("notificationHour", 9);
        int desiredMinute = mPreferences.getInt("notificationMinute", 0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, desiredHour);
        calendar.set(Calendar.MINUTE, desiredMinute);
        calendar.set(Calendar.SECOND, 0);
        return calendar;
    }

    @SuppressLint("SetTextI18n")
    private void updateTextView(int hour, int minute) {
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
                updateTextView(hour, minute);
            }
        }, currentHour, currentMinute, true);
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
                Toast.makeText(SettingsActivity.this,
                        "Restart application for the changes to take effect!",
                        Toast.LENGTH_LONG).show();
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

}
