package com.tealeafdisease.tealeafdisease;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    private static final String EXTRA_IMAGE_PATH = "extra_image_path";
    private static final String EXTRA_CLICK_COUNT = "extra_click_count";
    private static final int MODEL_INPUT = 224;

    private ImageView resultImage;
    private TextView tvPrediction, tvConfidence, tvDescription;
    private ProgressBar confidenceBar;
    private Button btnBack, btnKnowMore;
    private TeaClassifier classifier;

    public static Intent createIntent(Context ctx, String imagePath, int clickCount) {
        Intent i = new Intent(ctx, ResultActivity.class);
        i.putExtra(EXTRA_IMAGE_PATH, imagePath);
        i.putExtra(EXTRA_CLICK_COUNT, clickCount);
        return i;
    }

    private void saveStatistics(String disease) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        AppDatabase db = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class, "statistics-db"
        ).allowMainThreadQueries().build();

        StatisticsDao dao = db.statisticsDao();
        StatisticsEntity existing = dao.getStatByDateAndDisease(today, disease);

        if (existing != null) {
            dao.incrementCount(existing.id);
        } else {
            StatisticsEntity newStat = new StatisticsEntity(disease, 1, today);
            dao.insert(newStat);
        }
    }

    private String getDiseaseInfo(String disease) {
        switch (disease) {
            case "Tea Red Scab":
                return "⚠️ Tea Red Scab\n" +
                        "👉 Symptoms: Small reddish-brown scab-like lesions on leaves.\n" +
                        "🛡️ Prevention: Use resistant varieties, remove infected leaves, and apply fungicides if needed.";
            case "Tea Red Leaf Spot":
                return "⚠️ Tea Red Leaf Spot\n" +
                        "👉 Symptoms: Circular red-brown spots with pale centers on leaves.\n" +
                        "🛡️ Prevention: Prune affected leaves, improve air circulation, and apply protective fungicides.";
            case "Tea Leaf Blight":
                return "⚠️ Tea Leaf Blight\n" +
                        "👉 Symptoms: Large irregular brown patches, leaf wilting, and premature fall.\n" +
                        "🛡️ Prevention: Avoid overcrowding, ensure good drainage, and apply copper-based fungicides.";
            case "Healthy":
                return "✅ Healthy Leaf\n" +
                        "👉 No visible disease symptoms.\n" +
                        "🛡️ Prevention: Maintain proper nutrition and regular monitoring.";
            default:
                return "ℹ️ No additional info available.";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        resultImage = findViewById(R.id.resultImage);
        tvPrediction = findViewById(R.id.tvPrediction);
        tvConfidence = findViewById(R.id.tvConfidence);
        confidenceBar = findViewById(R.id.confidenceBar);
        tvDescription = findViewById(R.id.tvDescription);
        btnBack = findViewById(R.id.btnBack);
        btnKnowMore = findViewById(R.id.btnKnowMore);

        String imagePath = getIntent().getStringExtra(EXTRA_IMAGE_PATH);
        int clickCount = getIntent().getIntExtra(EXTRA_CLICK_COUNT, 0);

        if (imagePath == null) {
            finish();
            return;
        }

        File f = new File(imagePath);
        if (!f.exists()) {
            finish();
            return;
        }

        Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
        resultImage.setImageBitmap(bmp);

        new Thread(() -> {
            try {
                classifier = new TeaClassifier(
                        ResultActivity.this,
                        "f_tea_net_model.tflite",
                        "labels.txt",
                        MODEL_INPUT
                );

                TeaClassifier.Result res = classifier.classify(bmp);

                // ✅ Save only on first click and show Toast
                if (clickCount == 1) {
                    saveStatistics(res.label);
                    runOnUiThread(() -> Toast.makeText(
                            ResultActivity.this,
                            "Result is saved.",
                            Toast.LENGTH_SHORT
                    ).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(
                            ResultActivity.this,
                            "This image classification result is already saved!",
                            Toast.LENGTH_SHORT
                    ).show());
                }

                runOnUiThread(() -> {
                    tvPrediction.setText(String.format(Locale.getDefault(),
                            "%s %s", getString(R.string.prediction), res.label));
                    tvConfidence.setText(String.format(Locale.getDefault(),
                            "%s %.2f%%", getString(R.string.confidence), res.confidence * 100f));
                    confidenceBar.setProgress(Math.round(res.confidence * 100f));

                    // ✅ Show disease info
                    tvDescription.setText(getDiseaseInfo(res.label));

                    // ✅ Know More button action
                    btnKnowMore.setOnClickListener(v -> {
                        String query = "Tea " + res.label + " disease symptoms and prevention";
                        String url = "https://www.google.com/search?q=" + Uri.encode(query);
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    });
                });

            } catch (Exception e) {
                android.util.Log.e("ResultActivity", "Classification error", e);
                runOnUiThread(() -> {
                    tvPrediction.setText(getString(R.string.error));
                    tvConfidence.setText("");
                    tvDescription.setText("");
                    confidenceBar.setProgress(0);
                });
            }
        }).start();

        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (classifier != null) classifier.close();
    }
}
