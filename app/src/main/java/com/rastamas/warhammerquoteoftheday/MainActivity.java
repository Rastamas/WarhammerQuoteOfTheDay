package com.rastamas.warhammerquoteoftheday;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    private RelativeLayout mainLayout;
    private static int backgroundID = 0;
    private TextView mQuoteTextView;
    private Button mToggleButton;
    private Button mThemeButton;
    private Button mArchivesButton;
    private ImageButton mVisibilityButton;
    private ImageButton mSettingsButton;
    private String mQuote;
    private String dateKey;

    public SharedPreferences mPreferences;
    public DBAdapter mDBAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupViewReferences();
        processPreferences();
        changeTheme(R.style.BloodRaven);

        mVisibilityButton.setImageResource(R.drawable.ic_hide_button);
        mSettingsButton.setImageResource(R.drawable.ic_settings);
        setupFonts();

        mDBAdapter = new DBAdapter(getApplicationContext());
        dateKey = createTodaysDateKey();

        new GetMainQuoteTask().execute(dateKey);
    }

    private String createTodaysDateKey() {
        Date today = new Date();
        String[] dateStringParts = today.toString().split(" ");
        //example: "Sun Dec 18 04:54:14 GMT+01:00 2016"
        return dateStringParts[5] + dateStringParts[1] + dateStringParts[2];
    }

    private void processPreferences() {
        mPreferences = getSharedPreferences("WarhammerQuotePreferences", 0);

        String visibilitySettings = mPreferences.getString("visibility", "");
        if(visibilitySettings.equals("hidden")){
            mArchivesButton.setAlpha(0.0f);
            mThemeButton.setAlpha(0.0f);
            mVisibilityButton.setImageResource(R.drawable.ic_show_button);
        } else {
            mArchivesButton.setAlpha(1.0f);
            mThemeButton.setAlpha(1.0f);
            mVisibilityButton.setImageResource(R.drawable.ic_hide_button);
        }
    }


    private void setupViewReferences() {
        mainLayout = (RelativeLayout) findViewById(R.id.activity_main);
        mQuoteTextView = (TextView) findViewById(R.id.textview_quote);
        mThemeButton = (Button) findViewById(R.id.button_change_theme);
        mToggleButton = (Button) findViewById(R.id.button_toggle_quote);
        mArchivesButton = (Button) findViewById(R.id.button_archives);
        mVisibilityButton = (ImageButton) findViewById(R.id.button_visibility);
        mSettingsButton = (ImageButton) findViewById(R.id.button_settings);
    }

    private void setupFonts() {
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/CaslonAntiqueBold.ttf");
        mArchivesButton.setTypeface(custom_font);
        mQuoteTextView.setTypeface(custom_font);
        mToggleButton.setTypeface(custom_font);
        mThemeButton.setTypeface(custom_font);

    }

    public void toggleVisibility(View view){
        if(mPreferences.getString("visibility", "").equals(""))
            mPreferences.edit().putString("visibility", "visible").apply();
        if(mArchivesButton.getAlpha() > 0.0f){
            mArchivesButton.animate().alpha(0.0f).setDuration(1000);
            mThemeButton.animate().alpha(0.0f).setDuration(1000);
            mVisibilityButton.setImageResource(R.drawable.ic_show_button);
            mPreferences.edit().remove("visibility").putString("visibility", "hidden").apply();
        } else {
            mArchivesButton.animate().alpha(1.0f).setDuration(1000);
            mThemeButton.animate().alpha(1.0f).setDuration(1000);
            mVisibilityButton.setImageResource(R.drawable.ic_hide_button);
            mPreferences.edit().remove("visibility").putString("visibility", "visible").apply();
        }
    }

    public void toggleQuote(View view) {
        if (mQuote != null) {
            Date yesterday = new Date(new Date().getTime() - 24 * 3600 * 1000);
            String[] dateStringParts = yesterday.toString().split(" ");
            //example: "Sun Dec 18 04:54:14 GMT+01:00 2016"
            String yesterdayskey = dateStringParts[5] + dateStringParts[1] + dateStringParts[2];
            mDBAdapter.open();
            String prevQuote = mDBAdapter.getQuote(yesterdayskey);
            mDBAdapter.close();
            if(!mQuote.equals(prevQuote)){
                archiveQuote();
            }
        } else {
            new GetQuoteTask().execute();
            //contactDeveloper();
        }
        if (mQuoteTextView.getAlpha() == 0.0f) {
            mQuoteTextView.animate().alpha(1.0f).setDuration(1000);
        } else {
            mQuoteTextView.animate().alpha(0.0f).setDuration(1000);
        }
    }

    private void contactDeveloper() {
        //TODO
    }

    private void archiveQuote() {
        mDBAdapter.open();
        mDBAdapter.putQuote(dateKey, mQuote);
        mDBAdapter.close();
    }

    private void changeTheme(int themeID) {
        setTheme(themeID);
        if (themeID == R.style.BloodRaven) {
            mainLayout.setBackground(getDrawable(R.drawable.bloodraven_background1));
            Bitmap originalImage = BitmapFactory.decodeResource(getResources(), R.drawable.bloodraven_button);
            Bitmap scaledImage = Bitmap.createScaledBitmap(originalImage, 600, 200, true);
            Bitmap topImage = Bitmap.createScaledBitmap(originalImage, 530, 200, true);
            mToggleButton.setBackground(new BitmapDrawable(getResources(), scaledImage));
            mArchivesButton.setBackground(new BitmapDrawable(getResources(), topImage));
            mThemeButton.setBackground(new BitmapDrawable(getResources(), topImage));
            mQuoteTextView.setTextColor(getResources().getColor(R.color.bloodRavenAccent));
        }

    }

    public void archivesButtonOnClick(View view) {
        Intent intent = new Intent(this, ArchiveActivity.class);
        startActivity(intent);
    }

    public void backgroundButtonOnClick(View view) {
        switch(backgroundID) {
            case 0 :
                mainLayout.setBackground(getDrawable(R.drawable.death_watch));
                backgroundID++;
                break;
            case 1 :
                mainLayout.setBackground(getDrawable(R.drawable.bloodraven_background1));
                backgroundID = 0;
                break;
    }}

    public void buttonSettingsOnClick(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private class GetMainQuoteTask extends GetQuoteTask {

        @Override
        public void onPostExecute(String response) {

            if (response == null) {
                mQuote = null;
                return;
            }
            Log.i("INFO", response);
            mQuote = response.split("#")[1].replaceAll("^\"|\"$", "");
            mQuoteTextView.setText(mQuote);

        }

    }
}
