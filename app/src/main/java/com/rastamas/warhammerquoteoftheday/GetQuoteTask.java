package com.rastamas.warhammerquoteoftheday;

import android.os.AsyncTask;
import android.util.Log;

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
                String quoteId = keys[0];
                String dateKey = keys[1];
                String API_SERVER_ADDRESS = App.getContext().getString(R.string.API_SERVER_ADDRESS);
                URL apiUrl = new URL(API_SERVER_ADDRESS + quoteId);
                HttpURLConnection urlConnection = (HttpURLConnection) apiUrl.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String quote = bufferedReader.readLine();
                    bufferedReader.close();
                    urlConnection.disconnect();
                    return quoteId + "#" + dateKey + "#" + quote;
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
                return "%Server error! Please contact the developer!";
            }
        }

}
