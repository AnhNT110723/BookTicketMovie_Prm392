package com.example.bookingticketmove_prm392;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.bookingticketmove_prm392.database.dao.MovieDAO;
import com.example.bookingticketmove_prm392.models.Movie;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

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
    private Button watchTrailerButton;
    
    // Data
    private Movie currentMovie;
    private int movieId;
    
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
        watchTrailerButton = findViewById(R.id.watch_trailer_button);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Movie Details");
        }
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
                startActivity(intent);
            }
        });
        
        watchTrailerButton.setOnClickListener(v -> {
            if (currentMovie != null && currentMovie.getTrailerUrl() != null && !currentMovie.getTrailerUrl().isEmpty()) {
                // TODO: Implement trailer viewing functionality
                Toast.makeText(this, "Trailer feature coming soon", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Trailer not available", Toast.LENGTH_SHORT).show();
            }
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
        
        // TODO: Load poster image using an image loading library like Glide or Picasso
        // For now, set a placeholder
        posterImageView.setImageResource(R.drawable.ic_movie_placeholder);
        
        // Enable/disable book button based on movie status
        bookTicketButton.setEnabled(movie.isActive());
        if (!movie.isActive()) {
            bookTicketButton.setText("Not Available");
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.movie_detail_menu, menu);
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
            toggleFavorite();
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
    
    private void toggleFavorite() {
        // TODO: Implement favorite functionality
        Toast.makeText(this, "Favorite feature coming soon", Toast.LENGTH_SHORT).show();
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
}
