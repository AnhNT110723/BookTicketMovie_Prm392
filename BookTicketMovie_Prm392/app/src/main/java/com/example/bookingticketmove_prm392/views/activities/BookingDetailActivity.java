package com.example.bookingticketmove_prm392.views.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.bookingticketmove_prm392.R;
import com.example.bookingticketmove_prm392.database.dao.BookingDAO;
import com.example.bookingticketmove_prm392.models.Booking;
import com.example.bookingticketmove_prm392.models.Seat;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class BookingDetailActivity extends AppCompatActivity {

    public static final String EXTRA_BOOKING = "extra_booking";
    private static final String TAG = "BookingDetailActivity";

    private BookingDAO bookingDAO;
    private Booking currentBooking;

    private ImageView moviePoster, qrCodeImage;
    private TextView movieTitle, cinemaName, showTime, bookedSeats, bookingStatus, totalPrice;
    private Button printTicketButton, exportInvoiceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_detail);

        bookingDAO = new BookingDAO();
        
        setupToolbar();
        initViews();

        currentBooking = (Booking) getIntent().getSerializableExtra(EXTRA_BOOKING);

        if (currentBooking != null) {
            populateBookingData();
            fetchSeatsForBooking();
            generateQrCode();
        } else {
            Toast.makeText(this, "Failed to load booking details.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Booking object was null.");
            finish();
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initViews() {
        moviePoster = findViewById(R.id.movie_poster);
        qrCodeImage = findViewById(R.id.qr_code_image);
        movieTitle = findViewById(R.id.movie_title);
        cinemaName = findViewById(R.id.cinema_name);
        showTime = findViewById(R.id.show_time);
        bookedSeats = findViewById(R.id.booked_seats);
        bookingStatus = findViewById(R.id.booking_status);
        totalPrice = findViewById(R.id.total_price);
        printTicketButton = findViewById(R.id.print_ticket_button);
        exportInvoiceButton = findViewById(R.id.export_invoice_button);
    }

    private void populateBookingData() {
        movieTitle.setText(currentBooking.getMovieTitle());
        cinemaName.setText(String.format("%s - %s", currentBooking.getCinemaName(), currentBooking.getHallName()));

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy | HH:mm", Locale.getDefault());
        showTime.setText(dateFormat.format(currentBooking.getShowtime()));

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        totalPrice.setText(String.format("Total: %s", currencyFormat.format(currentBooking.getTotalPrice())));
        
        bookingStatus.setText(currentBooking.getStatus().toUpperCase());
        setStatusColor(currentBooking.getStatus());

        Glide.with(this)
             .load(currentBooking.getPosterUrl())
             .placeholder(R.drawable.ic_movie_placeholder)
             .into(moviePoster);
    }
    
    private void setStatusColor(String status) {
        int color;
        switch (status.toLowerCase()) {
            case "confirmed":
                color = Color.parseColor("#4CAF50"); // Green
                break;
            case "pending":
                color = Color.parseColor("#FFC107"); // Amber
                break;
            case "cancelled":
                color = Color.parseColor("#F44336"); // Red
                break;
            default:
                color = Color.parseColor("#9E9E9E"); // Grey
                break;
        }
        GradientDrawable background = (GradientDrawable) bookingStatus.getBackground();
        background.setColor(color);
    }

    private void fetchSeatsForBooking() {
        bookingDAO.getSeatsForBooking(currentBooking.getBookingId(), new BookingDAO.GetSeatsListener() {
            @Override
            public void onSuccess(List<Seat> seats) {
                if (seats != null && !seats.isEmpty()) {
                    String seatNames = seats.stream().map(Seat::toString).collect(Collectors.joining(", "));
                    bookedSeats.setText(String.format("Seats: %s", seatNames));
                } else {
                    bookedSeats.setText("Seats: N/A");
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to fetch seats", e);
                bookedSeats.setText("Seats: Error");
            }
        });
    }

    private void generateQrCode() {
        String qrData = currentBooking.getQrcode();
        if (qrData == null || qrData.isEmpty()) {
            // Use booking ID as fallback QR data
            qrData = "BookingID:" + currentBooking.getBookingId();
            Log.w(TAG, "QR code data is null or empty. Using fallback data: " + qrData);
        }

        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(qrData, BarcodeFormat.QR_CODE, 400, 400);
            qrCodeImage.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Log.e(TAG, "Failed to generate QR code", e);
            Toast.makeText(this, "Could not generate QR code.", Toast.LENGTH_SHORT).show();
        }
    }
} 