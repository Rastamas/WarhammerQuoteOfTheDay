package com.rastamas.warhammerquoteoftheday;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences mPreferences;
    private PendingIntent alarmPendingIntent;
    private AlarmManager mAlarmManager;

    private Switch mNotificationSwitch;
    private Button mContactButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mPreferences = getSharedPreferences("WarhammerQuotePreferences", 0);
        mNotificationSwitch = (Switch) findViewById(R.id.switch_notifications);
        mContactButton = (Button) findViewById(R.id.button_contact);

        setupControls();
    }

    private void setupControls() {
        setupToolBar();
        setupDateFormatControl();
        setupAdControl();
        setupNotificationControl();
        setupButtonImageBackgrounds();
    }

    private void setupButtonImageBackgrounds() {
        IntTuple dimensions = Helper.getScreenSize(getWindowManager());

        Bitmap originalImage = BitmapFactory.decodeResource(getResources(), R.drawable.light_button);
        Bitmap scaledImage = Bitmap.createScaledBitmap(originalImage,
                (int) (500f / 1080 * dimensions.x),
                (int) (200f / 1920 * dimensions.y), true);
        mContactButton.setBackground(new BitmapDrawable(getResources(), scaledImage));
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
                mAlarmManager = (AlarmManager)
                        SettingsActivity.this.getSystemService(ALARM_SERVICE);
                alarmPendingIntent = createPendingIntentForNotification();

                if(isChecked){
                    setNotificationAtDesiredTime();
                }else{
                    cancelNotification();
                }
            mPreferences.edit().putBoolean("notifications", isChecked).apply();
            }
        });

    }

    private void cancelNotification() {
        mAlarmManager.cancel(alarmPendingIntent);
        updateNotificationTextView(-1, -1);
        mPreferences.edit()
                .remove("notificationHour")
                .remove("notificationMinute")
                .apply();
    }

    private PendingIntent createPendingIntentForNotification() {
        Intent intent = new Intent(SettingsActivity.this, AlarmReceiver.class);
        return PendingIntent.getBroadcast(SettingsActivity.this,
                0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private boolean shouldHaveAlreadyNotified(Calendar then) {
        Calendar now = Calendar.getInstance();
        return now.after(then);
    }

    private Calendar getTimeForNotification() {
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

    private void setNotificationAtDesiredTime() {
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
                createNotification();
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

    private void createNotification() {
        Calendar calendar = getTimeForNotification();
        if(shouldHaveAlreadyNotified(calendar))
            calendar.add(Calendar.DATE, 1);
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                24 * 60 * 60 * 1000, alarmPendingIntent);
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

    public void contactButtonOnClick(View view) {
        Intent intent = new Intent(this, ContactActivity.class);
        startActivity(intent);
    }
}
