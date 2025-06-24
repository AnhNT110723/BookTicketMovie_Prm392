package com.example.bookingticketmove_prm392;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bookingticketmove_prm392.database.dao.UserDAO;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;


public class ResetPasswordActivity extends AppCompatActivity {
    private TextInputEditText passwordInput, confirmPasswordInput;
    private TextInputLayout passwordInputLayout, confirmPasswordInputLayout;
    private Button resetButton;
    private ProgressBar loadingIndicator;
    private String verifiedEmail;

    private String oobCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Khởi tạo views
        passwordInput = findViewById(R.id.password_input);
        passwordInputLayout = findViewById(R.id.password_input_layout);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);
        confirmPasswordInputLayout = findViewById(R.id.confirm_password_input_layout);
        resetButton = findViewById(R.id.reset_button);
        loadingIndicator = findViewById(R.id.loading_indicator);

        Uri data = getIntent().getData();
        if(data != null && data.getQueryParameter("token") != null) {
            String token = data.getQueryParameter("token");
            verifyToken(token);
        } else {
            Toast.makeText(this, "Invalid reset link", Toast.LENGTH_SHORT).show();
            finish();
        }

        resetButton.setOnClickListener(v -> handleResetPassword());

    }

    private void handleResetPassword() {
        // Xóa lỗi trước đó
        passwordInputLayout.setError(null);
        confirmPasswordInputLayout.setError(null);

        // Lấy dữ liệu đầu vào
        String password = passwordInput.getText() != null ? passwordInput.getText().toString().trim() : "";
        String confirmPassword = confirmPasswordInput.getText() != null ? confirmPasswordInput.getText().toString().trim() : "";

        // Xác thực đầu vào
        if (TextUtils.isEmpty(password)) {
            passwordInputLayout.setError("Password cannot be empty");
            return;
        } else if (password.length() < 6) {
            passwordInputLayout.setError("Password must be at least 6 characters");
            return;
        } else if (!password.equals(confirmPassword)) {
            confirmPasswordInputLayout.setError("Passwords do not match");
            return;
        }

        // Hiển thị trạng thái tải
        setLoadingState(true);
        // Gọi cập nhật mật khẩu
        UserDAO.UpdatePasswordTask task = new UserDAO.UpdatePasswordTask(
                verifiedEmail,
                password,
                new UserDAO.DatabaseTaskListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        setLoadingState(false);
                        if (result) {
                            Toast.makeText(ResetPasswordActivity.this, "Password has been updated", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(ResetPasswordActivity.this, "Password update failed", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onError(Exception error) {
                        setLoadingState(false);
                        Toast.makeText(ResetPasswordActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );

        task.execute();

    }

    private void verifyToken(String token) {
        setLoadingState(true);

        UserDAO userDAO = new UserDAO();
        userDAO.verifyResetToken(token, new UserDAO.DatabaseTaskListener<String>() {
            @Override
            public void onSuccess(String email) {
                setLoadingState(false);
                verifiedEmail = email; // lưu email hợp lệ để reset sau này
            }

            @Override
            public void onError(Exception error) {
                setLoadingState(false);
                Toast.makeText(ResetPasswordActivity.this, "Invalid or expired token", Toast.LENGTH_LONG).show();
                finish(); // thoát nếu token sai
            }
        });
    }

    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            resetButton.setText("");
            loadingIndicator.setVisibility(View.VISIBLE);
            resetButton.setEnabled(false);
            passwordInput.setEnabled(false);
            confirmPasswordInput.setEnabled(false);
        } else {
            resetButton.setText("Reset Password");
            loadingIndicator.setVisibility(View.GONE);
            resetButton.setEnabled(true);
            passwordInput.setEnabled(true);
            confirmPasswordInput.setEnabled(true);
        }
    }
}