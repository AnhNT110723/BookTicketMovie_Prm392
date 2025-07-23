package com.example.bookingticketmove_prm392;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SuccessPaymentActivity extends AppCompatActivity {
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success_payment);
        toolbar = findViewById(R.id.toolbar);
        setupToolbar();
        TextView tvSuccessMessage = findViewById(R.id.tv_success_message);
        Button btnHome = findViewById(R.id.btn_home);

        // Nhận dữ liệu từ Intent
        String transactionNo = getIntent().getStringExtra("transaction_no");
        String amount = getIntent().getStringExtra("amount");
        String orderInfo = getIntent().getStringExtra("order_info");

        // Hiển thị thông tin thanh toán thành công
        String message = "Thanh toán thành công!\nMã giao dịch: " + transactionNo + "\nSố tiền: " + amount + " VND\nThông tin: " + orderInfo;
        tvSuccessMessage.setText(message);

        // Nút về Trang chủ
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("");
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
}