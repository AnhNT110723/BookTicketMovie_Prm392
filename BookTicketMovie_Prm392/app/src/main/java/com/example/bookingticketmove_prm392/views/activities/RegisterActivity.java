package com.example.bookingticketmove_prm392.views.activities;

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

import com.example.bookingticketmove_prm392.R;
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

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    // UI Components
    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText phoneInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;
    private TextInputLayout nameInputLayout;
    private TextInputLayout emailInputLayout;
    private TextInputLayout phoneInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputLayout confirmPasswordInputLayout;
    private Button registerButton;
    private ProgressBar loadingIndicator;
    private TextView loginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

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
        nameInput = findViewById(R.id.name_input);
        emailInput = findViewById(R.id.email_input);
        phoneInput = findViewById(R.id.phone_input);
        passwordInput = findViewById(R.id.password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);
        nameInputLayout = findViewById(R.id.name_input_layout);
        emailInputLayout = findViewById(R.id.email_input_layout);
        phoneInputLayout = findViewById(R.id.phone_input_layout);
        passwordInputLayout = findViewById(R.id.password_input_layout);
        confirmPasswordInputLayout = findViewById(R.id.confirm_password_input_layout);
        registerButton = findViewById(R.id.register_button);
        loadingIndicator = findViewById(R.id.loading_indicator);
        loginLink = findViewById(R.id.login_link);
    }

    private void setupClickListeners() {
        registerButton.setOnClickListener(v -> attemptRegister());
        
        loginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Remove register activity from stack
        });
    }

    private void attemptRegister() {
        // Clear previous errors
        clearErrors();

        // Get input values
        String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        String phone = phoneInput.getText() != null ? phoneInput.getText().toString().trim() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString() : "";
        String confirmPassword = confirmPasswordInput.getText() != null ? confirmPasswordInput.getText().toString() : "";

        // Validate inputs
        if (!validateInputs(name, email, phone, password, confirmPassword)) {
            return;
        }

        // Show loading state
        setLoadingState(true);

        // First check if email already exists
        checkEmailExists(email, name, phone, password);
    }

    private void clearErrors() {
        nameInputLayout.setError(null);
        emailInputLayout.setError(null);
        phoneInputLayout.setError(null);
        passwordInputLayout.setError(null);
        confirmPasswordInputLayout.setError(null);
    }

    private boolean validateInputs(String name, String email, String phone, String password, String confirmPassword) {
        boolean isValid = true;

        if (TextUtils.isEmpty(name)) {
            nameInputLayout.setError(getString(R.string.error_empty_name));
            isValid = false;
        } else if (!ValidationUtils.isValidName(name)) {
            nameInputLayout.setError("Name must be at least 2 characters long");
            isValid = false;
        }

        if (TextUtils.isEmpty(email)) {
            emailInputLayout.setError(getString(R.string.error_empty_email));
            isValid = false;
        } else if (!ValidationUtils.isValidEmail(email)) {
            emailInputLayout.setError(getString(R.string.error_invalid_email));
            isValid = false;
        }

        if (TextUtils.isEmpty(phone)) {
            phoneInputLayout.setError(getString(R.string.error_empty_phone));
            isValid = false;
        } else if (!ValidationUtils.isValidPhone(phone)) {
            phoneInputLayout.setError("Please enter a valid phone number");
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInputLayout.setError(getString(R.string.error_empty_password));
            isValid = false;
        } else if (!ValidationUtils.isValidPassword(password)) {
            passwordInputLayout.setError(getString(R.string.error_password_too_short));
            isValid = false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordInputLayout.setError("Please confirm your password");
            isValid = false;
        } else if (!ValidationUtils.doPasswordsMatch(password, confirmPassword)) {
            confirmPasswordInputLayout.setError(getString(R.string.error_passwords_dont_match));
            isValid = false;
        }

        return isValid;
    }

    private void checkEmailExists(String email, String name, String phone, String password) {
        UserDAO.GetUserByEmailTask checkEmailTask = new UserDAO.GetUserByEmailTask(email,
            new UserDAO.DatabaseTaskListener<User>() {
                @Override
                public void onSuccess(User existingUser) {
                    if (existingUser != null) {
                        setLoadingState(false);
                        emailInputLayout.setError(getString(R.string.error_email_already_exists));
                    } else {
                        // Email doesn't exist, proceed with registration
                        createUser(name, email, phone, password);
                    }
                }

                @Override
                public void onError(Exception error) {
                    setLoadingState(false);
                    Log.e(TAG, "Error checking email: " + error.getMessage(), error);
                    Toast.makeText(RegisterActivity.this, "Error checking email: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        checkEmailTask.execute();
    }

    private void createUser(String name, String email, String phone, String password) {
        // Create user object
        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPhone(phone);
        newUser.setPasswordHash(PasswordUtils.simpleHash(password));
        newUser.setRoleID(2); // Default to Customer role

        Log.d(TAG, "Creating new user: " + email);

        // Create user in database
        UserDAO.CreateUserTask createTask = new UserDAO.CreateUserTask(newUser,
            new UserDAO.DatabaseTaskListener<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    setLoadingState(false);
                    if (result) {
                        onRegistrationSuccess();
                    } else {
                        onRegistrationFailed(getString(R.string.error_registration_failed));
                    }
                }

                @Override
                public void onError(Exception error) {
                    setLoadingState(false);
                    Log.e(TAG, "Registration error: " + error.getMessage(), error);
                    onRegistrationFailed("Registration failed: " + error.getMessage());
                }
            });

        createTask.execute();
    }

    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            registerButton.setText("");
            loadingIndicator.setVisibility(View.VISIBLE);
            registerButton.setEnabled(false);
            setInputsEnabled(false);
        } else {
            registerButton.setText(R.string.sign_up);
            loadingIndicator.setVisibility(View.GONE);
            registerButton.setEnabled(true);
            setInputsEnabled(true);
        }
    }

    private void setInputsEnabled(boolean enabled) {
        nameInput.setEnabled(enabled);
        emailInput.setEnabled(enabled);
        phoneInput.setEnabled(enabled);
        passwordInput.setEnabled(enabled);
        confirmPasswordInput.setEnabled(enabled);
    }

    private void onRegistrationSuccess() {
        Toast.makeText(this, getString(R.string.registration_successful), Toast.LENGTH_LONG).show();
        Log.d(TAG, "Registration successful");
        
        // Navigate to login activity
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void onRegistrationFailed(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        
        // Clear password fields for security
        passwordInput.setText("");
        confirmPasswordInput.setText("");
    }
}
