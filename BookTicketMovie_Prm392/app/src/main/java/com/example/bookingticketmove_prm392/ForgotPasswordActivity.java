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
import com.example.bookingticketmove_prm392.utils.EmailSenderUtil;
import com.example.bookingticketmove_prm392.utils.ValidationUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

public class ForgotPasswordActivity extends AppCompatActivity {
    private TextInputEditText emailInput;
    private TextInputLayout emailInputLayout;
    private Button submitButton;
    private ProgressBar loadingIndicator;
    private TextView backToLogin;

    private static final String SENDER_EMAIL = "your_email@gmail.com";
    private static final String SENDER_PASSWORD = "your_app_password";
    // Khởi tạo FirebaseAuth
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        // Khởi tạo FirebaseAuth

        // Initialize views
        emailInput = findViewById(R.id.email_input);
        emailInputLayout = findViewById(R.id.email_input_layout);
        submitButton = findViewById(R.id.submit_button);
        loadingIndicator = findViewById(R.id.loading_indicator);
        backToLogin = findViewById(R.id.back_to_login);

        submitButton.setOnClickListener(v -> handleForgotPassword());
        backToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void handleForgotPassword() {
        // Clear previous errors
        emailInputLayout.setError(null);

        // Get email input
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";

        // Validate email
        if (TextUtils.isEmpty(email)) {
            emailInputLayout.setError(getString(R.string.error_empty_email));
            return;
        } else if (!ValidationUtils.isValidEmail(email)) {
            emailInputLayout.setError(getString(R.string.error_invalid_email));
            return;
        }

        // Show loading state
        setLoadingState(true);

        // Check if email exists and initiate password reset
        UserDAO userDAO = new UserDAO();
        userDAO.isEmailExists(email, new UserDAO.DatabaseTaskListener<Boolean>() {
            @Override
            public void onSuccess(Boolean emailExists) {
                setLoadingState(false);
                if (emailExists) {
                    sendPasswordResetEmail(email);
                    Intent i = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    emailInputLayout.setError("Email not found");
                    Toast.makeText(ForgotPasswordActivity.this, "Email not registered", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception error) {
                setLoadingState(false);
                Toast.makeText(ForgotPasswordActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        Log.d("ForgotPassword", "handleForgotPassword finished"); // Kiểm tra điểm thoát
    }
    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            submitButton.setText("");
            loadingIndicator.setVisibility(View.VISIBLE);
            submitButton.setEnabled(false);
            emailInput.setEnabled(false);
        } else {
            submitButton.setText(R.string.send_reset_link);
            loadingIndicator.setVisibility(View.GONE);
            submitButton.setEnabled(true);
            emailInput.setEnabled(true);
        }
    }

    private void sendPasswordResetEmail(String email) {
        // 1. Tạo token
        String token = UUID.randomUUID().toString();
        // 2. Tính thời gian hết hạn (30 phút)
        long expiryTimeMillis = System.currentTimeMillis() + 30 * 60 * 1000;
        Timestamp expiresAt = new Timestamp(expiryTimeMillis);

        // 3. Lưu token vào DB
        UserDAO userDAO = new UserDAO();
        userDAO.saveResetToken(email, token, expiresAt, new UserDAO.DatabaseTaskListener<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (result) {
                    // 4. Tạo link và gửi qua email
                    String resetLink = "https://bookingticketmove.page.link/reset-password?token=" + token;
                    String subject = "Reset your password";
                    String body = "Click this link to reset your password: " + resetLink;
                    new EmailSenderUtil(ForgotPasswordActivity.this, email, subject, body).execute();
                    Toast.makeText(ForgotPasswordActivity.this, "Reset link sent to email", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Failed to save reset token", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(ForgotPasswordActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}