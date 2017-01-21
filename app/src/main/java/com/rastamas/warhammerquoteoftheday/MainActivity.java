package com.rastamas.warhammerquoteoftheday;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.Date;


public class MainActivity extends AppCompatActivity {

    private RelativeLayout mainLayout;
    private static int backgroundID;
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

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupReferences();
        dateKey = Helper.createDateKey(new Date());
        new GetMainQuoteTask().execute(dateKey);

        processPreferences();
        changeTheme(R.style.BloodRaven);
        setupFonts();
        mSettingsButton.setImageResource(R.drawable.ic_settings);
    }

    private void processPreferences() {
        mPreferences = getSharedPreferences("WarhammerQuotePreferences", 0);

        processVisibilityPreferences();
        processAdPreferences();
        processBackGroundPreferences();
    }

    private void processBackGroundPreferences() {
        backgroundID = mPreferences.getInt("backgroundID", 0);
        if (backgroundID == 0) {
            mPreferences.edit().putInt("backgroundID", 0).apply();
        }
        backgroundButtonOnClick(null);
    }

    private void processVisibilityPreferences() {
        if (!mPreferences.getBoolean("showTopButtons", true)) {
            mArchivesButton.setAlpha(0.0f);
            mThemeButton.setAlpha(0.0f);
            mVisibilityButton.setImageResource(R.drawable.ic_show_button);
        } else {
            mArchivesButton.setAlpha(1.0f);
            mThemeButton.setAlpha(1.0f);
            mVisibilityButton.setImageResource(R.drawable.ic_hide_button);
        }
    }

    private void processAdPreferences() {
        boolean showAds = mPreferences.getBoolean("showAds", true);
        mAdView.setVisibility(showAds ? View.VISIBLE : View.INVISIBLE);

        RelativeLayout mainBottom = (RelativeLayout) findViewById(R.id.main_bottom_buttons);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mainBottom.getLayoutParams();
        if (showAds) {
            params.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.addRule(RelativeLayout.ABOVE, R.id.adView);

            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        } else {
            params.removeRule(RelativeLayout.ABOVE);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }
        mainBottom.setLayoutParams(params);
    }

    private void setupReferences() {
        mainLayout = (RelativeLayout) findViewById(R.id.activity_main);
        mQuoteTextView = (TextView) findViewById(R.id.textview_quote);
        mThemeButton = (Button) findViewById(R.id.button_change_theme);
        mToggleButton = (Button) findViewById(R.id.button_toggle_quote);
        mArchivesButton = (Button) findViewById(R.id.button_archives);
        mVisibilityButton = (ImageButton) findViewById(R.id.button_visibility);
        mSettingsButton = (ImageButton) findViewById(R.id.button_settings);
        mDBAdapter = new DBAdapter(getApplicationContext());
        mAdView = (AdView) findViewById(R.id.adView);
    }

    private void setupFonts() {
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/CaslonAntiqueBold.ttf");
        mArchivesButton.setTypeface(custom_font);
        mQuoteTextView.setTypeface(custom_font);
        mToggleButton.setTypeface(custom_font);
        mThemeButton.setTypeface(custom_font);

    }

    public void toggleVisibility(View view) {
        if (mArchivesButton.getAlpha() > 0.0f) {
            mArchivesButton.animate().alpha(0.0f).setDuration(1000);
            mThemeButton.animate().alpha(0.0f).setDuration(1000);
            mVisibilityButton.setImageResource(R.drawable.ic_show_button);
            mPreferences.edit().putBoolean("showTopButtons", false).apply();
        } else {
            mArchivesButton.animate().alpha(1.0f).setDuration(1000);
            mThemeButton.animate().alpha(1.0f).setDuration(1000);
            mVisibilityButton.setImageResource(R.drawable.ic_hide_button);
            mPreferences.edit().putBoolean("showTopButtons", true).apply();
        }
    }

    public void toggleQuote(View view) {
        if (mQuote != null) {
            String yesterdaysKey = Helper.createDateKey(new Date(new Date().getTime() - 24 * 3600 * 1000));
            mDBAdapter.open();
            String prevQuote = mDBAdapter.getQuote(yesterdaysKey);
            mDBAdapter.close();
            if (!mQuote.equals(prevQuote)) {
                archiveQuote();
            }
        } else {
            if (Helper.isServerAccessible(MainActivity.this)) {
                new GetMainQuoteTask().execute(dateKey);
            }
            else{
                Toast.makeText(MainActivity.this, "No network connection", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        if (mQuoteTextView.getAlpha() == 0.0f) {
            mQuoteTextView.animate().alpha(1.0f).setDuration(1000);
        } else {
            mQuoteTextView.animate().alpha(0.0f).setDuration(1000);
        }
    }

    private void archiveQuote() {
        mDBAdapter.open();
        mDBAdapter.putQuote(dateKey, mQuote);
        mDBAdapter.close();
    }

    private void changeTheme(int themeID) {
        setTheme(themeID);
        if (themeID == R.style.BloodRaven) {
            setupButtonImageBackgrounds();

            mQuoteTextView.setTextColor(getResources().getColor(R.color.bloodRavenAccent));
        }

    }

    private void setupButtonImageBackgrounds() {
        IntTuple dimensions = Helper.getScreenSize(getWindowManager());

        Bitmap originalImage = BitmapFactory.decodeResource(getResources(), R.drawable.bloodraven_button);
        Bitmap scaledImage = Bitmap.createScaledBitmap(originalImage,
                (int)(1.0f * 500 / 1080 * dimensions.x),
                (int)(1.0f * 180 / 1920 * dimensions.y), true);
        Bitmap topImage = Bitmap.createScaledBitmap(originalImage,
                (int)(1.0f * 400 / 1080 * dimensions.x),
                (int)(1.0f * 160 / 1920 * dimensions.y), true);
        //Bitmap topImage = Bitmap.createScaledBitmap(originalImage, 530, 200, true);
        mToggleButton.setBackground(new BitmapDrawable(getResources(), scaledImage));
        mArchivesButton.setBackground(new BitmapDrawable(getResources(), topImage));
        mThemeButton.setBackground(new BitmapDrawable(getResources(), topImage));
    }

    public void archivesButtonOnClick(View view) {
        Intent intent = new Intent(this, ArchiveActivity.class);
        startActivity(intent);
    }

    public void backgroundButtonOnClick(View view) {
        mPreferences.edit().putInt("backgroundID", backgroundID).apply();
        switch (backgroundID) {
            case 0:
                mainLayout.setBackground(getDrawable(R.drawable.bloodraven_background1));
                backgroundID++;
                break;
            case 1:
                mainLayout.setBackground(getDrawable(R.drawable.death_watch));
                backgroundID++;
                break;
            case 2:
                mainLayout.setBackground(getDrawable(R.drawable.background3));
                backgroundID = 0;
                break;
        }
    }

    public void buttonSettingsOnClick(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private class GetMainQuoteTask extends GetQuoteTask {

        @Override
        public void onPostExecute(String response) {

            if (response.contains("%")) {
                mQuote = null;
                Toast.makeText(MainActivity.this, response.replace("%", ""), Toast.LENGTH_SHORT).show();
                return;
            }
            Log.i("INFO", response);
            mQuote = response.split("#")[1].replaceAll("^\"|\"$", "");
            mQuoteTextView.setText(mQuote);
        }
    }

}
