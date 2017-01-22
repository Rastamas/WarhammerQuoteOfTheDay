package com.rastamas.warhammerquoteoftheday;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;

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
        notificationSwitch.setChecked(mPreferences.getBoolean("notifications", false));
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
                    Calendar calendar = Calendar.getInstance();
                    int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                    calendar.set(Calendar.HOUR_OF_DAY, 9);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    if(currentHour > 9) calendar.add(Calendar.DATE, 1);
                    alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);
                }else{
                    alarmManager.cancel(pendingIntent);
                }
            }
        });
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

}
