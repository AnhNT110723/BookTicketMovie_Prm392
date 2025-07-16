package com.example.bookingticketmove_prm392;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.WriterException;

public class TicketViewActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_view);

        Toolbar toolbar = findViewById(R.id.ticket_toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ImageView poster = findViewById(R.id.img_ticket_poster);
        TextView title = findViewById(R.id.tv_ticket_movie_title);
        TextView date = findViewById(R.id.tv_ticket_date);
        TextView time = findViewById(R.id.tv_ticket_time);
        TextView row = findViewById(R.id.tv_ticket_row);
        TextView seats = findViewById(R.id.tv_ticket_seats);
        ImageView qr = findViewById(R.id.img_ticket_qr);

        // Lấy dữ liệu từ Intent (hoặc database)
        String movieTitle = getIntent().getStringExtra("movieTitle");
        String posterUrl = getIntent().getStringExtra("posterUrl");
        String showDate = getIntent().getStringExtra("showDate");
        String showTime = getIntent().getStringExtra("showTime");
        String rowStr = getIntent().getStringExtra("row");
        String seatsStr = getIntent().getStringExtra("seats");

        title.setText(movieTitle != null ? movieTitle : "Movie Title");
        date.setText("Date: " + (showDate != null ? showDate : "--"));
        time.setText("   Time: " + (showTime != null ? showTime : "--"));
        row.setText("Row: " + (rowStr != null ? rowStr : "--"));
        seats.setText("   Seats: " + (seatsStr != null ? seatsStr : "--"));

        Glide.with(this)
                .load(posterUrl)
                .placeholder(R.drawable.ic_movie_placeholder)
                .into(poster);
        // QR code: tạo barcode dạng Code 128
        String barcodeContent = "Test-Barcode-1234";
        try {
            Bitmap barcodeBitmap = createBarcode(barcodeContent, 600, 180);
            qr.setImageBitmap(barcodeBitmap);
        } catch (WriterException e) {
            qr.setImageResource(R.drawable.ic_movie_placeholder);
        }
    }

    // Hàm tạo barcode Code 128
    private Bitmap createBarcode(String contents, int width, int height) throws WriterException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(contents, BarcodeFormat.CODE_128, width, height);
        int w = bitMatrix.getWidth();
        int h = bitMatrix.getHeight();
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                bmp.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return bmp;
    }
} 