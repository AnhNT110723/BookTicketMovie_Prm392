package com.example.bookingticketmove_prm392;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.bookingticketmove_prm392.database.dao.BookingDAO;
import com.example.bookingticketmove_prm392.models.Booking;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.NumberFormat;
import java.util.Locale;

public class BookingDetailActivity extends AppCompatActivity {

    public static final String EXTRA_BOOKING_ID = "extra_booking_id";

    private ImageView moviePoster, qrCodeImage;
    private TextView movieTitle, cinemaName, showTime, seatCount, totalPrice;
    private Button printButton, exportButton, viewTicketButton; // Xóa paymentButton
    private ProgressBar progressBar;
    private View contentLayout;

    private int bookingId;
    private BookingDAO bookingDAO;
    private Booking currentBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_detail);

        bookingId = getIntent().getIntExtra(EXTRA_BOOKING_ID, -1);
        if (bookingId == -1) {
            Toast.makeText(this, "Error: Booking ID not found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        bookingDAO = new BookingDAO();
        initViews();
        setupToolbar();
        loadBookingDetails();
    }

    private void initViews() {
        moviePoster = findViewById(R.id.iv_movie_poster_detail);
        // qrCodeImage = findViewById(R.id.iv_qr_code); // Đã ẩn QR code, không cần ánh xạ nữa
        movieTitle = findViewById(R.id.tv_movie_title_detail);
        cinemaName = findViewById(R.id.tv_cinema_name_detail);
        showTime = findViewById(R.id.tv_show_time_detail);
        seatCount = findViewById(R.id.tv_seats_detail);
        totalPrice = findViewById(R.id.tv_total_price_detail);
        viewTicketButton = findViewById(R.id.btn_view_ticket);
        // printButton = findViewById(R.id.btn_print_ticket); // Đã ẩn
        // exportButton = findViewById(R.id.btn_export_invoice); // Đã ẩn
        // paymentButton = findViewById(R.id.btn_payment); // Đã xóa
        progressBar = findViewById(R.id.progress_bar_detail);
        contentLayout = findViewById(R.id.scroll_view_content);

        // printButton.setOnClickListener(v -> Toast.makeText(this, "Print Ticket clicked", Toast.LENGTH_SHORT).show());
        // exportButton.setOnClickListener(v -> Toast.makeText(this, "Export Invoice clicked", Toast.LENGTH_SHORT).show());
        viewTicketButton.setOnClickListener(v -> {
            if (currentBooking == null) return;
            android.content.Intent intent = new android.content.Intent(this, TicketViewActivity.class);
            intent.putExtra("movieTitle", currentBooking.getMovieTitle());
            intent.putExtra("posterUrl", currentBooking.getPosterURL());
            // Giả lập tách ngày, giờ, row, seats từ dữ liệu booking
            String[] dateTime = currentBooking.getShowTime().split(" ");
            String date = dateTime.length > 0 ? dateTime[0] : "";
            String time = dateTime.length > 1 ? dateTime[1] : "";
            intent.putExtra("showDate", date);
            intent.putExtra("showTime", time);
            intent.putExtra("row", "1"); // fix cứng row, hoặc lấy từ booking nếu có
            intent.putExtra("seats", String.valueOf(currentBooking.getNumberOfSeats()));
            startActivity(intent);
        });
        // Xóa setOnClickListener cho paymentButton
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_booking_detail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Booking Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void loadBookingDetails() {
        new FetchBookingDetailsTask().execute(bookingId);
    }

    private void populateUI(Booking booking) {
        if (booking == null) {
            Toast.makeText(this, "Failed to load booking details.", Toast.LENGTH_LONG).show();
            // Handle error case, maybe show an error message on the screen
            return;
        }
        currentBooking = booking;
        movieTitle.setText(booking.getMovieTitle());
        cinemaName.setText(booking.getCinemaName());
        showTime.setText(booking.getShowTime());
        seatCount.setText(String.format(Locale.getDefault(), "%d Seats", booking.getNumberOfSeats()));
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        totalPrice.setText(String.format("Total: %s", currencyFormat.format(booking.getTotalPrice())));
        Glide.with(this)
                .load(booking.getPosterURL())
                .placeholder(R.drawable.ic_movie_placeholder)
                .into(moviePoster);
        // generateQrCode(booking.getQrCodeData()); // Đã ẩn QR code, không cần sinh nữa
    }

    private void generateQrCode(String data) {
        if (data == null || data.isEmpty()) {
            qrCodeImage.setVisibility(View.GONE);
            return;
        }
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 400, 400);
            qrCodeImage.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "Could not generate QR code.", Toast.LENGTH_SHORT).show();
            qrCodeImage.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class FetchBookingDetailsTask extends AsyncTask<Integer, Void, Booking> {
        private Exception exception;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            contentLayout.setVisibility(View.GONE);
        }

        @Override
        protected Booking doInBackground(Integer... integers) {
            try {
                int id = integers[0];
                return bookingDAO.getBookingById(id);
            } catch (Exception e) {
                exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(Booking booking) {
            super.onPostExecute(booking);
            progressBar.setVisibility(View.GONE);
            contentLayout.setVisibility(View.VISIBLE);
            
            if (exception != null) {
                Toast.makeText(BookingDetailActivity.this, 
                    "Error loading booking details: " + exception.getMessage(), 
                    Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            
            populateUI(booking);
        }
    }
} 