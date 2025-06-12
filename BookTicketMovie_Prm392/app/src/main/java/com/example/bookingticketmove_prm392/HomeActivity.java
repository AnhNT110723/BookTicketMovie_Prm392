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

import com.example.bookingticketmove_prm392.adapters.MovieAdapter;
import com.example.bookingticketmove_prm392.database.dao.MovieDAO;
import com.example.bookingticketmove_prm392.database.dao.UserDAO;
import com.example.bookingticketmove_prm392.models.Movie;
import com.example.bookingticketmove_prm392.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    
    // UI Components
    private Toolbar toolbar;
    private TextView welcomeText;
    private TextView userNameText;
    private TextView loyaltyPointsText;
    private CardView browseMoviesCard;
    private CardView myBookingsCard;
    private CardView cinemasCard;
    private CardView profileCard;
    private FloatingActionButton fabQuickBooking;
    
    // Movie RecyclerViews
    private RecyclerView trendingMoviesRecycler;
    private RecyclerView featuredMoviesRecycler;
    private TextView seeAllTrending;
    private TextView seeAllFeatured;
    
    // Adapters
    private MovieAdapter trendingMoviesAdapter;
    private MovieAdapter featuredMoviesAdapter;
    
    // User data
    private User currentUser;
    private SharedPreferences sharedPreferences;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        
        // Initialize shared preferences
        sharedPreferences = getSharedPreferences("MovieBookingApp", MODE_PRIVATE);
        
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
        
        // Load movies
        loadMovies();
        
        // Set up click listeners
        setupClickListeners();
        
        // Initialize RecyclerViews
        initRecyclerViews();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        welcomeText = findViewById(R.id.welcome_text);
        userNameText = findViewById(R.id.user_name_text);
        loyaltyPointsText = findViewById(R.id.loyalty_points_text);
        browseMoviesCard = findViewById(R.id.browse_movies_card);
        myBookingsCard = findViewById(R.id.my_bookings_card);
        cinemasCard = findViewById(R.id.cinemas_card);
        profileCard = findViewById(R.id.profile_card);
        fabQuickBooking = findViewById(R.id.fab_quick_booking);
        
        // Initialize RecyclerViews
        trendingMoviesRecycler = findViewById(R.id.trending_movies_recycler);
        featuredMoviesRecycler = findViewById(R.id.featured_movies_recycler);
        seeAllTrending = findViewById(R.id.see_all_trending);
        seeAllFeatured = findViewById(R.id.see_all_featured);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }
    
    private void loadUserData() {
        // Get user email from shared preferences (saved during login)
        String userEmail = sharedPreferences.getString("user_email", "");
        
        if (!userEmail.isEmpty()) {
            // Load user data from database
            UserDAO.GetUserByEmailTask getUserTask = new UserDAO.GetUserByEmailTask(userEmail,
                new UserDAO.DatabaseTaskListener<User>() {
                    @Override
                    public void onSuccess(User user) {
                        if (user != null) {
                            currentUser = user;
                            updateUI();
                        } else {
                            Log.e(TAG, "User not found in database");
                            redirectToLogin();
                        }
                    }
                    
                    @Override
                    public void onError(Exception error) {
                        Log.e(TAG, "Error loading user data: " + error.getMessage(), error);
                        Toast.makeText(HomeActivity.this, "Error loading user data", Toast.LENGTH_SHORT).show();
                        // Use cached data if available
                        loadCachedUserData();
                    }
                });
            
            getUserTask.execute();
        } else {
            Log.e(TAG, "No user email found in preferences");
            redirectToLogin();
        }
    }
    
    private void loadCachedUserData() {
        String userName = sharedPreferences.getString("user_name", "User");
        String userEmail = sharedPreferences.getString("user_email", "");
        float loyaltyPoints = sharedPreferences.getFloat("loyalty_points", 0.0f);
        
        // Create a basic user object with cached data
        currentUser = new User();
        currentUser.setName(userName);
        currentUser.setEmail(userEmail);
        currentUser.setLoyaltyPoints(BigDecimal.valueOf(loyaltyPoints));
        
        updateUI();
    }
    
    private void updateUI() {
        if (currentUser != null) {
            // Update welcome message based on time of day
            updateWelcomeMessage();
            
            // Update user name
            userNameText.setText(currentUser.getName());
            
            // Update loyalty points
            BigDecimal points = currentUser.getLoyaltyPoints();
            if (points != null) {
                loyaltyPointsText.setText(String.format("%.0f points", points.doubleValue()));
            } else {
                loyaltyPointsText.setText("0 points");
            }
            
            // Cache user data
            cacheUserData();
        }
    }
    
    private void updateWelcomeMessage() {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        String greeting;
        
        if (hour < 12) {
            greeting = "Good morning!";
        } else if (hour < 17) {
            greeting = "Good afternoon!";
        } else {
            greeting = "Good evening!";
        }
        
        welcomeText.setText(greeting);
    }
    
    private void cacheUserData() {
        if (currentUser != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("user_name", currentUser.getName());
            editor.putString("user_email", currentUser.getEmail());
            editor.putFloat("loyalty_points", currentUser.getLoyaltyPoints() != null ? 
                currentUser.getLoyaltyPoints().floatValue() : 0.0f);
            editor.apply();
        }
    }
    
    private void setupClickListeners() {
        browseMoviesCard.setOnClickListener(v -> {
            Toast.makeText(this, "Browse Movies - Coming Soon!", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to MoviesActivity
            // Intent intent = new Intent(this, MoviesActivity.class);
            // startActivity(intent);
        });

        myBookingsCard.setOnClickListener(v -> {
            Toast.makeText(this, "My Bookings - Coming Soon!", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to BookingsActivity
            // Intent intent = new Intent(this, BookingsActivity.class);
            // startActivity(intent);
        });

        cinemasCard.setOnClickListener(v -> {
            Toast.makeText(this, "Cinemas - Coming Soon!", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to CinemasActivity
            // Intent intent = new Intent(this, CinemasActivity.class);
            // startActivity(intent);
        });

        profileCard.setOnClickListener(v -> {
            Toast.makeText(this, "Profile - Coming Soon!", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to ProfileActivity
            // Intent intent = new Intent(this, ProfileActivity.class);
            // startActivity(intent);
        });

        fabQuickBooking.setOnClickListener(v -> {
            Toast.makeText(this, "Quick Booking - Coming Soon!", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to QuickBookingActivity
            // Intent intent = new Intent(this, QuickBookingActivity.class);
            // startActivity(intent);
        });
        
        seeAllTrending.setOnClickListener(v -> {
            Toast.makeText(this, "See All Trending Movies - Coming Soon!", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to MoviesActivity with trending filter
        });
        
        seeAllFeatured.setOnClickListener(v -> {
            Toast.makeText(this, "See All Featured Movies - Coming Soon!", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to MoviesActivity with featured filter
        });
    }    private void initRecyclerViews() {
        // Set up Trending Movies RecyclerView
        trendingMoviesAdapter = new MovieAdapter(this, new ArrayList<>(), true); // true for horizontal
        trendingMoviesAdapter.setOnMovieClickListener(this::onMovieClick);
        trendingMoviesRecycler.setAdapter(trendingMoviesAdapter);
        trendingMoviesRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        
        // Set up Featured Movies RecyclerView
        featuredMoviesAdapter = new MovieAdapter(this, new ArrayList<>(), false); // false for grid
        featuredMoviesAdapter.setOnMovieClickListener(this::onMovieClick);
        featuredMoviesRecycler.setAdapter(featuredMoviesAdapter);
        featuredMoviesRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }
    
    private void loadMovies() {
        // Load trending movies
        new LoadTrendingMoviesTask().execute();
        
        // Load featured movies
        new LoadFeaturedMoviesTask().execute();
    }
      private void onMovieClick(Movie movie) {
        // TODO: Navigate to MovieDetailActivity
        Toast.makeText(this, "Selected: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
        // Intent intent = new Intent(this, MovieDetailActivity.class);
        // intent.putExtra("movie_id", movie.getMovieId());
        // startActivity(intent);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_logout) {
            logout();
            return true;
        } else if (id == R.id.action_refresh) {
            loadUserData();
            Toast.makeText(this, "Refreshed", Toast.LENGTH_SHORT).show();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void logout() {
        // Clear user data from shared preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        
        // Navigate back to login
        redirectToLogin();
        
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
    
    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user data when returning to home screen
        if (currentUser != null) {
            updateWelcomeMessage();
        }
    }
    
    @Override
    public void onBackPressed() {
        // Override back button to prevent going back to login
        // Show exit confirmation or minimize app
        moveTaskToBack(true);
    }
    
    // AsyncTask to load trending movies
    private class LoadTrendingMoviesTask extends AsyncTask<Void, Void, List<Movie>> {
        @Override
        protected List<Movie> doInBackground(Void... voids) {
            try {
                MovieDAO movieDAO = new MovieDAO();
                return movieDAO.getTrendingMovies();
            } catch (Exception e) {
                Log.e(TAG, "Error loading trending movies", e);
                return createSampleTrendingMovies();
            }
        }
        
        @Override
        protected void onPostExecute(List<Movie> movies) {
            super.onPostExecute(movies);
            if (trendingMoviesAdapter != null) {
                trendingMoviesAdapter.updateMovies(movies);
            }
        }
    }
    
    // AsyncTask to load featured movies
    private class LoadFeaturedMoviesTask extends AsyncTask<Void, Void, List<Movie>> {
        @Override
        protected List<Movie> doInBackground(Void... voids) {
            try {
                MovieDAO movieDAO = new MovieDAO();
                return movieDAO.getFeaturedMovies();
            } catch (Exception e) {
                Log.e(TAG, "Error loading featured movies", e);
                return createSampleFeaturedMovies();
            }
        }
        
        @Override
        protected void onPostExecute(List<Movie> movies) {
            super.onPostExecute(movies);
            if (featuredMoviesAdapter != null) {
                featuredMoviesAdapter.updateMovies(movies);
            }
        }
    }
      // Create sample trending movies for testing
    private List<Movie> createSampleTrendingMovies() {
        List<Movie> movies = new ArrayList<>();
        
        Movie movie1 = new Movie("Avatar: The Way of Water", 
            "Set more than a decade after the events of the first film...", 
            "Action, Adventure, Sci-Fi", 192, "James Cameron", new java.util.Date());
        movie1.setMovieId(1);
        movie1.setRating(7.6);
        movie1.setTrending(true);
        movie1.setPosterUrl("");
        movies.add(movie1);
            
        Movie movie2 = new Movie("Top Gun: Maverick", 
            "After thirty years, Maverick is still pushing the envelope...", 
            "Action, Drama", 130, "Joseph Kosinski", new java.util.Date());
        movie2.setMovieId(2);
        movie2.setRating(8.3);
        movie2.setTrending(true);
        movie2.setPosterUrl("");
        movies.add(movie2);
            
        Movie movie3 = new Movie("Black Panther: Wakanda Forever", 
            "Queen Ramonda, Shuri, M'Baku, Okoye and the Dora Milaje...", 
            "Action, Adventure, Drama", 161, "Ryan Coogler", new java.util.Date());
        movie3.setMovieId(3);
        movie3.setRating(6.7);
        movie3.setTrending(true);
        movie3.setPosterUrl("");
        movies.add(movie3);
            
        Movie movie4 = new Movie("Spider-Man: No Way Home", 
            "With Spider-Man's identity now revealed, Peter asks Doctor Strange for help...", 
            "Action, Adventure, Sci-Fi", 148, "Jon Watts", new java.util.Date());
        movie4.setMovieId(4);
        movie4.setRating(8.4);
        movie4.setTrending(true);
        movie4.setPosterUrl("");
        movies.add(movie4);
            
        return movies;
    }
      // Create sample featured movies for testing
    private List<Movie> createSampleFeaturedMovies() {
        List<Movie> movies = new ArrayList<>();
        
        Movie movie1 = new Movie("The Batman", 
            "When a sadistic serial killer begins murdering key political figures in Gotham...", 
            "Action, Crime, Drama", 176, "Matt Reeves", new java.util.Date());
        movie1.setMovieId(5);
        movie1.setRating(7.8);
        movie1.setTrending(false);
        movie1.setPosterUrl("");
        movies.add(movie1);
            
        Movie movie2 = new Movie("Doctor Strange in the Multiverse of Madness", 
            "Doctor Strange teams up with a mysterious teenage girl...", 
            "Action, Adventure, Fantasy", 126, "Sam Raimi", new java.util.Date());
        movie2.setMovieId(6);
        movie2.setRating(6.9);
        movie2.setTrending(false);
        movie2.setPosterUrl("");
        movies.add(movie2);
            
        Movie movie3 = new Movie("Minions: The Rise of Gru", 
            "A fanboy of a supervillain supergroup known as the Vicious 6...", 
            "Animation, Adventure, Comedy", 87, "Kyle Balda", new java.util.Date());
        movie3.setMovieId(7);
        movie3.setRating(6.5);
        movie3.setTrending(false);
        movie3.setPosterUrl("");
        movies.add(movie3);
            
        Movie movie4 = new Movie("Thor: Love and Thunder", 
            "Thor enlists the help of Valkyrie, Korg and ex-girlfriend Jane Foster...", 
            "Action, Adventure, Comedy", 119, "Taika Waititi", new java.util.Date());
        movie4.setMovieId(8);
        movie4.setRating(6.2);
        movie4.setTrending(false);
        movie4.setPosterUrl("");
        movies.add(movie4);
            
        return movies;
    }
}
