package com.rastamas.warhammerquoteoftheday;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Map;

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

        Map archives = MainActivity.mArchives.getAll();
        for (Object record :
                archives.values()) {
            TextView recordTextView = new TextView(getApplicationContext());
            recordTextView.setText(record.toString());
            recordTextView.setTextColor(getResources().getColor(R.color.emperorAccent));
            recordTextView.setTypeface(custom_font);
            recordTextView.setTextSize(24);

            mArchiveLayout.addView(recordTextView);
        }
    }

    public void clearArchives(View view) {
        MainActivity.mArchives.edit().clear().apply();
        mArchiveLayout.removeAllViews();

    }
}
