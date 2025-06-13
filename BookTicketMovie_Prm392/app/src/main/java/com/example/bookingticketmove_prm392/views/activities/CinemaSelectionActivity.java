package com.example.bookingticketmove_prm392.views.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookingticketmove_prm392.R;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingticketmove_prm392.adapters.CinemaAdapter;
import com.example.bookingticketmove_prm392.database.dao.CinemaDAO;
import com.example.bookingticketmove_prm392.models.Cinema;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CinemaSelectionActivity extends AppCompatActivity implements CinemaAdapter.OnCinemaClickListener {
    private static final String TAG = "CinemaSelectionActivity";
    
    // UI Components
    private Toolbar toolbar;
    private TextView movieTitleText;
    private TextView moviePriceText;
    private RecyclerView cinemasRecyclerView;
    private CinemaAdapter cinemaAdapter;
    
    // Data
    private int movieId;
    private String movieTitle;
    private double moviePrice;
    private List<Cinema> cinemaList;
      @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cinema_selection);
        
        // Get data from intent
        movieId = getIntent().getIntExtra("movie_id", -1);
        movieTitle = getIntent().getStringExtra("movie_title");
        moviePrice = getIntent().getDoubleExtra("movie_price", 0.0);
        
        if (movieId == -1 || movieTitle == null) {
            Toast.makeText(this, "Invalid movie data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        setupToolbar();
        setupRecyclerView();
        displayMovieInfo();
        loadCinemas();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        movieTitleText = findViewById(R.id.movie_title_text);
        moviePriceText = findViewById(R.id.movie_price_text);
        cinemasRecyclerView = findViewById(R.id.cinemas_recycler_view);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Select Cinema");
        }
    }
    
    private void setupRecyclerView() {
        cinemaList = new ArrayList<>();
        cinemaAdapter = new CinemaAdapter(this, cinemaList);
        cinemaAdapter.setOnCinemaClickListener(this);
        
        cinemasRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cinemasRecyclerView.setAdapter(cinemaAdapter);
    }
    
    private void displayMovieInfo() {
        movieTitleText.setText(movieTitle);
        moviePriceText.setText(String.format("$%.2f", moviePrice));
    }
    
    private void loadCinemas() {
        new LoadCinemasTask().execute();
    }
    
    @Override
    public void onCinemaClick(Cinema cinema) {
        // Navigate to showtime selection
        Intent intent = new Intent(this, ShowtimeSelectionActivity.class);
        intent.putExtra("movie_id", movieId);
        intent.putExtra("movie_title", movieTitle);
        intent.putExtra("movie_price", moviePrice);
        intent.putExtra("cinema_id", cinema.getCinemaId());
        intent.putExtra("cinema_name", cinema.getName());
        startActivity(intent);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
      private class LoadCinemasTask extends AsyncTask<Void, Void, List<Cinema>> {
        @Override
        protected List<Cinema> doInBackground(Void... voids) {
            try {
                CinemaDAO cinemaDAO = new CinemaDAO();
                return cinemaDAO.getAllCinemas();            } catch (SQLException e) {
                Log.e(TAG, "Error loading cinemas", e);
                return new ArrayList<>();
            }
        }
        
        @Override
        protected void onPostExecute(List<Cinema> cinemas) {
            if (cinemas != null && !cinemas.isEmpty()) {
                cinemaList.clear();
                cinemaList.addAll(cinemas);
                cinemaAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(CinemaSelectionActivity.this, "No cinemas available", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
