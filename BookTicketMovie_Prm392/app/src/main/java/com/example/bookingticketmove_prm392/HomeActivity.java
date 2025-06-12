package com.example.bookingticketmove_prm392;

import android.content.Intent;
import android.content.SharedPreferences;
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

import com.example.bookingticketmove_prm392.database.dao.UserDAO;
import com.example.bookingticketmove_prm392.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.math.BigDecimal;

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
        
        // Set up click listeners
        setupClickListeners();
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
}
