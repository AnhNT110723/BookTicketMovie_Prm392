package com.example.bookingticketmove_prm392.views.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookingticketmove_prm392.R;
import androidx.appcompat.widget.Toolbar;

import com.example.bookingticketmove_prm392.utils.ImageUtils;

public class TrailerActivity extends AppCompatActivity {
    private static final String TAG = "TrailerActivity";
    
    // Intent extras
    public static final String EXTRA_MOVIE_TITLE = "movie_title";
    public static final String EXTRA_TRAILER_URL = "trailer_url";
    public static final String EXTRA_POSTER_URL = "poster_url";
    
    // UI Components
    private Toolbar toolbar;
    private TextView movieTitleText;
    private ImageView posterImageView;
    private WebView trailerWebView;
    private ProgressBar loadingProgressBar;
    private Button openExternalButton;
    private TextView errorMessageText;
    
    // Data
    private String movieTitle;
    private String trailerUrl;
    private String posterUrl;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trailer);
        
        // Get data from intent
        getIntentData();
        
        if (trailerUrl == null || trailerUrl.isEmpty()) {
            Toast.makeText(this, "Trailer URL not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        setupToolbar();
        setupWebView();
        loadTrailer();
    }
    
    private void getIntentData() {
        Intent intent = getIntent();
        movieTitle = intent.getStringExtra(EXTRA_MOVIE_TITLE);
        trailerUrl = intent.getStringExtra(EXTRA_TRAILER_URL);
        posterUrl = intent.getStringExtra(EXTRA_POSTER_URL);
        
        if (movieTitle == null) movieTitle = "Movie Trailer";
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        movieTitleText = findViewById(R.id.movie_title_text);
        posterImageView = findViewById(R.id.poster_image_view);
        trailerWebView = findViewById(R.id.trailer_webview);
        loadingProgressBar = findViewById(R.id.loading_progress_bar);
        openExternalButton = findViewById(R.id.open_external_button);
        errorMessageText = findViewById(R.id.error_message_text);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Trailer");
        }
        
        movieTitleText.setText(movieTitle);
        
        // Load poster image if available
        if (posterUrl != null && !posterUrl.isEmpty()) {
            ImageUtils.loadMoviePosterFitCenter(this, posterImageView, posterUrl);
        } else {
            posterImageView.setVisibility(View.GONE);
        }
    }
    
    private void setupWebView() {
        trailerWebView.getSettings().setJavaScriptEnabled(true);
        trailerWebView.getSettings().setDomStorageEnabled(true);
        trailerWebView.getSettings().setLoadWithOverviewMode(true);
        trailerWebView.getSettings().setUseWideViewPort(true);
        trailerWebView.getSettings().setBuiltInZoomControls(false);
        trailerWebView.getSettings().setDisplayZoomControls(false);
        
        trailerWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                showLoading(true);
            }
            
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                showLoading(false);
            }
            
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                showError("Failed to load trailer: " + description);
                Log.e(TAG, "WebView error: " + description);
            }
        });
        
        // Setup external button click
        openExternalButton.setOnClickListener(v -> openInExternalApp());
    }
    
    private void loadTrailer() {
        try {
            String embedUrl = convertToEmbedUrl(trailerUrl);
            Log.d(TAG, "Loading trailer URL: " + embedUrl);
            trailerWebView.loadUrl(embedUrl);
        } catch (Exception e) {
            Log.e(TAG, "Error loading trailer", e);
            showError("Failed to load trailer");
        }
    }
    
    private String convertToEmbedUrl(String url) {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }
        
        // Convert YouTube URLs to embed format
        if (url.contains("youtube.com") || url.contains("youtu.be")) {
            return convertYouTubeUrl(url);
        }
        
        // Convert Vimeo URLs to embed format
        if (url.contains("vimeo.com")) {
            return convertVimeoUrl(url);
        }
        
        // For other URLs, return as is (assuming they're direct video links or already embed URLs)
        return url;
    }
    
    private String convertYouTubeUrl(String url) {
        String videoId = extractYouTubeVideoId(url);
        if (videoId != null) {
            return "https://www.youtube.com/embed/" + videoId + "?autoplay=1&rel=0&showinfo=0&controls=1";
        }
        return url; // Return original URL if can't extract video ID
    }
    
    private String extractYouTubeVideoId(String url) {
        // Handle different YouTube URL formats
        if (url.contains("youtube.com/watch?v=")) {
            return url.split("v=")[1].split("&")[0];
        } else if (url.contains("youtu.be/")) {
            return url.split("youtu.be/")[1].split("\\?")[0];
        } else if (url.contains("youtube.com/embed/")) {
            return url.split("embed/")[1].split("\\?")[0];
        }
        return null;
    }
    
    private String convertVimeoUrl(String url) {
        // Extract Vimeo video ID and convert to embed format
        String videoId = url.replaceAll(".*vimeo.com/", "").split("\\?")[0];
        return "https://player.vimeo.com/video/" + videoId + "?autoplay=1";
    }
    
    private void openInExternalApp() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            // Try to open with YouTube app first if it's a YouTube URL
            if (trailerUrl.contains("youtube.com") || trailerUrl.contains("youtu.be")) {
                intent.setPackage("com.google.android.youtube");
                try {
                    startActivity(intent);
                    return;
                } catch (Exception e) {
                    // YouTube app not installed, fall back to browser
                    intent.setPackage(null);
                }
            }
            
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening external app", e);
            Toast.makeText(this, "No app available to play this trailer", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showLoading(boolean show) {
        loadingProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!show) {
            errorMessageText.setVisibility(View.GONE);
        }
    }
    
    private void showError(String message) {
        showLoading(false);
        errorMessageText.setText(message);
        errorMessageText.setVisibility(View.VISIBLE);
        trailerWebView.setVisibility(View.GONE);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed() {
        if (trailerWebView.canGoBack()) {
            trailerWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (trailerWebView != null) {
            trailerWebView.destroy();
        }
    }
}
