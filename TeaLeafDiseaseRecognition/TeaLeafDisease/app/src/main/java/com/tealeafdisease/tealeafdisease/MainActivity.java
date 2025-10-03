package com.tealeafdisease.tealeafdisease;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private ImageView imagePreview;
    private Button btnCamera, btnGallery, btnAnalyze, btnReset;
    private Button btnAbout, btnLeafInfo, btnStats, btnHowToUse, btnExit;
    private LinearLayout llExtraButtons;

    private Bitmap selectedBitmap;
    private File savedImageFile;
    private int analyzeClickCount = 0;

    private ActivityResultLauncher<Void> takePictureLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    private static final int MODEL_INPUT = 224;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imagePreview = findViewById(R.id.imagePreview);
        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);
        btnAnalyze = findViewById(R.id.btnAnalyze);
        btnReset = findViewById(R.id.btnReset);

        btnAbout = findViewById(R.id.btnAbout);
        btnLeafInfo = findViewById(R.id.btnLeafInfo);
        btnStats = findViewById(R.id.btnStats);
        btnHowToUse = findViewById(R.id.btnHowToUse);
        btnExit = findViewById(R.id.btnExit);
        llExtraButtons = findViewById(R.id.llExtraButtons);

        // Launchers
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        takeCameraPicture();
                    } else {
                        Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
                    }
                });

        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                bitmap -> {
                    if (bitmap != null) {
                        Bitmap resized = resizeAndCenterCrop(bitmap, MODEL_INPUT, MODEL_INPUT);
                        showSelectedImage(resized);
                    } else {
                        Toast.makeText(this, "Failed to take picture", Toast.LENGTH_SHORT).show();
                    }
                });

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        Bitmap bmp = uriToBitmap(uri);
                        if (bmp != null) {
                            Bitmap resized = resizeAndCenterCrop(bmp, MODEL_INPUT, MODEL_INPUT);
                            showSelectedImage(resized);
                        }
                    }
                });

        // Button actions
        btnCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                takeCameraPicture();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        btnGallery.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        btnAnalyze.setOnClickListener(v -> {
            if (selectedBitmap != null) {
                try {
                    savedImageFile = saveBitmapToFile(selectedBitmap);
                    analyzeClickCount++;
                    Intent intent = ResultActivity.createIntent(
                            MainActivity.this,
                            savedImageFile.getAbsolutePath(),
                            analyzeClickCount
                    );
                    startActivity(intent);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnReset.setOnClickListener(v -> resetUi());

        btnAbout.setOnClickListener(v -> startActivity(new Intent(this, AboutActivity.class)));
        btnLeafInfo.setOnClickListener(v -> startActivity(new Intent(this, LeafInfoActivity.class)));
        btnStats.setOnClickListener(v -> startActivity(new Intent(this, StatisticsActivity.class)));
        btnHowToUse.setOnClickListener(v -> startActivity(new Intent(this, HowToUseActivity.class)));

        btnExit.setOnClickListener(v -> {
            if (doubleBackToExitPressedOnce) {
                finishAffinity(); // close the app
            } else {
                doubleBackToExitPressedOnce = true;
                Toast.makeText(this, "Double click to exit the app", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
            }
        });

        btnAnalyze.setVisibility(View.GONE);
        btnReset.setVisibility(View.GONE);
    }

    private void takeCameraPicture() {
        takePictureLauncher.launch(null);
    }

    private void showSelectedImage(Bitmap bitmap) {
        selectedBitmap = bitmap;
        analyzeClickCount = 0;
        imagePreview.setImageBitmap(bitmap);
        imagePreview.setVisibility(View.VISIBLE);
        btnAnalyze.setVisibility(View.VISIBLE);
        btnReset.setVisibility(View.VISIBLE);
        llExtraButtons.setVisibility(View.VISIBLE);
    }

    private void resetUi() {
        selectedBitmap = null;
        analyzeClickCount = 0;
        imagePreview.setImageDrawable(null);
        imagePreview.setVisibility(View.GONE);
        btnAnalyze.setVisibility(View.GONE);
        btnReset.setVisibility(View.GONE);
        llExtraButtons.setVisibility(View.VISIBLE);
    }

    private Bitmap uriToBitmap(Uri uri) {
        try {
            if (Build.VERSION.SDK_INT >= 28) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
                return ImageDecoder.decodeBitmap(source);
            } else {
                return MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private File saveBitmapToFile(Bitmap bitmap) throws IOException {
        File file = new File(getCacheDir(), "selected_image.png");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
        }
        return file;
    }

    private Bitmap resizeAndCenterCrop(Bitmap src, int targetW, int targetH) {
        if (src == null) return null;
        int srcW = src.getWidth();
        int srcH = src.getHeight();

        float scale = Math.max((float) targetW / srcW, (float) targetH / srcH);
        int scaledW = Math.round(scale * srcW);
        int scaledH = Math.round(scale * srcH);

        Bitmap scaled = Bitmap.createScaledBitmap(src, scaledW, scaledH, true);

        int x = Math.max(0, (scaled.getWidth() - targetW) / 2);
        int y = Math.max(0, (scaled.getHeight() - targetH) / 2);

        return Bitmap.createBitmap(scaled, x, y, targetW, targetH);
    }
}
