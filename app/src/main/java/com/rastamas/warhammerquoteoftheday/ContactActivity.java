package com.rastamas.warhammerquoteoftheday;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ContactActivity extends AppCompatActivity {

    //TODO Implement sendButtonOnClick with "throttling"

    private EditText mEmailEditText;
    private EditText mMessageEditText;
    private Button mSendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        mSendButton = (Button) findViewById(R.id.button_send);
        mEmailEditText = (EditText) findViewById(R.id.emailEditText);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);

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
}
