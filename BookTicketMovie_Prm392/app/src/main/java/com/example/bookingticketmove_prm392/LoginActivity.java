package com.example.bookingticketmove_prm392;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
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
    private static final String TAG = "LoginActivity";    // UI Components
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private Button loginButton;
    private ProgressBar loadingIndicator;
    private TextView registerLink;
    private TextView forgotPasswordLink;
    private TextView appTitle;
    private TextView welcomeTitle;
    private TextView welcomeSubtitle;
    private ImageView logoImage;

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
        });        // Set up click listeners
        setupClickListeners();
        
        // Start entrance animations
        startEntranceAnimations();
    }    private void initViews() {
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        emailInputLayout = findViewById(R.id.email_input_layout);
        passwordInputLayout = findViewById(R.id.password_input_layout);
        loginButton = findViewById(R.id.login_button);
        loadingIndicator = findViewById(R.id.loading_indicator);
        registerLink = findViewById(R.id.register_link);
        forgotPasswordLink = findViewById(R.id.forgot_password);
        appTitle = findViewById(R.id.app_title);
        welcomeTitle = findViewById(R.id.welcome_title);
        welcomeSubtitle = findViewById(R.id.welcome_subtitle);
        
        // Set initial alpha for animations
        setViewsForAnimation();
    }    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> {
            // Add button press animation
            animateButtonPress(v);
            new Handler().postDelayed(this::attemptLogin, 100);
        });
        
        registerLink.setOnClickListener(v -> {
            animateButtonPress(v);
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }, 100);
        });
        
        forgotPasswordLink.setOnClickListener(v -> {
            animateButtonPress(v);
            new Handler().postDelayed(() -> {
                Toast.makeText(this, "Forgot password feature coming soon!", Toast.LENGTH_SHORT).show();
            }, 100);
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
        }        if (!isValid) {
            // Shake animation for invalid inputs
            shakeView(emailInput.getText().toString().isEmpty() ? emailInputLayout : passwordInputLayout);
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
    }    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            loginButton.setText("");
            loadingIndicator.setVisibility(View.VISIBLE);
            loginButton.setEnabled(false);
            emailInput.setEnabled(false);
            passwordInput.setEnabled(false);
            
            // Animate button to loading state
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(loginButton, "scaleX", 1f, 0.95f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(loginButton, "scaleY", 1f, 0.95f);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleX, scaleY);
            animatorSet.setDuration(150);
            animatorSet.start();
        } else {
            loginButton.setText(R.string.sign_in);
            loadingIndicator.setVisibility(View.GONE);
            loginButton.setEnabled(true);
            emailInput.setEnabled(true);
            passwordInput.setEnabled(true);
            
            // Animate button back to normal state
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(loginButton, "scaleX", 0.95f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(loginButton, "scaleY", 0.95f, 1f);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleX, scaleY);
            animatorSet.setDuration(150);
            animatorSet.start();
        }
    }    private void onLoginSuccess(User user) {
        // Success animation
        ObjectAnimator pulseX = ObjectAnimator.ofFloat(loginButton, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator pulseY = ObjectAnimator.ofFloat(loginButton, "scaleY", 1f, 1.1f, 1f);
        AnimatorSet pulseAnimator = new AnimatorSet();
        pulseAnimator.playTogether(pulseX, pulseY);
        pulseAnimator.setDuration(300);
        pulseAnimator.start();
        
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
            
        // Delayed navigation for animation
        new Handler().postDelayed(() -> {
            // Navigate based on user role
            Intent intent;
            if (user.getRoleID() == 1) { // Admin role
                showAdminNavigationDialog(user);
                return;
            } else { // Customer or FrontDeskOfficer
                intent = new Intent(LoginActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        }, 500);
    }    private void onLoginFailed(String errorMessage) {
        // Error shake animation
        shakeView(loginButton);
        
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
    
    /**
     * Animation methods for enhanced user experience
     */
    
    private void setViewsForAnimation() {
        // Set initial alpha to 0 for fade-in animation
        appTitle.setAlpha(0f);
        welcomeTitle.setAlpha(0f);
        welcomeSubtitle.setAlpha(0f);
        emailInputLayout.setAlpha(0f);
        passwordInputLayout.setAlpha(0f);
        loginButton.setAlpha(0f);
        forgotPasswordLink.setAlpha(0f);
        findViewById(R.id.register_link_layout).setAlpha(0f);
        
        // Set initial translation for slide-in animation
        appTitle.setTranslationY(-50f);
        welcomeTitle.setTranslationY(-30f);
        welcomeSubtitle.setTranslationY(-20f);
        emailInputLayout.setTranslationY(30f);
        passwordInputLayout.setTranslationY(30f);
        loginButton.setTranslationY(50f);
        forgotPasswordLink.setTranslationY(20f);
        findViewById(R.id.register_link_layout).setTranslationY(30f);
    }
    
    private void startEntranceAnimations() {
        // Animate app title
        animateViewFadeInSlide(appTitle, 200, 0);
        
        // Animate welcome texts
        animateViewFadeInSlide(welcomeTitle, 250, 100);
        animateViewFadeInSlide(welcomeSubtitle, 300, 200);
        
        // Animate input fields
        animateViewFadeInSlide(emailInputLayout, 350, 300);
        animateViewFadeInSlide(passwordInputLayout, 400, 400);
        
        // Animate buttons
        animateViewFadeInSlide(loginButton, 450, 500);
        animateViewFadeInSlide(forgotPasswordLink, 300, 600);
        animateViewFadeInSlide(findViewById(R.id.register_link_layout), 350, 700);
    }
    
    private void animateViewFadeInSlide(View view, long duration, long delay) {
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        ObjectAnimator slideY = ObjectAnimator.ofFloat(view, "translationY", view.getTranslationY(), 0f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.8f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.8f, 1f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(fadeIn, slideY, scaleX, scaleY);
        animatorSet.setDuration(duration);
        animatorSet.setStartDelay(delay);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }
    
    private void animateButtonPress(View view) {
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f);
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 0.95f, 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 0.95f, 1f);
        
        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.playTogether(scaleDownX, scaleDownY);
        scaleDown.setDuration(100);
        
        AnimatorSet scaleUp = new AnimatorSet();
        scaleUp.playTogether(scaleUpX, scaleUpY);
        scaleUp.setDuration(100);
        
        scaleDown.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                scaleUp.start();
            }
        });
        
        scaleDown.start();
    }
    
    private void shakeView(View view) {
        ObjectAnimator shake = ObjectAnimator.ofFloat(view, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
        shake.setDuration(600);
        shake.start();
    }
}
