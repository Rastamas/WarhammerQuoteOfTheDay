package com.rastamas.warhammerquoteoftheday;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ContactActivity extends AppCompatActivity {

    private EditText mEmailEditText;
    private EditText mMessageEditText;
    private Button mSendButton;
    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        mSendButton = (Button) findViewById(R.id.button_send);
        mEmailEditText = (EditText) findViewById(R.id.emailEditText);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mPreferences = getSharedPreferences("WarhammerQuotePreferences", 0);

        setupToolBarText();
        setupButtonImage();
    }


    private void setupButtonImage() {
        IntTuple dimensions = Helper.getScreenSize(getWindowManager());

        Bitmap originalImage = BitmapFactory.decodeResource(getResources(), R.drawable.light_button);
        Bitmap scaledImage = Bitmap.createScaledBitmap(originalImage,
                (int) (500f / 1080 * dimensions.x),
                (int) (200f / 1920 * dimensions.y), true);
        mSendButton.setBackground(new BitmapDrawable(getResources(), scaledImage));
    }

    private void setupToolBarText() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.contact_toolbar);
        toolbar.setTitle("Contact");
        setSupportActionBar(toolbar);
    }


    public void sendButtonOnClick(View view) throws IOException {
        if (mMessageEditText.getText().toString().replace(" ", "").length() == 0) {
            Toast.makeText(this, "You have to write more than that!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (lastFeedbackWasMoreThanADayAgo()) {
            Toast.makeText(this, "Feedback sent!", Toast.LENGTH_SHORT).show();
            new RetrievePostResponse().execute(mEmailEditText.getText().toString(), mMessageEditText.getText().toString());
        } else {
            Toast.makeText(this, "Sorry, only one feedback in 12 hours", Toast.LENGTH_LONG).show();
        }
    }

    private boolean lastFeedbackWasMoreThanADayAgo() {
        SimpleDateFormat parser = new SimpleDateFormat("EEE MMM d HH:mm:ss zzzz yyyy", Locale.getDefault());
        String string = mPreferences.getString("last_feedback", "");
        if (string.equals("")) return true;
        try {
            Date lastFeedback = parser.parse(string);
            Calendar twelveHoursBeforeNow = Calendar.getInstance();
            twelveHoursBeforeNow.add(Calendar.HOUR, -12);
            return lastFeedback.before(twelveHoursBeforeNow.getTime());
        } catch (ParseException e) {
            return true;
        }
    }

    private class RetrievePostResponse extends AsyncTask<String, Void, String> {

        private Exception exception;

        protected String doInBackground(String... params) {
            StringBuilder jsonString = new StringBuilder();
            try {
                URL url = new URL(getString(R.string.API_SERVER_ADDRESS) + "feedback");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                String line;

                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setInstanceFollowRedirects(false);
                connection.connect();
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
                writer.write(params[0] + "\n" + params[1]);
                writer.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    jsonString.append(line);
                }
                reader.close();
                connection.disconnect();

                Log.d("POST RESPONSE", jsonString.toString());
            } catch (Exception ex) {
                exception = ex;
            }
            return jsonString.toString();
        }

        protected void onPostExecute(String response) {
            if (exception != null) {
                Log.d("ON_POST_EXECUTE", exception.getMessage());
                if (exception instanceof ConnectException)
                    Toast.makeText(ContactActivity.this, "Network error!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ContactActivity.this, response.replaceAll("\"", "") + "!\nThank you!", Toast.LENGTH_SHORT).show();
                mPreferences.edit().putString("last_feedback", Calendar.getInstance().getTime().toString()).apply();
            }
        }
    }
}
