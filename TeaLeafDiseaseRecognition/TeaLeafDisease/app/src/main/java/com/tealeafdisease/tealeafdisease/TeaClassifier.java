package com.tealeafdisease.tealeafdisease;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class TeaClassifier {
    private static final String TAG = "TeaClassifier";

    private final Interpreter interpreter;
    private final List<String> labels;
    private final int inputSize; // e.g., 224
    private final int numChannels; // usually 3

    /**
     * Construct classifier. Model file and labels must be present under assets/.
     *
     * @param context      app context
     * @param modelAsset   file name in assets (e.g., "tea_cnn_model.tflite")
     * @param labelsAsset  file name in assets (e.g., "labels.txt")
     * @param inputSize    model input width/height (e.g., 224)
     * @throws IOException if assets missing
     */
    public TeaClassifier(Context context, String modelAsset, String labelsAsset, int inputSize) throws IOException {
        this.inputSize = inputSize;

        // Load model
        MappedByteBuffer modelBuffer = loadModelFile(context, modelAsset);

        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(4);
        interpreter = new Interpreter(modelBuffer, options);

        // Load labels
        labels = loadLabels(context, labelsAsset);

        // detect input channels (fallback to 3)
        int[] shape = interpreter.getInputTensor(0).shape(); // e.g., [1, H, W, C]
        if (shape.length == 4) {
            numChannels = shape[3];
        } else {
            numChannels = 3;
        }

        Log.i(TAG, "Model loaded. Input size: " + inputSize + "x" + inputSize + "x" + numChannels + ", labels: " + labels.size());
    }

    public void close() {
        interpreter.close();
    }

    public List<String> getLabels() {
        return labels;
    }

    /**
     * Classify bitmap and return best label + confidence (0..1)
     * Run off UI thread.
     */
    public Result classify(Bitmap bitmap) {
        Bitmap resized = preprocessBitmap(bitmap, inputSize, inputSize);

        // Create input ByteBuffer (float32 normalized 0..1)
        ByteBuffer inputBuffer = convertBitmapToByteBuffer(resized);

        // Prepare output
        float[][] output = new float[1][labels.size()];

        // Run inference
        interpreter.run(inputBuffer, output);

        // Find best
        float[] probs = output[0];
        int best = 0;
        float bestScore = probs[0];
        for (int i = 1; i < probs.length; i++) {
            if (probs[i] > bestScore) {
                bestScore = probs[i];
                best = i;
            }
        }

        String label = labels.get(best);
        float confidence = bestScore;

        return new Result(label, confidence);
    }

    // ---------- Helpers ----------

    private static MappedByteBuffer loadModelFile(Context context, String assetFileName) throws IOException {
        try (AssetFileDescriptor fileDescriptor = context.getAssets().openFd(assetFileName);
             FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
             FileChannel channel = inputStream.getChannel()) {

            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            return channel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }
    }

    private static List<String> loadLabels(Context context, String labelsFile) throws IOException {
        List<String> out = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open(labelsFile)))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) out.add(line);
            }
        }
        return out;
    }

    // Resize center-crop + scale exactly to desired size
    private static Bitmap preprocessBitmap(Bitmap src, int dstWidth, int dstHeight) {
        if (src == null) return null;
        int srcW = src.getWidth();
        int srcH = src.getHeight();

        float scale = Math.max((float) dstWidth / srcW, (float) dstHeight / srcH);
        int scaledW = Math.round(scale * srcW);
        int scaledH = Math.round(scale * srcH);

        Bitmap scaled = Bitmap.createScaledBitmap(src, scaledW, scaledH, true);

        // center crop
        int x = Math.max(0, (scaled.getWidth() - dstWidth) / 2);
        int y = Math.max(0, (scaled.getHeight() - dstHeight) / 2);

        return Bitmap.createBitmap(scaled, x, y, dstWidth, dstHeight);
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        // float32 input, order: r,g,b normalized to [0,1]
        int bytesPerChannel = 4;
        ByteBuffer buffer = ByteBuffer.allocateDirect(inputSize * inputSize * numChannels * bytesPerChannel);
        buffer.order(ByteOrder.nativeOrder());

        int[] pixels = new int[inputSize * inputSize];
        bitmap.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize);

        int p = 0;
        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                int val = pixels[p++];
                float r = ((val >> 16) & 0xFF) / 255.0f;
                float g = ((val >> 8) & 0xFF) / 255.0f;
                float b = (val & 0xFF) / 255.0f;
                buffer.putFloat(r);
                if (numChannels > 1) buffer.putFloat(g);
                if (numChannels > 2) buffer.putFloat(b);
            }
        }
        buffer.rewind();
        return buffer;
    }

    // Result container
    public static class Result {
        public final String label;
        public final float confidence; // 0..1

        public Result(String label, float confidence) {
            this.label = label;
            this.confidence = confidence;
        }
    }
}
