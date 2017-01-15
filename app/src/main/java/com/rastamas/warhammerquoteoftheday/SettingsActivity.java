package com.rastamas.warhammerquoteoftheday;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.CompoundButton;
import android.widget.Switch;

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
