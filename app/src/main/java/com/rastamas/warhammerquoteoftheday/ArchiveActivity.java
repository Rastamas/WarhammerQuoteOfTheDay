package com.rastamas.warhammerquoteoftheday;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArchiveActivity extends AppCompatActivity {

    private LinearLayout mArchiveLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);
        mArchiveLayout = (LinearLayout) findViewById(R.id.archive_list_liner_layout);
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/CaslonAntiqueBold.ttf");
        Button clearArchivesButton = (Button) findViewById(R.id.button_clear_archives);
        clearArchivesButton.setTypeface(custom_font);

        TreeMap archives = new TreeMap(MainActivity.mArchives.getAll());
        for (Object entry :
                archives.descendingKeySet()) {
            String value = archives.get(entry.toString()).toString();
            TextView recordTextView = new TextView(getApplicationContext());
            recordTextView.setText(value);
            recordTextView.setTextColor(getResources().getColor(R.color.emperorAccent));
            recordTextView.setTypeface(custom_font);
            recordTextView.setTextSize(28);

            TextView dateTextView = new TextView(getApplicationContext());
            dateTextView.setText(prettifyDate(entry.toString()));
            dateTextView.setTextColor(getResources().getColor(R.color.emperorPrimary));
            dateTextView.setTypeface(custom_font);
            dateTextView.setTextSize(18);

            mArchiveLayout.addView(dateTextView);
            mArchiveLayout.addView(recordTextView);
        }
    }

    private String prettifyDate(String dateToParse){
        Date date;
        try {
            date = new SimpleDateFormat("yyyyMMMdd").parse(dateToParse);
        } catch (ParseException e) {
            return dateToParse;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        return cal.get(Calendar.YEAR) + " - " + cal.get(Calendar.MONTH) + " - " + cal.get(Calendar.DAY_OF_MONTH);
    }

    public void clearArchives(View view) {
        MainActivity.mArchives.edit().clear().apply();
        mArchiveLayout.removeAllViews();

    }
}
