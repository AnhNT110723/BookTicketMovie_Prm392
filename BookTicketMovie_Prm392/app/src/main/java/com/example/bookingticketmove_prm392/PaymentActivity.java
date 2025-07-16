package com.example.bookingticketmove_prm392;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class PaymentActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        TextView tvAmount = findViewById(R.id.tv_payment_amount);
        TextView tvContent = findViewById(R.id.tv_payment_content);
        ImageView ivQr = findViewById(R.id.iv_payment_qr);

        // Nhận dữ liệu từ intent
        String amount = getIntent().getStringExtra("amount");
        String content = getIntent().getStringExtra("content");
        tvAmount.setText("Số tiền: " + amount + " đ");
        tvContent.setText("Nội dung: " + content);

        // --- Sử dụng VietQR API để sinh mã QR chuyển khoản BIDV ---
        String bankId = "BIDV";
        String accountNumber = "4271030930";
        String qrUrl = "https://img.vietqr.io/image/" + bankId + "-" + accountNumber + "-compact2.png?amount=" + amount + "&addInfo=" + content;
        Glide.with(this).load(qrUrl).into(ivQr);

        // Xử lý nút Trang chủ và Quay lại
        findViewById(R.id.btn_home).setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
} 