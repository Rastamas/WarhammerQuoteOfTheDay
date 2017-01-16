package com.rastamas.warhammerquoteoftheday;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupReferences();
        dateKey = createDateKey(new Date());
        new GetMainQuoteTask().execute(dateKey);

        processPreferences();
        changeTheme(R.style.BloodRaven);
        setupFonts();
        mSettingsButton.setImageResource(R.drawable.ic_settings);
    }

    public static String createDateKey(Date date) {
        String[] dateStringParts = date.toString().split(" ");
        //example: "Sun Dec 18 04:54:14 GMT+01:00 2016"
        return dateStringParts[5] + dateStringParts[1] + dateStringParts[2];
    }

    private void processPreferences() {
        mPreferences = getSharedPreferences("WarhammerQuotePreferences", 0);


        if (!mPreferences.getBoolean("showTopButtons", true)) {
            mArchivesButton.setAlpha(0.0f);
            mThemeButton.setAlpha(0.0f);
            mVisibilityButton.setImageResource(R.drawable.ic_show_button);
        } else {
            mArchivesButton.setAlpha(1.0f);
            mThemeButton.setAlpha(1.0f);
            mVisibilityButton.setImageResource(R.drawable.ic_hide_button);
        }

        boolean showAds = mPreferences.getBoolean("showAds", true);
        mAdView.setVisibility(showAds ? View.VISIBLE : View.INVISIBLE);

        RelativeLayout mainBottom = (RelativeLayout) findViewById(R.id.main_bottom_buttons);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mainBottom.getLayoutParams();
        if(showAds){
            params.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.addRule(RelativeLayout.ABOVE, R.id.adView);

            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
        else{
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
            String yesterdaysKey = createDateKey(new Date(new Date().getTime() - 24 * 3600 * 1000));
            mDBAdapter.open();
            String prevQuote = mDBAdapter.getQuote(yesterdaysKey);
            mDBAdapter.close();
            if (!mQuote.equals(prevQuote)) {
                archiveQuote();
            }
        } else {
            new GetMainQuoteTask().execute(dateKey);
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
        switch (backgroundID) {
            case 0:
                mainLayout.setBackground(getDrawable(R.drawable.death_watch));
                backgroundID++;
                break;
            case 1:
                mainLayout.setBackground(getDrawable(R.drawable.background3));
                backgroundID++;
                break;
            case 2:
                mainLayout.setBackground(getDrawable(R.drawable.bloodraven_background1));
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
