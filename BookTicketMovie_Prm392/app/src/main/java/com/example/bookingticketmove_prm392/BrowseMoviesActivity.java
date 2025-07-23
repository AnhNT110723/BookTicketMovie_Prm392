package com.example.bookingticketmove_prm392;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingticketmove_prm392.adapters.MovieAdapter;
import com.example.bookingticketmove_prm392.database.dao.MovieDAO;
import com.example.bookingticketmove_prm392.models.Movie;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BrowseMoviesActivity extends AppCompatActivity implements MovieAdapter.OnMovieClickListener {
    private static final String TAG = "BrowseMoviesActivity";
    
    // Intent extras
    public static final String EXTRA_CATEGORY = "category";
    public static final String CATEGORY_ALL = "all";
    public static final String CATEGORY_TRENDING = "trending";
    public static final String CATEGORY_FEATURED = "featured";
    
    // UI Components
    private Toolbar toolbar;
    private SearchView searchView;
    private ChipGroup genreChipGroup;
    private RecyclerView moviesRecyclerView;
    private TextView emptyStateText;
    private ProgressBar progressBar;
    
    // Data
    private MovieAdapter movieAdapter;
    private List<Movie> allMovies;
    private List<Movie> filteredMovies;
    private String currentCategory = CATEGORY_ALL;
    private String currentSearchQuery = "";
    private String selectedGenre = "";
    private String currentSortBy = "title"; // Default sort by title
    
    // Common genres for filtering
    private String[] genres = {"Action", "Adventure", "Animation", "Comedy", "Crime", "Drama", 
                              "Fantasy", "Horror", "Romance", "Sci-Fi", "Thriller"};
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_movies);


        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Get category from intent
        currentCategory = getIntent().getStringExtra(EXTRA_CATEGORY);
        if (currentCategory == null) {
            currentCategory = CATEGORY_ALL;
        }
        
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupGenreChips();
        setupSearchView();
        loadMovies();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        searchView = findViewById(R.id.search_view);
        genreChipGroup = findViewById(R.id.genre_chip_group);
        moviesRecyclerView = findViewById(R.id.movies_recycler_view);
        emptyStateText = findViewById(R.id.empty_state_text);
        progressBar = findViewById(R.id.progress_bar);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            
            // Set title based on category
            String title;
            switch (currentCategory) {
                case CATEGORY_TRENDING:
                    title = "Trending Movies";
                    break;
                case CATEGORY_FEATURED:
                    title = "Featured Movies";
                    break;
                default:
                    title = "Browse Movies";
                    break;
            }
            getSupportActionBar().setTitle(title);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void setupRecyclerView() {
        allMovies = new ArrayList<>();
        filteredMovies = new ArrayList<>();
        movieAdapter = new MovieAdapter(this, filteredMovies, false); // false for grid layout
        movieAdapter.setOnMovieClickListener(this);
        
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        moviesRecyclerView.setLayoutManager(layoutManager);
        moviesRecyclerView.setAdapter(movieAdapter);
    }
    
    private void setupGenreChips() {
        // Add "All" chip first
        Chip allChip = new Chip(this);
        allChip.setText("All");
        allChip.setCheckable(true);
        allChip.setChecked(true);
        allChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedGenre = "";
                clearOtherChips(allChip);
                applyFilters();
            }
        });
        genreChipGroup.addView(allChip);
        
        // Add genre chips
        for (String genre : genres) {
            Chip chip = new Chip(this);
            chip.setText(genre);
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedGenre = genre;
                    clearOtherChips(chip);
                    applyFilters();
                }
            });
            genreChipGroup.addView(chip);
        }
    }
    
    private void clearOtherChips(Chip selectedChip) {
        for (int i = 0; i < genreChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) genreChipGroup.getChildAt(i);
            if (chip != selectedChip) {
                chip.setChecked(false);
            }
        }
    }
    
    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentSearchQuery = query.trim();
                applyFilters();
                return true;
            }
            
            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText.trim();
                applyFilters();
                return true;
            }
        });
    }
    
    private void loadMovies() {
        showLoading(true);
        new LoadMoviesTask().execute();
    }
      private void applyFilters() {
        filteredMovies.clear();
        
        for (Movie movie : allMovies) {
            boolean matchesSearch = currentSearchQuery.isEmpty() || 
                                  movie.getTitle().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                                  movie.getDirector().toLowerCase().contains(currentSearchQuery.toLowerCase());
            
            boolean matchesGenre = selectedGenre.isEmpty() || 
                                 movie.getGenre().toLowerCase().contains(selectedGenre.toLowerCase());
            
            if (matchesSearch && matchesGenre) {
                filteredMovies.add(movie);
            }
        }
        
        // Apply sorting
        applySorting();
        
        movieAdapter.notifyDataSetChanged();
        updateEmptyState();
    }
    
    private void applySorting() {
        switch (currentSortBy) {
            case "title":
                Collections.sort(filteredMovies, (m1, m2) -> m1.getTitle().compareToIgnoreCase(m2.getTitle()));
                break;
            case "rating":
                Collections.sort(filteredMovies, (m1, m2) -> Double.compare(m2.getRating(), m1.getRating()));
                break;
            case "release_date":
                Collections.sort(filteredMovies, (m1, m2) -> {
                    if (m1.getReleaseDate() == null && m2.getReleaseDate() == null) return 0;
                    if (m1.getReleaseDate() == null) return 1;
                    if (m2.getReleaseDate() == null) return -1;
                    return m2.getReleaseDate().compareTo(m1.getReleaseDate());
                });
                break;
            case "price":
                Collections.sort(filteredMovies, (m1, m2) -> Double.compare(m1.getPrice(), m2.getPrice()));
                break;
        }
    }
    
    private void updateEmptyState() {
        if (filteredMovies.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            moviesRecyclerView.setVisibility(View.GONE);
            
            if (!currentSearchQuery.isEmpty()) {
                emptyStateText.setText("No movies found for \"" + currentSearchQuery + "\"");
            } else if (!selectedGenre.isEmpty()) {
                emptyStateText.setText("No " + selectedGenre + " movies found");
            } else {
                emptyStateText.setText("No movies available");
            }
        } else {
            emptyStateText.setVisibility(View.GONE);
            moviesRecyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        moviesRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);
    }
      @Override
    public void onMovieClick(Movie movie) {
        Intent intent = new Intent(this, MovieDetailActivity.class);
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_ID, movie.getMovieId());
        startActivity(intent);
    }
    
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.browse_movies_menu, menu);
//        return true;
//    }
    
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//
//        if (id == android.R.id.home) {
//            onBackPressed();
//            return true;
//        } else if (id == R.id.action_search) {
//            // Focus on search view
//            if (searchView != null) {
//                searchView.setIconified(false);
//                searchView.requestFocus();
//            }
//            return true;
//        } else if (id == R.id.action_filter) {
//            // Scroll to filter section
//            if (genreChipGroup != null) {
//                genreChipGroup.getParent().requestChildFocus(genreChipGroup, genreChipGroup);
//            }
//            return true;
//        } else if (id == R.id.sort_by_title) {
//            currentSortBy = "title";
//            applyFilters();
//            Toast.makeText(this, "Sorted by Title", Toast.LENGTH_SHORT).show();
//            return true;
//        } else if (id == R.id.sort_by_rating) {
//            currentSortBy = "rating";
//            applyFilters();
//            Toast.makeText(this, "Sorted by Rating", Toast.LENGTH_SHORT).show();
//            return true;
//        } else if (id == R.id.sort_by_release_date) {
//            currentSortBy = "release_date";
//            applyFilters();
//            Toast.makeText(this, "Sorted by Release Date", Toast.LENGTH_SHORT).show();
//            return true;
//        } else if (id == R.id.sort_by_price) {
//            currentSortBy = "price";
//            applyFilters();
//            Toast.makeText(this, "Sorted by Price", Toast.LENGTH_SHORT).show();
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
    // AsyncTask to load movies based on category
    private class LoadMoviesTask extends AsyncTask<Void, Void, List<Movie>> {        @Override
        protected List<Movie> doInBackground(Void... voids) {
            try {
                MovieDAO movieDAO = new MovieDAO();
                
                switch (currentCategory) {
                    case CATEGORY_TRENDING:
                        return movieDAO.getAllTrendingMovies();
                    case CATEGORY_FEATURED:
                        return movieDAO.getAllFeaturedMovies();
                    default:
                        return movieDAO.getAllMovies();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading movies", e);
                return new ArrayList<>();
            }
        }
        
        @Override
        protected void onPostExecute(List<Movie> movies) {
            super.onPostExecute(movies);
            showLoading(false);
            
            allMovies.clear();
            allMovies.addAll(movies);
            
            // Apply filters (this will also update the adapter)
            applyFilters();
            
            Log.d(TAG, "Loaded " + movies.size() + " movies for category: " + currentCategory);
        }
    }
}
