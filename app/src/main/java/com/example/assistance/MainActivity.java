package com.example.assistance;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private OkHttpClient client = new OkHttpClient();
    private String apiKey = BuildConfig.GEMINI_API_KEY;

    // UI references
    private TextInputEditText inputEditText;
    private MaterialButton searchButton, refreshButton;
    private TextView resultTextView, emptyState;
    private LottieAnimationView lottieLoading;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find views
        inputEditText = findViewById(R.id.inputEditText);
        searchButton = findViewById(R.id.searchButton);
        refreshButton = findViewById(R.id.refreshButton);
        resultTextView = findViewById(R.id.resultTextView);
        emptyState = findViewById(R.id.emptyState);
        lottieLoading = findViewById(R.id.lottieLoading);
        scrollView = findViewById(R.id.scrollView);
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        // Initial state
        resultTextView.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);

        // Search button click
        searchButton.setOnClickListener(v -> {
            String prompt = inputEditText.getText().toString().trim();
            if (prompt.isEmpty()) {
                Snackbar.make(searchButton, "Please enter a query!", Snackbar.LENGTH_SHORT).show();
                return;
            }
            showLoading(true);
            sendRequest(prompt, result -> runOnUiThread(() -> {
                showLoading(false);
                if (result.startsWith("Failed") || result.startsWith("Error") || result.startsWith("Exception")) {
                    emptyState.setText(result);
                    emptyState.setVisibility(View.VISIBLE);
                    resultTextView.setVisibility(View.GONE);
                } else {
                    resultTextView.setText(result.trim());
                    resultTextView.setAlpha(0f);
                    resultTextView.setVisibility(View.VISIBLE);
                    resultTextView.animate().alpha(1f).setDuration(300).start();
                    emptyState.setVisibility(View.GONE);
                    // Auto-scroll to result
                    scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
                }
            }));
        });

        // Refresh button click
        refreshButton.setOnClickListener(v -> {
            inputEditText.setText("");
            resultTextView.setText("");
            resultTextView.setVisibility(View.GONE);
            emptyState.setText("Thank You for using Assistant!");
            emptyState.setVisibility(View.VISIBLE);
            Snackbar.make(refreshButton, "Developer Virenkumar", Snackbar.LENGTH_SHORT).show();
        });

        // Long press to copy result
        resultTextView.setOnLongClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("AI Result", resultTextView.getText());
            clipboard.setPrimaryClip(clip);
            Snackbar.make(resultTextView, "Copied to clipboard!", Snackbar.LENGTH_SHORT).show();
            return true;
        });
    }

    private void showLoading(boolean show) {
        if (show) {
            lottieLoading.setVisibility(View.VISIBLE);
            lottieLoading.playAnimation();
            searchButton.setEnabled(false);
            refreshButton.setEnabled(false);
        } else {
            lottieLoading.setVisibility(View.GONE);
            lottieLoading.cancelAnimation();
            searchButton.setEnabled(true);
            refreshButton.setEnabled(true);
        }
    }

    private void sendRequest(String prompt, ResponseCallback callback) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

        try {
            JSONObject part = new JSONObject().put("text", prompt);
            JSONObject content = new JSONObject().put("parts", new JSONArray().put(part));
            JSONObject body = new JSONObject().put("contents", new JSONArray().put(content));

            RequestBody requestBody = RequestBody.create(
                    body.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder().url(url).post(requestBody).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onResult("Failed: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String res = response.body().string();
                        Log.d("API_RESPONSE", res);
                        try {
                            JSONObject json = new JSONObject(res);
                            String reply = json
                                    .getJSONArray("candidates")
                                    .getJSONObject(0)
                                    .getJSONObject("content")
                                    .getJSONArray("parts")
                                    .getJSONObject(0)
                                    .getString("text");
                            callback.onResult(reply);
                        } catch (Exception e) {
                            callback.onResult("JSON Parsing Error: " + e.getMessage());
                        }
                    } else {
                        callback.onResult("Error: " + response.message());
                    }
                }
            });

        } catch (Exception e) {
            callback.onResult("Exception: " + e.getMessage());
        }
    }

    interface ResponseCallback {
        void onResult(String result);
    }
}
