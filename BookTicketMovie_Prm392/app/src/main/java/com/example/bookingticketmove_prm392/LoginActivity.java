package com.example.bookingticketmove_prm392;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bookingticketmove_prm392.database.dao.UserDAO;
import com.example.bookingticketmove_prm392.models.User;
import com.example.bookingticketmove_prm392.utils.PasswordUtils;
import com.example.bookingticketmove_prm392.utils.ValidationUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    // UI Components
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private Button loginButton;
    private ProgressBar loadingIndicator;
    private TextView registerLink;
    private TextView forgotPasswordLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Initialize views
        initViews();

        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up click listeners
        setupClickListeners();
    }

    private void initViews() {
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        emailInputLayout = findViewById(R.id.email_input_layout);
        passwordInputLayout = findViewById(R.id.password_input_layout);
        loginButton = findViewById(R.id.login_button);
        loadingIndicator = findViewById(R.id.loading_indicator);
        registerLink = findViewById(R.id.register_link);
        forgotPasswordLink = findViewById(R.id.forgot_password);
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> attemptLogin());
        
        registerLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
        
        forgotPasswordLink.setOnClickListener(v -> {
            Toast.makeText(this, "Forgot password feature coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void attemptLogin() {
        // Clear previous errors
        emailInputLayout.setError(null);
        passwordInputLayout.setError(null);

        // Get input values
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString() : "";

        // Validate inputs
        boolean isValid = true;

        if (TextUtils.isEmpty(email)) {
            emailInputLayout.setError(getString(R.string.error_empty_email));
            isValid = false;
        } else if (!ValidationUtils.isValidEmail(email)) {
            emailInputLayout.setError(getString(R.string.error_invalid_email));
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInputLayout.setError(getString(R.string.error_empty_password));
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Show loading state
        setLoadingState(true);

        // Hash the password for comparison
        String passwordHash = PasswordUtils.simpleHash(password);

        // Attempt login
        UserDAO.LoginUserTask loginTask = new UserDAO.LoginUserTask(email, passwordHash,
            new UserDAO.DatabaseTaskListener<User>() {
                @Override
                public void onSuccess(User user) {
                    setLoadingState(false);
                    if (user != null) {
                        onLoginSuccess(user);
                    } else {
                        onLoginFailed(getString(R.string.error_login_failed));
                    }
                }

                @Override
                public void onError(Exception error) {
                    setLoadingState(false);
                    Log.e(TAG, "Login error: " + error.getMessage(), error);
                    onLoginFailed("Login failed: " + error.getMessage());
                }
            });

        loginTask.execute();
    }

    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            loginButton.setText("");
            loadingIndicator.setVisibility(View.VISIBLE);
            loginButton.setEnabled(false);
            emailInput.setEnabled(false);
            passwordInput.setEnabled(false);
        } else {
            loginButton.setText(R.string.sign_in);
            loadingIndicator.setVisibility(View.GONE);
            loginButton.setEnabled(true);
            emailInput.setEnabled(true);
            passwordInput.setEnabled(true);
        }
    }    private void onLoginSuccess(User user) {
        Toast.makeText(this, getString(R.string.login_successful), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Login successful for user: " + user.getEmail() + " with role: " + user.getRoleID());
        
        // Save user session data to SharedPreferences
        getSharedPreferences("UserSession", MODE_PRIVATE)
            .edit()
            .putString("userEmail", user.getEmail())
            .putString("userName", user.getName())
            .putInt("userId", user.getUserID())
            .putInt("userRole", user.getRoleID())
            .putFloat("loyaltyPoints", user.getLoyaltyPoints() != null ? 
                user.getLoyaltyPoints().floatValue() : 0.0f)
            .putBoolean("isLoggedIn", true)
            .apply();
          // Navigate based on user role
        Intent intent;
        if (user.getRoleID() == 1) { // Admin role
            // Show dialog to choose between admin panel and home
            showAdminNavigationDialog(user);
            return; // Don't navigate immediately, let dialog handle it
        } else { // Customer or FrontDeskOfficer
            intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }    private void onLoginFailed(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        
        // Clear password field for security
        passwordInput.setText("");
    }
    
    private void showAdminNavigationDialog(User user) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Welcome Admin!")
                .setMessage("Where would you like to go?")
                .setPositiveButton("Admin Panel", (dialog, which) -> {
                    Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Home Screen", (dialog, which) -> {
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }
}
