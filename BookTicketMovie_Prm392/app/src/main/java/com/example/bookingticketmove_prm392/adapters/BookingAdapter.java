package com.example.bookingticketmove_prm392.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookingticketmove_prm392.R;
import com.example.bookingticketmove_prm392.models.Booking;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private final Context context;
    private final List<Booking> bookingList;
    private final OnBookingClickListener listener;

    public interface OnBookingClickListener {
        void onBookingClick(Booking booking);
    }

    public BookingAdapter(Context context, List<Booking> bookingList, OnBookingClickListener listener) {
        this.context = context;
        this.bookingList = bookingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        holder.bind(booking, listener);
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        private final ImageView moviePoster;
        private final TextView movieTitle;
        private final TextView cinemaName;
        private final TextView showTime;
        private final TextView numberOfSeats;
        private final TextView totalPrice;
        private final TextView bookingStatus;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            moviePoster = itemView.findViewById(R.id.movie_poster);
            movieTitle = itemView.findViewById(R.id.movie_title);
            cinemaName = itemView.findViewById(R.id.cinema_name);
            showTime = itemView.findViewById(R.id.show_time);
            numberOfSeats = itemView.findViewById(R.id.number_of_seats);
            totalPrice = itemView.findViewById(R.id.total_price);
            bookingStatus = itemView.findViewById(R.id.booking_status);
        }

        public void bind(final Booking booking, final OnBookingClickListener listener) {
            movieTitle.setText(booking.getMovieTitle());
            cinemaName.setText(String.format("%s - %s", booking.getCinemaName(), booking.getHallName()));
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            showTime.setText(dateFormat.format(booking.getShowtime()));
            
            numberOfSeats.setText(String.format(Locale.getDefault(), "%d Seats", booking.getNumberOfSeats()));

            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            totalPrice.setText(currencyFormat.format(booking.getTotalPrice()));
            
            bookingStatus.setText(booking.getStatus().toUpperCase());
            setStatusColor(booking.getStatus());

            Glide.with(itemView.getContext())
                 .load(booking.getPosterUrl())
                 .placeholder(R.drawable.ic_movie_placeholder)
                 .error(R.drawable.ic_movie_placeholder)
                 .into(moviePoster);

            itemView.setOnClickListener(v -> listener.onBookingClick(booking));
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
    }
} 