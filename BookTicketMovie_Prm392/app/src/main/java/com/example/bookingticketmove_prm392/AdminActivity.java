package com.example.bookingticketmove_prm392;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingticketmove_prm392.adapters.AdminMovieAdapter;
import com.example.bookingticketmove_prm392.database.dao.MovieDAO;
import com.example.bookingticketmove_prm392.database.dao.UserDAO;
import com.example.bookingticketmove_prm392.models.Movie;
import com.example.bookingticketmove_prm392.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {
    private static final String TAG = "AdminActivity";
    
    // UI Components
    private Toolbar toolbar;
    private TextView welcomeAdminText;
    private TextView userNameText;
    private CardView totalMoviesCard;
    private CardView activeMoviesCard;
    private CardView cinemaCard;
    private CardView totalUsersCard;
    private RecyclerView moviesRecyclerView;
    private FloatingActionButton fabAddMovie;
    
    // Data
    private AdminMovieAdapter movieAdapter;
    private User currentUser;
    private SharedPreferences sharedPreferences;
    private List<Movie> allMovies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);
          // Initialize shared preferences
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        
        // Check admin role
        if (!isUserAdmin()) {
            redirectToLogin();
            return;
        }
        
        // Initialize views
        initViews();
        
        // Set up toolbar
        setupToolbar();
        
        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Load user data
        loadUserData();
        
        // Set up RecyclerView
        setupRecyclerView();
        
        // Load movies and statistics
        loadMoviesAndStatistics();
        
        // Set up click listeners
        setupClickListeners();
    }
      private boolean isUserAdmin() {
        int userRole = sharedPreferences.getInt("userRole", 2); // Default to Customer
        Log.d(TAG, "User role from preferences: " + userRole);
        return userRole == 1; // Admin role
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        welcomeAdminText = findViewById(R.id.welcome_admin_text);
        userNameText = findViewById(R.id.user_name_text);
        totalMoviesCard = findViewById(R.id.total_movies_card);
        activeMoviesCard = findViewById(R.id.active_movies_card);
        cinemaCard = findViewById(R.id.cinema_card);
        totalUsersCard = findViewById(R.id.total_users_card);
        moviesRecyclerView = findViewById(R.id.movies_recycler_view);
        fabAddMovie = findViewById(R.id.fab_add_movie);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle("🎬 Admin Panel");
        }
    }
      private void loadUserData() {
        String userEmail = sharedPreferences.getString("userEmail", "");
        String userName = sharedPreferences.getString("userName", "Admin");
        
        welcomeAdminText.setText("Welcome back, Admin!");
        userNameText.setText(userName);
    }
    
    private void setupRecyclerView() {
        allMovies = new ArrayList<>();
        movieAdapter = new AdminMovieAdapter(this, allMovies, this::onMovieAction);
        moviesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        moviesRecyclerView.setAdapter(movieAdapter);
    }
    
    private void loadMoviesAndStatistics() {
        new LoadAllMoviesTask().execute();
    }
    
    private void setupClickListeners() {        fabAddMovie.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditMovieActivity.class);
            startActivityForResult(intent, AddEditMovieActivity.RESULT_MOVIE_SAVED);
        });
        
        totalMoviesCard.setOnClickListener(v -> {
            Toast.makeText(this, "Total Movies: " + allMovies.size(), Toast.LENGTH_SHORT).show();
        });
        
        activeMoviesCard.setOnClickListener(v -> {
            long activeCount = allMovies.stream().mapToLong(movie -> movie.isActive() ? 1 : 0).sum();
            Toast.makeText(this, "Active Movies: " + activeCount, Toast.LENGTH_SHORT).show();
        });
        
        cinemaCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, CinemaManagementActivity.class);
            startActivity(intent);
        });
          totalUsersCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserManagementActivity.class);
            startActivity(intent);
        });
    }
    
    private void onMovieAction(Movie movie, String action) {
        switch (action) {
            case "edit":
                editMovie(movie);
                break;
            case "delete":
                deleteMovie(movie);
                break;
            case "toggle_trending":
                toggleTrending(movie);
                break;
            case "toggle_active":
                toggleActive(movie);
                break;
        }
    }
      private void editMovie(Movie movie) {
        Intent intent = new Intent(this, AddEditMovieActivity.class);
        intent.putExtra(AddEditMovieActivity.EXTRA_MOVIE_ID, movie.getMovieId());
        startActivityForResult(intent, AddEditMovieActivity.RESULT_MOVIE_UPDATED);
    }
    
    private void deleteMovie(Movie movie) {
        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Movie")
            .setMessage("Are you sure you want to delete \"" + movie.getTitle() + "\"?")
            .setPositiveButton("Delete", (dialog, which) -> {
                new DeleteMovieTask(movie).execute();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void toggleTrending(Movie movie) {
        movie.setTrending(!movie.isTrending());
        new UpdateMovieTask(movie, "trending").execute();
    }
    
    private void toggleActive(Movie movie) {
        movie.setActive(!movie.isActive());
        new UpdateMovieTask(movie, "active").execute();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_logout) {
            logout();
            return true;
        } else if (id == R.id.action_refresh) {
            loadMoviesAndStatistics();
            Toast.makeText(this, "Refreshed", Toast.LENGTH_SHORT).show();
            return true;        } else if (id == R.id.action_user_management) {
            Intent intent = new Intent(this, UserManagementActivity.class);
            startActivity(intent);
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        
        redirectToLogin();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
    
    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == AddEditMovieActivity.RESULT_MOVIE_SAVED && resultCode == RESULT_OK) {
            // Movie was added successfully, refresh the list
            loadMoviesAndStatistics();
            Toast.makeText(this, "Movie added successfully", Toast.LENGTH_SHORT).show();
        } else if (requestCode == AddEditMovieActivity.RESULT_MOVIE_UPDATED && resultCode == RESULT_OK) {
            // Movie was updated successfully, refresh the list
            loadMoviesAndStatistics();
            Toast.makeText(this, "Movie updated successfully", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onBackPressed() {
        // Show exit confirmation for admin
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Exit Admin Panel")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Exit", (dialog, which) -> super.onBackPressed())
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    // AsyncTask to load all movies
    private class LoadAllMoviesTask extends AsyncTask<Void, Void, List<Movie>> {
        @Override
        protected List<Movie> doInBackground(Void... voids) {
            try {
                MovieDAO movieDAO = new MovieDAO();
                return movieDAO.getAllMovies();
            } catch (Exception e) {
                Log.e(TAG, "Error loading all movies", e);
                return new ArrayList<>();
            }
        }
        
        @Override
        protected void onPostExecute(List<Movie> movies) {
            super.onPostExecute(movies);
            allMovies.clear();
            allMovies.addAll(movies);
            
            if (movieAdapter != null) {
                movieAdapter.notifyDataSetChanged();
            }
            
            updateStatistics();
        }
    }
    
    // AsyncTask to delete a movie
    private class DeleteMovieTask extends AsyncTask<Void, Void, Boolean> {
        private Movie movie;
        
        public DeleteMovieTask(Movie movie) {
            this.movie = movie;
        }
        
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                MovieDAO movieDAO = new MovieDAO();
                return movieDAO.deleteMovie(movie.getMovieId());
            } catch (Exception e) {
                Log.e(TAG, "Error deleting movie", e);
                return false;
            }
        }
        
        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                allMovies.remove(movie);
                movieAdapter.notifyDataSetChanged();
                updateStatistics();
                Toast.makeText(AdminActivity.this, "Movie deleted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(AdminActivity.this, "Failed to delete movie", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    // AsyncTask to update movie
    private class UpdateMovieTask extends AsyncTask<Void, Void, Boolean> {
        private Movie movie;
        private String action;
        
        public UpdateMovieTask(Movie movie, String action) {
            this.movie = movie;
            this.action = action;
        }
        
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                MovieDAO movieDAO = new MovieDAO();
                return movieDAO.updateMovie(movie);
            } catch (Exception e) {
                Log.e(TAG, "Error updating movie", e);
                return false;
            }
        }
        
        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                movieAdapter.notifyDataSetChanged();
                updateStatistics();
                Toast.makeText(AdminActivity.this, "Movie updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(AdminActivity.this, "Failed to update movie", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void updateStatistics() {
        // Update statistics cards
        long totalMovies = allMovies.size();
        long activeMovies = allMovies.stream().mapToLong(movie -> movie.isActive() ? 1 : 0).sum();
        long trendingMovies = allMovies.stream().mapToLong(movie -> movie.isTrending() ? 1 : 0).sum();
        
        // You can update TextViews in the cards here
        // For now, the data is available when user clicks on cards
    }
}
