package com.example.bookingticketmove_prm392;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.example.bookingticketmove_prm392.database.dao.UserDAO;
import com.example.bookingticketmove_prm392.models.User;
import com.example.bookingticketmove_prm392.utils.PasswordUtils;
import com.example.bookingticketmove_prm392.utils.ValidationUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    // UI Components
    private Toolbar toolbar;
    private TextView nameText;
    private TextView emailText;
    private TextView phoneText;
    private TextView loyaltyPointsText;
    private TextView roleText;
    private TextView memberSinceText;
    private CardView profileInfoCard;
    private CardView accountStatsCard;
    private Button editProfileButton;
    private Button changePasswordButton;    private Button bookingHistoryButton;
    private Button favoriteMoviesButton;
    private ProgressBar loadingIndicator;
    private NestedScrollView contentLayout;
    private Button contactUs;

    // User data
    private User currentUser;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);        // Initialize shared preferences
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);

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
        nameText = findViewById(R.id.name_text);
        emailText = findViewById(R.id.email_text);
        phoneText = findViewById(R.id.phone_text);
        loyaltyPointsText = findViewById(R.id.loyalty_points_text);
        roleText = findViewById(R.id.role_text);
        memberSinceText = findViewById(R.id.member_since_text);
        profileInfoCard = findViewById(R.id.profile_info_card);
        accountStatsCard = findViewById(R.id.account_stats_card);
        editProfileButton = findViewById(R.id.edit_profile_button);
        changePasswordButton = findViewById(R.id.change_password_button);
        bookingHistoryButton = findViewById(R.id.booking_history_button);
        favoriteMoviesButton = findViewById(R.id.favorite_movies_button);
        loadingIndicator = findViewById(R.id.loading_indicator);
        contentLayout = findViewById(R.id.content_layout);
        contactUs = findViewById(R.id.contact_us_button);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("👤 My Profile");
        }
    }    private void loadUserData() {
        setLoadingState(true);

        // Get user email from shared preferences
        String userEmail = sharedPreferences.getString("userEmail", "");

        if (!userEmail.isEmpty()) {
            // Load user data from database
            UserDAO.GetUserByEmailTask getUserTask = new UserDAO.GetUserByEmailTask(userEmail,
                new UserDAO.DatabaseTaskListener<User>() {
                    @Override
                    public void onSuccess(User user) {
                        setLoadingState(false);
                        if (user != null) {
                            currentUser = user;
                            updateUI();
                        } else {
                            showError("User not found");
                            finish();
                        }
                    }

                    @Override
                    public void onError(Exception error) {
                        setLoadingState(false);
                        Log.e(TAG, "Error loading user data: " + error.getMessage(), error);
                        loadCachedUserData();
                    }
                });

            getUserTask.execute();
        } else {
            setLoadingState(false);
            showError("No user data found");
            finish();
        }
    }    private void loadCachedUserData() {
        String userName = sharedPreferences.getString("userName", "User");
        String userEmail = sharedPreferences.getString("userEmail", "");
        float loyaltyPoints = sharedPreferences.getFloat("loyaltyPoints", 0.0f);
        int userRole = sharedPreferences.getInt("userRole", 2);

        // Create a basic user object with cached data
        currentUser = new User();
        currentUser.setName(userName);
        currentUser.setEmail(userEmail);
        currentUser.setLoyaltyPoints(BigDecimal.valueOf(loyaltyPoints));
        currentUser.setRoleID(userRole);

        updateUI();
    }

    private void updateUI() {
        if (currentUser != null) {
            nameText.setText(currentUser.getName());
            emailText.setText(currentUser.getEmail());
            
            // Phone might be null for cached data
            String phone = currentUser.getPhone();
            phoneText.setText(!TextUtils.isEmpty(phone) ? phone : "Not provided");

            // Loyalty points
            BigDecimal points = currentUser.getLoyaltyPoints();
            if (points != null) {
                loyaltyPointsText.setText(String.format("%.0f points", points.doubleValue()));
            } else {
                loyaltyPointsText.setText("0 points");
            }

            // Role
            String roleText = getRoleDisplayName(currentUser.getRoleID());
            this.roleText.setText(roleText);

            // Member since (might be null for cached data)
            if (currentUser.getRegistrationDate() != null) {
                memberSinceText.setText("Member since " + 
                    android.text.format.DateFormat.format("MMM yyyy", currentUser.getRegistrationDate()));
            } else {
                memberSinceText.setText("Member");
            }
        }
    }

    private String getRoleDisplayName(int roleId) {
        switch (roleId) {
            case 1:
                return "Admin";
            case 2:
                return "Customer";
            case 3:
                return "Front Desk Officer";
            default:
                return "User";
        }
    }

    private void setupClickListeners() {
        editProfileButton.setOnClickListener(v -> showEditProfileDialog());
        
        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());
        
        bookingHistoryButton.setOnClickListener(v -> {
            Toast.makeText(this, "Booking History - Coming Soon!", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to BookingHistoryActivity
            // Intent intent = new Intent(this, BookingHistoryActivity.class);
            // startActivity(intent);
        });
        
        favoriteMoviesButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MovieFavoriteActivity.class);
            startActivity(intent);
        });

        contactUs.setOnClickListener(v -> {
            Intent intent = new Intent(this, ContactUsActivity.class);
            startActivity(intent);
        });

    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        
        TextInputEditText nameInput = dialogView.findViewById(R.id.name_input);
        TextInputEditText phoneInput = dialogView.findViewById(R.id.phone_input);
        TextInputLayout nameInputLayout = dialogView.findViewById(R.id.name_input_layout);
        TextInputLayout phoneInputLayout = dialogView.findViewById(R.id.phone_input_layout);
        
        // Pre-fill current data
        nameInput.setText(currentUser.getName());
        phoneInput.setText(currentUser.getPhone());
        
        builder.setView(dialogView)
                .setTitle("Edit Profile")
                .setPositiveButton("Update", null) // Set to null initially to override onClick
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        
        AlertDialog dialog = builder.create();
        
        // Override positive button click to validate input
        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String newName = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
                String newPhone = phoneInput.getText() != null ? phoneInput.getText().toString().trim() : "";
                
                // Clear previous errors
                nameInputLayout.setError(null);
                phoneInputLayout.setError(null);
                
                boolean isValid = true;
                
                if (TextUtils.isEmpty(newName)) {
                    nameInputLayout.setError("Name is required");
                    isValid = false;
                }
                
                if (!TextUtils.isEmpty(newPhone) && !ValidationUtils.isValidPhone(newPhone)) {
                    phoneInputLayout.setError("Invalid phone number");
                    isValid = false;
                }
                
                if (isValid) {
                    updateProfile(newName, newPhone);
                    dialog.dismiss();
                }
            });
        });
        
        dialog.show();
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        
        TextInputEditText currentPasswordInput = dialogView.findViewById(R.id.current_password_input);
        TextInputEditText newPasswordInput = dialogView.findViewById(R.id.new_password_input);
        TextInputEditText confirmPasswordInput = dialogView.findViewById(R.id.confirm_password_input);
        TextInputLayout currentPasswordLayout = dialogView.findViewById(R.id.current_password_layout);
        TextInputLayout newPasswordLayout = dialogView.findViewById(R.id.new_password_layout);
        TextInputLayout confirmPasswordLayout = dialogView.findViewById(R.id.confirm_password_layout);
        
        builder.setView(dialogView)
                .setTitle("Change Password")
                .setPositiveButton("Update", null)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        
        AlertDialog dialog = builder.create();
        
        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String currentPassword = currentPasswordInput.getText() != null ? 
                    currentPasswordInput.getText().toString() : "";
                String newPassword = newPasswordInput.getText() != null ? 
                    newPasswordInput.getText().toString() : "";
                String confirmPassword = confirmPasswordInput.getText() != null ? 
                    confirmPasswordInput.getText().toString() : "";
                
                // Clear previous errors
                currentPasswordLayout.setError(null);
                newPasswordLayout.setError(null);
                confirmPasswordLayout.setError(null);
                
                boolean isValid = true;
                
                if (TextUtils.isEmpty(currentPassword)) {
                    currentPasswordLayout.setError("Current password is required");
                    isValid = false;
                }
                
                if (TextUtils.isEmpty(newPassword)) {
                    newPasswordLayout.setError("New password is required");
                    isValid = false;
                } else if (!ValidationUtils.isValidPassword(newPassword)) {
                    newPasswordLayout.setError("Password must be at least 6 characters");
                    isValid = false;
                }
                
                if (!newPassword.equals(confirmPassword)) {
                    confirmPasswordLayout.setError("Passwords do not match");
                    isValid = false;
                }
                
                if (isValid) {
                    changePassword(currentPassword, newPassword);
                    dialog.dismiss();
                }
            });
        });
        
        dialog.show();
    }

    private void updateProfile(String newName, String newPhone) {
        setLoadingState(true);
        
        // Update user object
        currentUser.setName(newName);
        currentUser.setPhone(newPhone);
        
        // Update in database
        UserDAO.UpdateUserTask updateTask = new UserDAO.UpdateUserTask(currentUser,
            new UserDAO.DatabaseTaskListener<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    setLoadingState(false);
                    if (result) {
                        updateUI();
                        updateSharedPreferences();
                        showSuccess("Profile updated successfully!");
                    } else {
                        showError("Failed to update profile");
                    }
                }
                
                @Override
                public void onError(Exception error) {
                    setLoadingState(false);
                    Log.e(TAG, "Error updating profile: " + error.getMessage(), error);
                    showError("Error updating profile: " + error.getMessage());
                }
            });
        
        updateTask.execute();
    }

    private void changePassword(String currentPassword, String newPassword) {
        setLoadingState(true);
        
        // Verify current password first
        String currentPasswordHash = PasswordUtils.simpleHash(currentPassword);
        
        UserDAO.LoginUserTask verifyTask = new UserDAO.LoginUserTask(currentUser.getEmail(), currentPasswordHash,
            new UserDAO.DatabaseTaskListener<User>() {
                @Override
                public void onSuccess(User user) {
                    if (user != null) {
                        // Current password is correct, update with new password
                        String newPasswordHash = PasswordUtils.simpleHash(newPassword);
                        currentUser.setPasswordHash(newPasswordHash);
                        
                        UserDAO.UpdateUserTask updateTask = new UserDAO.UpdateUserTask(currentUser,
                            new UserDAO.DatabaseTaskListener<Boolean>() {
                                @Override
                                public void onSuccess(Boolean result) {
                                    setLoadingState(false);
                                    if (result) {
                                        showSuccess("Password changed successfully!");
                                    } else {
                                        showError("Failed to change password");
                                    }
                                }
                                
                                @Override
                                public void onError(Exception error) {
                                    setLoadingState(false);
                                    Log.e(TAG, "Error changing password: " + error.getMessage(), error);
                                    showError("Error changing password: " + error.getMessage());
                                }
                            });
                        
                        updateTask.execute();
                    } else {
                        setLoadingState(false);
                        showError("Current password is incorrect");
                    }
                }
                
                @Override
                public void onError(Exception error) {
                    setLoadingState(false);
                    Log.e(TAG, "Error verifying password: " + error.getMessage(), error);
                    showError("Error verifying current password");
                }
            });
        
        verifyTask.execute();
    }

    private void updateSharedPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_name", currentUser.getName());
        if (currentUser.getLoyaltyPoints() != null) {
            editor.putFloat("loyalty_points", currentUser.getLoyaltyPoints().floatValue());
        }
        editor.apply();
    }

    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            loadingIndicator.setVisibility(View.VISIBLE);
            contentLayout.setVisibility(View.GONE);
        } else {
            loadingIndicator.setVisibility(View.GONE);
            contentLayout.setVisibility(View.VISIBLE);
        }
    }

    private void showSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        } else if (id == R.id.action_refresh) {
            loadUserData();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Clear user data from shared preferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();
                    
                    // Navigate to login
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    
                    Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
