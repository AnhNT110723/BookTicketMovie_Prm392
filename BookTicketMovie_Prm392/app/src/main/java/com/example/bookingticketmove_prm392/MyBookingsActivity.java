package com.example.bookingticketmove_prm392;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingticketmove_prm392.adapters.BookingAdapter;
import com.example.bookingticketmove_prm392.database.dao.BookingDAO;
import com.example.bookingticketmove_prm392.models.Booking;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MyBookingsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private BookingAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvNoBookings;
    private BookingDAO bookingDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        // Initialize views
        setupViews();

        // Initialize DAO
        bookingDAO = new BookingDAO();

        // Set up RecyclerView
        setupRecyclerView();

        // Load bookings
        loadBookings();
    }

    private void setupViews() {
        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Bookings");
        }

        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        tvNoBookings = findViewById(R.id.tv_no_bookings);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);
    }

    private void loadBookings() {
        progressBar.setVisibility(View.VISIBLE);
        
        // Lấy userId thực tế từ SharedPreferences
        int userId = getSharedPreferences("UserSession", MODE_PRIVATE).getInt("userId", -1);
        if (userId == -1) {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                tvNoBookings.setText("Không tìm thấy thông tin tài khoản. Vui lòng đăng nhập lại.");
                tvNoBookings.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            });
            return;
        }

        new Thread(() -> {
            List<Booking> bookings = null;
            try {
                bookings = bookingDAO.getBookingsByUserId(userId);
            } catch (SQLException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvNoBookings.setText("Error loading bookings. Please try again.");
                    tvNoBookings.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                });
                return;
            }
            
            final List<Booking> finalBookings = bookings;
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                
                if (finalBookings != null && !finalBookings.isEmpty()) {
                    adapter.updateBookings(finalBookings);
                    tvNoBookings.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                } else {
                    tvNoBookings.setText("No bookings found");
                    tvNoBookings.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            });
        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 