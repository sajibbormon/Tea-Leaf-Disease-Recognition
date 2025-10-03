package com.tealeafdisease.tealeafdisease;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Enable clicking on email and links
        TextView emailSajib = findViewById(R.id.emailSajib);
        TextView emailHasan = findViewById(R.id.emailHasan);
        TextView emailAli = findViewById(R.id.emailAli);
        TextView tvPaperLink = findViewById(R.id.tvPaperLink);
        TextView tvDOI = findViewById(R.id.tvDOI);

        emailSajib.setMovementMethod(LinkMovementMethod.getInstance());
        emailHasan.setMovementMethod(LinkMovementMethod.getInstance());
        emailAli.setMovementMethod(LinkMovementMethod.getInstance());
        tvPaperLink.setMovementMethod(LinkMovementMethod.getInstance());
        tvDOI.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
