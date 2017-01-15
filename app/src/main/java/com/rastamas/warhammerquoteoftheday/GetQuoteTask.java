package com.rastamas.warhammerquoteoftheday;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Rasta on 1/15/2017.
 */

class GetQuoteTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... keys) {
            try {
                URL apiUrl = new URL("http://52.208.157.181:1994/api/Emperor/" + keys[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) apiUrl.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String quote = bufferedReader.readLine();
                    bufferedReader.close();
                    urlConnection.disconnect();
                    return keys[0] + "#" + quote;
                } catch (ConnectException e){
                    Log.d("Error: ", e.getMessage());
                    return "%Network error!";
                } catch (FileNotFoundException e) {
                    Log.d("Error: ", e.getMessage());
                    return "%Missing quote!";
                } catch (Exception e) {
                    return "%";
                }
                finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("Error with api request!", e.getMessage(), e);
                return null;
            }
        }

}
