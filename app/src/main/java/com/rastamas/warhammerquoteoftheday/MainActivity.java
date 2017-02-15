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
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;


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
    private String mQuoteId;
    private String dateKey;

    public SharedPreferences mPreferences;
    public DBAdapter mDBAdapter;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreferences = getSharedPreferences("WarhammerQuotePreferences", 0);
        setupReferences();
        initTodaysDateKeyAndQuoteId();
        getQuoteAndSetupTextView();

        processPreferences();
        setupLookAndFeel();
        setupFonts();
    }

    @Override
    public void onResume(){
        super.onResume();
        if(quoteIsOutdated()){
            initTodaysDateKeyAndQuoteId();
            getQuoteAndSetupTextView();
        }
    }

    private boolean quoteIsOutdated(){
        String yesterdaysKey = Helper.createDateKey(new Date(new Date().getTime() - 24 * 3600 * 1000));
        mDBAdapter.open();
        String prevQuote = mDBAdapter.getQuote(yesterdaysKey);
        mDBAdapter.close();
        return mQuote.equals(prevQuote);
    }

    private void getQuoteAndSetupTextView() {
        mDBAdapter.open();
        if (todaysQuoteIsAvailable()) {
            mQuote = mDBAdapter.getQuote(dateKey);
            mQuoteTextView.setText(mQuote);
        } else {
            makeSureQuoteIsHidden();
            new GetMainQuoteTask().execute(mQuoteId, dateKey);
        }
        mDBAdapter.close();
    }

    private void makeSureQuoteIsHidden() {
        mQuoteTextView.setAlpha(0.0f);
        mPreferences.edit().putBoolean("quoteVisible", false).apply();
    }

    private boolean todaysQuoteIsAvailable() {
        return mDBAdapter.quoteExists(dateKey);
    }

    private void processPreferences() {
        processVisibilityPreferences();
        processAdPreferences();
        processBackGroundPreferences();
        processQuoteTextSizePreferences();
        processQuoteVisibility();
    }

    private void processQuoteVisibility() {
        boolean quoteIsVisible = mPreferences.getBoolean("quoteVisible", false);
        mQuoteTextView.setAlpha(quoteIsVisible ? 1.0f : 0.0f);
    }

    private void processQuoteTextSizePreferences() {
        if (mQuote == null) return;
        int quoteTextSize = (int) Math.floor(Math.sqrt(20f / mQuote.length()) * 50); //mPreferences.getInt("quoteTextSize", 32);
        mQuoteTextView.setTextSize(quoteTextSize);
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

            MobileAds.initialize(getApplicationContext(), "ca-app-pub-6916535752895902~2200057071");
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
            processQuoteTextSizePreferences();
            loadAndArchiveTodaysQuote();
        } else {
            if (Helper.isServerAccessible(MainActivity.this)) {
                new GetMainQuoteTask().execute(mQuoteId, dateKey);
            } else {
                Toast.makeText(MainActivity.this, "No network connection", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        animateQuoteVisibility();
    }

    private void animateQuoteVisibility() {
        if (mQuoteTextView.getAlpha() == 0.0f) {
            mQuoteTextView.animate().alpha(1.0f).setDuration(1000);
            mPreferences.edit().putBoolean("quoteVisible", true).apply();
        } else {
            mQuoteTextView.animate().alpha(0.0f).setDuration(1000);
            mPreferences.edit().putBoolean("quoteVisible", false).apply();
        }
    }

    private void loadAndArchiveTodaysQuote() {

        if (!quoteIsOutdated()) {
            archiveQuote();
        }
    }

    private void archiveQuote() {
        try {
            mDBAdapter.open();
            mDBAdapter.putQuote(dateKey, mQuoteId, mQuote);
            mDBAdapter.close();
        } catch (Exception e) {
            Log.d("Error", e.getMessage());
        }
    }

    private void setupLookAndFeel() {
        setTheme(R.style.BloodRaven);
        setupButtonImageBackgrounds();
        mSettingsButton.setImageResource(R.drawable.ic_settings);
        mQuoteTextView.setTextColor(getResources().getColor(R.color.bloodRavenAccent));
    }

    private void setupButtonImageBackgrounds() {
        IntTuple dimensions = Helper.getScreenSize(getWindowManager());

        Bitmap originalImage = BitmapFactory.decodeResource(getResources(), R.drawable.bloodraven_button);
        Bitmap scaledImage = Bitmap.createScaledBitmap(originalImage,
                (int) (1.0f * 500 / 1080 * dimensions.x),
                (int) (1.0f * 180 / 1920 * dimensions.y), true);
        Bitmap topImage = Bitmap.createScaledBitmap(originalImage,
                (int) (1.0f * 400 / 1080 * dimensions.x),
                (int) (1.0f * 160 / 1920 * dimensions.y), true);
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
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Intent refresh = new Intent(this, MainActivity.class);
            startActivity(refresh);
            this.finish();
        }
    }

    private void initTodaysDateKeyAndQuoteId() {
        new GetQuoteNumberTask().execute();
        dateKey = Helper.createDateKey(new Date());

        mDBAdapter.open();
        if (mDBAdapter.quoteExists(dateKey)) {
            mQuoteId = mDBAdapter.getQuoteId(dateKey);
        } else {
            Random random = new Random();
            ArrayList<String> previousIds = mDBAdapter.getLastThirtyQuoteIds();
            do {
                mQuoteId = "" + random.nextInt( mPreferences.getInt("numberOfQuotes", 99));
            } while (previousIds.contains(mQuoteId));
        }
        mDBAdapter.close();
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
            mQuote = response.split("#")[2].replaceAll("^\"|\"$", "");
            mQuoteTextView.setText(mQuote);
        }
    }

    private class GetQuoteNumberTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                URL apiUrl = new URL("http://52.208.157.181:1994/api/Emperor/quoteNumber");
                HttpURLConnection urlConnection = (HttpURLConnection) apiUrl.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String numberOfQuotes = bufferedReader.readLine();
                    bufferedReader.close();
                    urlConnection.disconnect();
                    return Integer.parseInt(numberOfQuotes);

                } catch (Exception e) {
                    return 99;
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("Error with api request!", e.getMessage(), e);
                return null;
            }
        }

        @Override
        public void onPostExecute(Integer response) {
            mPreferences.edit().putInt("numberOfQuotes", response).apply();
        }
    }
}

