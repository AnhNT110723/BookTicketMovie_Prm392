package com.example.bookingticketmove_prm392.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookingticketmove_prm392.R;
import androidx.appcompat.widget.Toolbar;

public class ShowtimeSelectionActivity extends AppCompatActivity {
    
    // UI Components
    private Toolbar toolbar;
    private TextView movieTitleText;
    private TextView cinemaNameText;
    private TextView moviePriceText;
    
    // Data
    private int movieId;
    private String movieTitle;
    private double moviePrice;
    private int cinemaId;
    private String cinemaName;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showtime_selection);
        
        // Get data from intent
        movieId = getIntent().getIntExtra("movie_id", -1);
        movieTitle = getIntent().getStringExtra("movie_title");
        moviePrice = getIntent().getDoubleExtra("movie_price", 0.0);
        cinemaId = getIntent().getIntExtra("cinema_id", -1);
        cinemaName = getIntent().getStringExtra("cinema_name");
        
        if (movieId == -1 || movieTitle == null || cinemaId == -1 || cinemaName == null) {
            Toast.makeText(this, "Invalid booking data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        setupToolbar();
        displayBookingInfo();
        
        // TODO: This is a placeholder activity for showtime selection
        // Implement showtime list, seat selection here
        Toast.makeText(this, "Showtime Selection - Coming Soon!\nMovie: " + movieTitle + "\nCinema: " + cinemaName, Toast.LENGTH_LONG).show();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        movieTitleText = findViewById(R.id.movie_title_text);
        cinemaNameText = findViewById(R.id.cinema_name_text);
        moviePriceText = findViewById(R.id.movie_price_text);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Select Showtime");
        }
    }
    
    private void displayBookingInfo() {
        movieTitleText.setText(movieTitle);
        cinemaNameText.setText(cinemaName);
        moviePriceText.setText(String.format("$%.2f", moviePrice));
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
