package com.rastamas.warhammerquoteoftheday;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.security.PublicKey;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    public static SharedPreferences mArchives;
    private TextView mQuoteTextView;
    private Button mToggleButton;
    private Button mArchivesButton;
    private Typeface custom_font;
    private String mQuote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mArchives = getPreferences(0);

        setupViewReferences();
        setupFonts();

        new GetQuoteTask().execute();
    }


    private void setupViewReferences(){
        mQuoteTextView = (TextView) findViewById(R.id.textview_quote);
        mToggleButton = (Button) findViewById(R.id.button_toggle_quote);
        mArchivesButton = (Button) findViewById(R.id.button_archives);
    }

    private void setupFonts(){
        custom_font = Typeface.createFromAsset(getAssets(), "fonts/CaslonAntiqueBold.ttf");
        mArchivesButton.setTypeface(custom_font);
        mQuoteTextView.setTypeface(custom_font);
        mToggleButton.setTypeface(custom_font);
    }

    public void toggleQuote(View view) {
        if(mQuote != null){
            arvhiveQuote();
        }
        else {
            contactDeveloper();
        }
        if(mQuoteTextView.getAlpha() == 0.0f){
            mQuoteTextView.animate().alpha(1.0f).setDuration(1500);
        }
        else{
            mQuoteTextView.animate().alpha(0.0f).setDuration(1500);
        }
    }
    
    private void contactDeveloper(){
        //TODO
    }

    private void arvhiveQuote() {
        Date today = new Date();
        String[] dateStringParts = today.toString().split(" ");
        //example: "Sun Dec 18 04:54:14 GMT+01:00 2016"
        String key = dateStringParts[5] + dateStringParts[1] + dateStringParts[2];

        if(mArchives.getString(key, null) == null){
            mArchives.edit().putString(key, mQuote).apply();        
        }
    }

    public void archivesButtonOnClick(View view){
        Intent intent = new Intent(this, ArchiveActivity.class);
        startActivity(intent);
    }

    class GetQuoteTask extends AsyncTask<Void, Void, String>
    {
        @Override
        protected String doInBackground(Void... voids) {
            try{
                URL apiUrl = new URL("http://52.208.157.181:1994/api/Emperor");
                HttpURLConnection urlConnection = (HttpURLConnection) apiUrl.openConnection();
                try{
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String quote = bufferedReader.readLine();
                    bufferedReader.close();
                    urlConnection.disconnect();
                    return quote;
                } finally {
                    urlConnection.disconnect();
                }
            }catch (Exception e){
                Log.e("Error with api request!", e.getMessage(), e);
                return null;
            }
        }

        @Override
        public void onPostExecute(String response){
            if(response == null){
                mQuote = null;
                return;
            }
            Log.i("INFO", response);
            mQuote = response.replaceAll("^\"|\"$", "");
            mQuoteTextView.setText(mQuote);
        }

    }
}
