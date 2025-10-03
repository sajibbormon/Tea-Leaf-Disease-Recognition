package com.tealeafdisease.tealeafdisease;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HowToUseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_use);

        TextView tv = findViewById(R.id.tvHowToUse);
        tv.setText(
                "📸 How to Use this App:\n\n" +
                        "1️⃣ Take a Photo or Choose from File manager, Gallery, GDrive etc. of a tea leaf.\n\n" +
                        "2️⃣ Click 'Analyze Disease' to detect the disease.\n\n" +
                        "3️⃣ The result shows prediction, confidence, symptoms, and prevention.\n\n" +
                        "4️⃣ Use 'Know More' button to search on Google for more info.\n\n" +
                        "📊 Statistics Page:\n" +
                        "- View total leaves checked.\n" +
                        "- See how many leaves were healthy or diseased.\n" +
                        "- Track trends by date or custom ranges.\n (From a specific date to previous days)\n" +
                        "- Can analyze weekly and monthly data.\n" +
                        "- Statistics is shown by Bar Chart and Pie Chart.\n\n"+
                        "ℹ️ This helps tea farmers keep track of garden health and manage diseases effectively."
        );
    }
}
