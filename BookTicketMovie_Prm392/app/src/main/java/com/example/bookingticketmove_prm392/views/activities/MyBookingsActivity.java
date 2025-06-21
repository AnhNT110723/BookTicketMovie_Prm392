package com.example.bookingticketmove_prm392.views.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingticketmove_prm392.R;
import com.example.bookingticketmove_prm392.adapters.BookingAdapter;
import com.example.bookingticketmove_prm392.database.dao.BookingDAO;
import com.example.bookingticketmove_prm392.models.Booking;
import com.example.bookingticketmove_prm392.models.User;
import com.example.bookingticketmove_prm392.database.dao.UserDAO;

import java.util.ArrayList;
import java.util.List;

public class MyBookingsActivity extends AppCompatActivity implements BookingAdapter.OnBookingClickListener {

    private RecyclerView recyclerView;
    private BookingAdapter adapter;
    private List<Booking> bookingList;
    private ProgressBar progressBar;
    private TextView emptyView;
    private Toolbar toolbar;
    private BookingDAO bookingDAO;
    private UserDAO userDAO;
    private SharedPreferences sharedPreferences;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        initViews();
        setupToolbar();

        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        bookingDAO = new BookingDAO();
        userDAO = new UserDAO();

        bookingList = new ArrayList<>();
        adapter = new BookingAdapter(this, bookingList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadUserAndFetchBookings();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.bookings_recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        emptyView = findViewById(R.id.empty_view);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadUserAndFetchBookings() {
        progressBar.setVisibility(View.VISIBLE);
        String userEmail = sharedPreferences.getString("userEmail", null);
        Log.d("MyBookingsActivity", "User email from SharedPreferences: " + userEmail);

        if (userEmail != null) {
            userDAO.getUserByEmail(userEmail, new UserDAO.DatabaseTaskListener<User>() {
                @Override
                public void onSuccess(User user) {
                    if (user != null) {
                        currentUser = user;
                        Log.d("MyBookingsActivity", "User found: " + currentUser.getName() + ", ID: " + currentUser.getUserID());
                        fetchBookings(String.valueOf(currentUser.getUserID()));
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Log.e("MyBookingsActivity", "User not found in database for email: " + userEmail);
                        Toast.makeText(MyBookingsActivity.this, "User not found.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(Exception error) {
                    progressBar.setVisibility(View.GONE);
                    Log.e("MyBookingsActivity", "Error fetching user data.", error);
                    Toast.makeText(MyBookingsActivity.this, "Error fetching user data.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            progressBar.setVisibility(View.GONE);
            Log.w("MyBookingsActivity", "No user email found in SharedPreferences. User is not logged in.");
            Toast.makeText(this, "Not logged in.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchBookings(String userId) {
        Log.d("MyBookingsActivity", "Fetching bookings for UserID: " + userId);
        bookingDAO.getBookingsByUserId(userId, new BookingDAO.GetBookingsListener() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                progressBar.setVisibility(View.GONE);
                Log.d("MyBookingsActivity", "Bookings received: " + (bookings != null ? bookings.size() : "null"));
                if (bookings != null && !bookings.isEmpty()) {
                    bookingList.clear();
                    bookingList.addAll(bookings);
                    adapter.notifyDataSetChanged();
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                } else {
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(Exception e) {
                progressBar.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText("Failed to load bookings.");
                Toast.makeText(MyBookingsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("MyBookingsActivity", "Error fetching bookings", e);
            }
        });
    }

    @Override
    public void onBookingClick(Booking booking) {
        Intent intent = new Intent(this, BookingDetailActivity.class);
        intent.putExtra(BookingDetailActivity.EXTRA_BOOKING, booking);
        startActivity(intent);
    }
} 