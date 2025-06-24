package com.example.bookingticketmove_prm392.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookingticketmove_prm392.R;
import com.example.bookingticketmove_prm392.BookingDetailActivity;
import com.example.bookingticketmove_prm392.models.Booking;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {
    private Context context;
    private List<Booking> bookings;
    private final SimpleDateFormat dateFormat;

    public BookingAdapter(Context context, List<Booking> bookings) {
        this.context = context;
        this.bookings = bookings;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy | HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.bind(booking);
        
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BookingDetailActivity.class);
            intent.putExtra(BookingDetailActivity.EXTRA_BOOKING_ID, booking.getBookingID());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    public void updateBookings(List<Booking> newBookings) {
        this.bookings = newBookings;
        notifyDataSetChanged();
    }

    public class BookingViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMoviePoster;
        TextView tvMovieTitle, tvCinemaName, tvShowTime, tvSeats, tvTotalPrice, tvStatus;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMoviePoster = itemView.findViewById(R.id.iv_movie_poster);
            tvMovieTitle = itemView.findViewById(R.id.tv_movie_title);
            tvCinemaName = itemView.findViewById(R.id.tv_cinema_name);
            tvShowTime = itemView.findViewById(R.id.tv_show_time);
            tvSeats = itemView.findViewById(R.id.tv_seats);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);
            tvStatus = itemView.findViewById(R.id.tv_status);
        }

        public void bind(Booking booking) {
            // Load movie poster
            if (booking.getPosterURL() != null && !booking.getPosterURL().isEmpty()) {
                Glide.with(context)
                        .load(booking.getPosterURL())
                        .placeholder(R.drawable.ic_movie_placeholder)
                        .error(R.drawable.ic_movie_placeholder)
                        .into(ivMoviePoster);
            }

            // Set movie title
            tvMovieTitle.setText(booking.getMovieTitle());

            // Set cinema name
            tvCinemaName.setText(booking.getCinemaName());

            // Set show time
            if (booking.getBookingTime() != null) {
                tvShowTime.setText(dateFormat.format(booking.getBookingTime()));
            }

            // Set number of seats
            tvSeats.setText(String.format(Locale.getDefault(), "%d Seats", booking.getNumberOfSeats()));

            // Set total price
            if (booking.getTotalPrice() != null) {
                tvTotalPrice.setText(String.format(Locale.getDefault(), "%,.0f đ", booking.getTotalPrice().doubleValue()));
            }

            // Set status with appropriate color
            if (booking.getStatus() != null) {
                tvStatus.setText(booking.getStatus().toUpperCase());
                updateStatusBadge(tvStatus, booking.getStatus());
            }
        }

        private void updateStatusBadge(TextView statusView, String status) {
            int backgroundColor;
            switch (status.toLowerCase()) {
                case "confirmed":
                    backgroundColor = ContextCompat.getColor(context, R.color.status_confirmed);
                    break;
                case "cancelled":
                    backgroundColor = ContextCompat.getColor(context, R.color.status_cancelled);
                    break;
                default:
                    backgroundColor = ContextCompat.getColor(context, R.color.status_pending);
                    break;
            }
            statusView.getBackground().setTint(backgroundColor);
        }
    }
} 