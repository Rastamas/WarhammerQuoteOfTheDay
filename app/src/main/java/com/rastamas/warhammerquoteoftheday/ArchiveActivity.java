package com.rastamas.warhammerquoteoftheday;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public class ArchiveActivity extends AppCompatActivity {

    private LinearLayout mArchiveLayout;
    private DBAdapter mDBAdapter;
    private SharedPreferences mPreferences;
    private Typeface custom_font;
    private DatePickerDialog.OnDateSetListener onDateSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);

        setupLookAndFeel();

        mPreferences = getSharedPreferences("WarhammerQuotePreferences", 0);

        loadQuotes();
        setupOnDateSetListener();
    }

    private void setupLookAndFeel() {
        Bitmap originalImage = BitmapFactory.decodeResource(getResources(), R.drawable.bloodraven_button);
        IntTuple dimensions = Helper.getScreenSize(getWindowManager());
        Bitmap scaledImage = Bitmap.createScaledBitmap(originalImage,
                (int)(1.0f * 400 / 1080 * dimensions.x),
                (int)(1.0f * 160 / 1920 * dimensions.y), true);
        Button mGetArchivesButton = (Button) findViewById(R.id.button_getarchives);
        mGetArchivesButton.setBackground(new BitmapDrawable(getResources(), scaledImage));

        mArchiveLayout = (LinearLayout) findViewById(R.id.archive_list_linear_layout);
        custom_font = Typeface.createFromAsset(getAssets(), "fonts/CaslonAntiqueBold.ttf");
        mGetArchivesButton.setTypeface(custom_font);
    }

    private void setupOnDateSetListener() {
        onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                if (!Helper.isServerAccessible(ArchiveActivity.this)) {
                    Toast.makeText(ArchiveActivity.this, "No network connection", Toast.LENGTH_SHORT).show();
                    return;
                }
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                clearArchives(null);
                fillArchivesSinceDate(calendar);
            }
        };
    }

    private void fillArchivesSinceDate(Calendar start) {
        Calendar end = Calendar.getInstance();
        for (Date date = end.getTime(); end.after(start); end.add(Calendar.DATE, -1), date = end.getTime()) {

            String key = Helper.createDateKey(date);
            new FillArchiveTask().execute(key);
        }
    }

    private void loadQuotes() {
        mDBAdapter = new DBAdapter(getApplicationContext());
        mDBAdapter.open();
        TreeMap archives = mDBAdapter.getAllQuotes();
        mDBAdapter.close();

        for (Object date :
                sortDateStrings(archives.keySet())) {
            String quote = archives.get(date).toString();
            createNewArchiveEntry(quote, date.toString(), false);
        }
    }

    @SuppressLint("SimpleDateFormat")
    private ArrayList<String> sortDateStrings(Set<String> setToSort) {
        List<Date> convertedSet = new ArrayList<>();
        for (String date :
                setToSort) {
            try {
                convertedSet.add(new SimpleDateFormat("yyyyMMMdd").parse(date));
            } catch (ParseException e) {
                //contact dev
            }
        }
        Collections.sort(convertedSet, new Comparator<Date>() {
            @Override
            public int compare(Date date1, Date date2) {
                return date1.compareTo(date2);
            }
        });
        ArrayList<String> stringSet = new ArrayList<>();
        for (Date date :
                convertedSet) {
            stringSet.add(Helper.createDateKey(date));
        }
        Collections.reverse(stringSet);
        return stringSet;
    }

    private void createNewArchiveEntry(String quote, String date, boolean fadeIn) {
        TextView recordTextView = new TextView(getApplicationContext());
        recordTextView.setText(quote);
        recordTextView.setTextColor(getResources().getColor(R.color.bloodRavenAccent));
        recordTextView.setTypeface(custom_font);
        recordTextView.setTextSize(28);

        TextView dateTextView = new TextView(getApplicationContext());
        dateTextView.setText(prettifyDate(date));
        dateTextView.setTextColor(getResources().getColor(R.color.bloodRavenPrimary));
        dateTextView.setTypeface(custom_font);
        dateTextView.setTextSize(18);

        recordTextView.setAlpha(fadeIn ? 0.0f : 1.0f);
        dateTextView.setAlpha(fadeIn ? 0.0f : 1.0f);
        mArchiveLayout.addView(dateTextView);
        mArchiveLayout.addView(recordTextView);

        if (fadeIn) {
            recordTextView.animate().alpha(1.0f).setDuration(1000);
            dateTextView.animate().alpha(1.0f).setDuration(1000);
        }
    }

    @SuppressLint("SimpleDateFormat")
    private String prettifyDate(String dateToParse) {
        Date date;
        try {
            date = new SimpleDateFormat("yyyyMMMdd").parse(dateToParse);
        } catch (ParseException e) {
            return dateToParse;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        if (mPreferences.getString("dateFormat", "").equals("imperial")) {
            return convertStandardToImperial(cal);
        }
        return cal.get(Calendar.YEAR) + " - " + (cal.get(Calendar.MONTH) + 1) + " - " + cal.get(Calendar.DAY_OF_MONTH);
    }

    private String convertStandardToImperial(Calendar cal) {
        String checkNumber = "0 ";
        int yearFraction = (int) ((float) cal.get(Calendar.DAY_OF_YEAR) / (365 + ((cal.get(Calendar.YEAR) % 4) == 0 ? 1 : 0)) * 1000);
        String year = " " + cal.get(Calendar.YEAR) % 1000 + ".M" + (1 + cal.get(Calendar.YEAR) / 1000);

        return checkNumber + fillWithZeroes(yearFraction) + year;
    }

    private String fillWithZeroes(int i) {
        String returnString = "" + i;
        if (i < 10) returnString = "0" + returnString;
        if (i < 100) returnString = "0" + returnString;
        return returnString;
    }

    public void clearArchives(View view) {

        mArchiveLayout.removeAllViews();
        mDBAdapter.open();
        mDBAdapter.clearQuotes();
        mDBAdapter.close();

    }

    public void getArchivesButtonOnClick(View view) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(ArchiveActivity.this, onDateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    private class FillArchiveTask extends GetQuoteTask {

        @Override
        public void onPostExecute(String response) {
            String quote;
            if (response.contains("%")) {
                Toast.makeText(ArchiveActivity.this, response.replace("%", ""), Toast.LENGTH_SHORT).show();
                return;
            }
            quote = response.split("#")[1];
            quote = quote.replaceAll("^\"|\"$", "");
            String dateKey = response.split("#")[0];
            createNewArchiveEntry(quote, dateKey, true);
            mDBAdapter.open();
            mDBAdapter.putQuote(dateKey, quote);
            mDBAdapter.close();
        }

    }
}
