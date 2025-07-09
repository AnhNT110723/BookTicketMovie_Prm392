package com.example.bookingticketmove_prm392;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.bookingticketmove_prm392.database.dao.MovieDAO;
import com.example.bookingticketmove_prm392.database.dao.MovieFavoriteDAO;
import com.example.bookingticketmove_prm392.models.Movie;
import com.example.bookingticketmove_prm392.models.MovieFavorite;
import com.example.bookingticketmove_prm392.utils.ImageUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MovieDetailActivity extends AppCompatActivity {
    private static final String TAG = "MovieDetailActivity";
    public static final String EXTRA_MOVIE_ID = "movie_id";
    // UI Components
    private Toolbar toolbar;
    private ImageView posterImageView;
    private TextView titleTextView;
    private TextView descriptionTextView;
    private TextView directorTextView;
    private TextView durationTextView;
    private TextView releaseDateTextView;
    private TextView ratingTextView;
    private TextView priceTextView;
    private ChipGroup genreChipGroup;
    private Button bookTicketButton;

    // Embedded Trailer Components
    private CardView trailerCard;
    private WebView embeddedTrailerWebview;
    private LinearLayout trailerLoadingLayout;
    private LinearLayout trailerErrorLayout;
    private Button fullscreenButton;
    private Button retryTrailerButton;    // Data
    private Movie currentMovie;
    private int movieId;
    private boolean isFullscreen = false;
    private Button btn_comments;

    // Fullscreen video handling
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private FrameLayout fullscreenContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        // Get movie ID from intent
        movieId = getIntent().getIntExtra(EXTRA_MOVIE_ID, -1);
        if (movieId == -1) {
            Toast.makeText(this, "Invalid movie selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        loadMovieDetails();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        posterImageView = findViewById(R.id.poster_image_view);
        titleTextView = findViewById(R.id.title_text_view);
        descriptionTextView = findViewById(R.id.description_text_view);
        directorTextView = findViewById(R.id.director_text_view);
        durationTextView = findViewById(R.id.duration_text_view);
        releaseDateTextView = findViewById(R.id.release_date_text_view);
        ratingTextView = findViewById(R.id.rating_text_view);
        priceTextView = findViewById(R.id.price_text_view);
        genreChipGroup = findViewById(R.id.genre_chip_group);
        bookTicketButton = findViewById(R.id.book_ticket_button);

        // Initialize embedded trailer components
        trailerCard = findViewById(R.id.trailer_card);
        embeddedTrailerWebview = findViewById(R.id.embedded_trailer_webview);
        trailerLoadingLayout = findViewById(R.id.trailer_loading_layout);
        trailerErrorLayout = findViewById(R.id.trailer_error_layout);
        fullscreenButton = findViewById(R.id.fullscreen_button);
        retryTrailerButton = findViewById(R.id.retry_trailer_button);
        btn_comments = findViewById(R.id.btn_comments);

        // Initialize fullscreen container
        fullscreenContainer = new FrameLayout(this);
        fullscreenContainer.setBackgroundColor(Color.BLACK);

        setupEmbeddedTrailer();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Movie Details");
        }
    }

    private void setupEmbeddedTrailer() {
        // Configure WebView settings
        WebSettings webSettings = embeddedTrailerWebview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(false);
        webSettings.setAllowFileAccess(false);
        webSettings.setAllowContentAccess(false);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // Set WebView client to handle page loading
        embeddedTrailerWebview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                showTrailerLoadingState();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                showTrailerWebView();
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.e(TAG, "WebView error: " + description);
                showTrailerErrorState();
            }
        });
        // Set WebChromeClient for better media support
        embeddedTrailerWebview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
                    showTrailerWebView();
                }
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
                // Handle fullscreen video playback
                enterFullscreenVideo(view, callback);
            }

            @Override
            public void onHideCustomView() {
                super.onHideCustomView();
                // Exit fullscreen video playback
                exitFullscreenVideo();
            }
        });
    }

    private void loadEmbeddedTrailer(String trailerUrl) {
        if (trailerUrl == null || trailerUrl.isEmpty()) {
            trailerCard.setVisibility(View.GONE);
            return;
        }

        try {
            // Show trailer card
            trailerCard.setVisibility(View.VISIBLE);

            // Convert YouTube URL to embeddable format
            String embedUrl = convertToEmbedUrl(trailerUrl);

            if (embedUrl != null) {
                // Create HTML content for embedded YouTube player
                String htmlContent = createEmbedHtml(embedUrl);
                embeddedTrailerWebview.loadDataWithBaseURL("https://www.youtube.com", htmlContent, "text/html", "UTF-8", null);
            } else {
                showTrailerErrorState();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading embedded trailer: " + e.getMessage());
            showTrailerErrorState();
        }
    }

    private String convertToEmbedUrl(String youtubeUrl) {
        try {
            // Extract video ID from various YouTube URL formats
            String videoId = null;

            if (youtubeUrl.contains("youtube.com/watch?v=")) {
                videoId = youtubeUrl.split("v=")[1].split("&")[0];
            } else if (youtubeUrl.contains("youtu.be/")) {
                videoId = youtubeUrl.split("youtu.be/")[1].split("\\?")[0];
            } else if (youtubeUrl.contains("youtube.com/embed/")) {
                return youtubeUrl; // Already in embed format
            }

            if (videoId != null && !videoId.isEmpty()) {
                return "https://www.youtube.com/embed/" + videoId + "?autoplay=0&rel=0&modestbranding=1";
            }
        } catch (Exception e) {
            Log.e(TAG, "Error converting YouTube URL: " + e.getMessage());
        }
        return null;
    }

    private String createEmbedHtml(String embedUrl) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<style>" +
                "body { margin: 0; padding: 0; background: #000; }" +
                "iframe { width: 100%; height: 320px; border: none; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<iframe src='" + embedUrl + "' " +
                "frameborder='0' " +
                "allow='accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture' " +
                "allowfullscreen>" +
                "</iframe>" +
                "</body>" +
                "</html>";
    }

    private void showTrailerLoadingState() {
        trailerLoadingLayout.setVisibility(View.VISIBLE);
        embeddedTrailerWebview.setVisibility(View.GONE);
        trailerErrorLayout.setVisibility(View.GONE);
    }

    private void showTrailerWebView() {
        trailerLoadingLayout.setVisibility(View.GONE);
        embeddedTrailerWebview.setVisibility(View.VISIBLE);
        trailerErrorLayout.setVisibility(View.GONE);
    }

    private void showTrailerErrorState() {
        trailerLoadingLayout.setVisibility(View.GONE);
        embeddedTrailerWebview.setVisibility(View.GONE);
        trailerErrorLayout.setVisibility(View.VISIBLE);
    }

    private void loadMovieDetails() {
        new LoadMovieDetailsTask().execute(movieId);
    }

    private void setupClickListeners() {
        bookTicketButton.setOnClickListener(v -> {
            if (currentMovie != null) {
                // Check if user is logged in
                SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
                int userId = prefs.getInt("userId", -1);

                if (!isLoggedIn) {
                    Toast.makeText(this, "Please login to book tickets", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                    return;
                }

                // Navigate to seat selection or cinema selection
                Intent intent = new Intent(this, CinemaSelectionActivity.class);
                intent.putExtra("movie_id", currentMovie.getMovieId());
                intent.putExtra("movie_title", currentMovie.getTitle());
                intent.putExtra("movie_price", currentMovie.getPrice());
                intent.putExtra("movie_image", currentMovie.getPosterUrl());
                startActivity(intent);
            }
        });

        // Fullscreen button click listener
        fullscreenButton.setOnClickListener(v -> {
            if (currentMovie != null && currentMovie.getTrailerUrl() != null && !currentMovie.getTrailerUrl().isEmpty()) {
                try {
                    // Load trailer with fullscreen capabilities
                    String embedUrl = convertToEmbedUrl(currentMovie.getTrailerUrl());
                    if (embedUrl != null) {
                        String fullscreenHtml = createFullscreenEmbedHtml(embedUrl);
                        embeddedTrailerWebview.loadDataWithBaseURL("https://www.youtube.com", fullscreenHtml, "text/html", "UTF-8", null);
                        Toast.makeText(this, "Tap the video to enter fullscreen", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading fullscreen trailer: " + e.getMessage());
                    Toast.makeText(this, "Error loading fullscreen trailer", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Trailer not available", Toast.LENGTH_SHORT).show();
            }
        });

        // Retry button click listener
        retryTrailerButton.setOnClickListener(v -> {
            if (currentMovie != null && currentMovie.getTrailerUrl() != null && !currentMovie.getTrailerUrl().isEmpty()) {
                loadEmbeddedTrailer(currentMovie.getTrailerUrl());
                Toast.makeText(this, "Retrying trailer load...", Toast.LENGTH_SHORT).show();
            }
        });

        // All comments button click listener
        btn_comments.setOnClickListener(v -> {

            Intent intent = new Intent(this, FeedbackActivity.class);
            intent.putExtra("movie_id", currentMovie.getMovieId());
            startActivity(intent);
        });
    }

    private void displayMovieDetails(Movie movie) {
        if (movie == null) {
            Toast.makeText(this, "Movie details not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentMovie = movie;

        // Set title
        titleTextView.setText(movie.getTitle());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(movie.getTitle());
        }

        // Set description
        descriptionTextView.setText(movie.getDescription());

        // Set director
        directorTextView.setText("Directed by " + movie.getDirector());

        // Set duration
        int hours = movie.getDuration() / 60;
        int minutes = movie.getDuration() % 60;
        String durationText = hours > 0 ? hours + "h " + minutes + "m" : minutes + "m";
        durationTextView.setText(durationText);

        // Set release date
        if (movie.getReleaseDate() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            releaseDateTextView.setText("Released: " + dateFormat.format(movie.getReleaseDate()));
        }

        // Set rating
        ratingTextView.setText(String.format(Locale.getDefault(), "★ %.1f", movie.getRating()));

        // Set price
        priceTextView.setText(String.format(Locale.getDefault(), "$%.2f", movie.getPrice()));
        // Set genre chips
        setupGenreChips(movie.getGenre());

        // Load poster image using ImageUtils
        ImageUtils.loadMoviePosterFitCenter(this, posterImageView, movie.getPosterUrl());
        // Enable/disable book button based on movie status
        bookTicketButton.setEnabled(movie.isActive());
        if (!movie.isActive()) {
            bookTicketButton.setText("Not Available");
        }

        // Load embedded trailer if available
        if (movie.getTrailerUrl() != null && !movie.getTrailerUrl().isEmpty()) {
            loadEmbeddedTrailer(movie.getTrailerUrl());
        } else {
            trailerCard.setVisibility(View.GONE);
        }
    }

    private void setupGenreChips(String genres) {
        genreChipGroup.removeAllViews();

        if (genres != null && !genres.isEmpty()) {
            String[] genreArray = genres.split(",");
            for (String genre : genreArray) {
                Chip chip = new Chip(this);
                chip.setText(genre.trim());
                chip.setClickable(false);
                genreChipGroup.addView(chip);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (embeddedTrailerWebview != null) {
            embeddedTrailerWebview.destroy();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.movie_detail_menu, menu);

        // Check if user is logged in
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        if (isLoggedIn) {
            int userId = prefs.getInt("userId", -1);
            new getMovieFavorite(userId, movieId, menu).execute();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_share) {
            shareMovie();
            return true;
        } else if (id == R.id.action_favorite) {
            toggleFavorite(item);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void shareMovie() {
        if (currentMovie != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareText = "Check out this movie: " + currentMovie.getTitle() +
                    "\nDirected by: " + currentMovie.getDirector() +
                    "\nRating: " + currentMovie.getRating() + "/10";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "Share Movie"));
        }
    }

    private void toggleFavorite(MenuItem item) {
        // TODO: Implement favorite functionality
        //Toast.makeText(this, "Favorite feature coming soon", Toast.LENGTH_SHORT).show();

        // Check if user is logged in
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (!isLoggedIn) {
            Toast.makeText(this, "Please login to add your movie favorite", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return;
        }

        int userId = prefs.getInt("userId", -1);
        new toggleMovieFavorite(userId, movieId, item).execute();


    }

    private String createFullscreenEmbedHtml(String embedUrl) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<style>" +
                "body { margin: 0; padding: 0; background: #000; }" +
                "iframe { width: 100%; height: 320px; border: none; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<iframe src='" + embedUrl + "&autoplay=0&fs=1' " +
                "frameborder='0' " +
                "allow='accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; fullscreen' " +
                "allowfullscreen " +
                "webkitallowfullscreen " +
                "mozallowfullscreen>" +
                "</iframe>" +
                "</body>" +
                "</html>";
    }

    private void enterFullscreenVideo(View view, WebChromeClient.CustomViewCallback callback) {
        if (customView != null) {
            callback.onCustomViewHidden();
            return;
        }

        customView = view;
        customViewCallback = callback;

        // Hide system UI
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        // Set landscape orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Add custom view to fullscreen container
        fullscreenContainer.addView(customView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // Add fullscreen container to root view
        ViewGroup rootView = (ViewGroup) getWindow().getDecorView();
        rootView.addView(fullscreenContainer, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        isFullscreen = true;
    }

    private void exitFullscreenVideo() {
        if (customView == null) {
            return;
        }

        // Remove fullscreen container from root view
        ViewGroup rootView = (ViewGroup) getWindow().getDecorView();
        rootView.removeView(fullscreenContainer);

        // Remove custom view from fullscreen container
        fullscreenContainer.removeView(customView);

        // Restore system UI
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

        // Restore portrait orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Clean up
        customView = null;
        if (customViewCallback != null) {
            customViewCallback.onCustomViewHidden();
            customViewCallback = null;
        }

        isFullscreen = false;
    }

    @Override
    public void onBackPressed() {
        if (isFullscreen) {
            exitFullscreenVideo();
        } else {
            super.onBackPressed();
        }
    }

    private class LoadMovieDetailsTask extends AsyncTask<Integer, Void, Movie> {
        @Override
        protected Movie doInBackground(Integer... params) {
            try {
                MovieDAO movieDAO = new MovieDAO();
                return movieDAO.getMovieById(params[0]);
            } catch (Exception e) {
                Log.e(TAG, "Error loading movie details", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Movie movie) {
            displayMovieDetails(movie);
        }
    }

    private class toggleMovieFavorite extends AsyncTask<Void, Void, Boolean> {
        private int userId, movieId;
        private MenuItem menuItem;
        private MovieFavorite movieFavorite;

        public toggleMovieFavorite(int userId, int movieId, MenuItem menuItem) {
            this.userId = userId;
            this.movieId = movieId;
            this.menuItem = menuItem;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                MovieFavoriteDAO movieFavoriteDAO = new MovieFavoriteDAO();
                 movieFavorite = movieFavoriteDAO.getMovieFavorites(userId, movieId);
                if(movieFavorite != null){
                    return movieFavoriteDAO.deleteMovieFavorite(userId, movieId);
                }
                return movieFavoriteDAO.addMovieFavorite(new MovieFavorite(userId, movieId));
            } catch (SQLException e) {
                Log.e(TAG, "Error adding movie favorite", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                if(movieFavorite != null){
                    Toast.makeText(MovieDetailActivity.this, "Đã xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
                    changeIconColor(menuItem, R.color.default_date_background);
                    return;
                }else{
                    Toast.makeText(MovieDetailActivity.this, "Đã thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show();
                    changeIconColor(menuItem, R.color.red);
                }

            } else {
                Toast.makeText(MovieDetailActivity.this, "Thao tác thất bại", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private class getMovieFavorite extends AsyncTask<Void, Void, Boolean> {
        private int userId, movieId;
        private Menu menu;

        public getMovieFavorite(int userId, int movieId, Menu menu) {
            this.userId = userId;
            this.movieId = movieId;
            this.menu = menu;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                MovieFavoriteDAO movieFavoriteDAO = new MovieFavoriteDAO();
                MovieFavorite movieFavorite = movieFavoriteDAO.getMovieFavorites(userId, movieId);
                if (movieFavorite != null) return true;
            } catch (SQLException e) {
                Log.e(TAG, "Error loading movie favorite", e);
                return false;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                MenuItem favoriteItem = menu.findItem(R.id.action_favorite);
               changeIconColor(favoriteItem, R.color.red);
            }
        }
    }

    private void changeIconColor(MenuItem item, @ColorRes int colorResId) {
        Drawable icon = item.getIcon();
        if (icon != null) {
            icon.mutate(); // để tránh ảnh hưởng icon gốc
            icon.setTint(ContextCompat.getColor(this, colorResId));
        }
    }

}
