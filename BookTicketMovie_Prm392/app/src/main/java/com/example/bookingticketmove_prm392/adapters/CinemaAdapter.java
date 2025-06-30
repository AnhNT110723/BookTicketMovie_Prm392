package com.example.bookingticketmove_prm392.adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingticketmove_prm392.R;
import com.example.bookingticketmove_prm392.models.CinemaWithShowtimes;
import com.example.bookingticketmove_prm392.models.Showtime;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class CinemaAdapter extends RecyclerView.Adapter<CinemaAdapter.CinemaViewHolder> {
    private Context context;
    private int movieId;
    private List<CinemaWithShowtimes> cinemaWithShowtimesList;
    private LocalDate selectedDate;
    private OnCinemaClickListener onCinemaClickListener;

    public interface OnCinemaClickListener {
        void onCinemaClick(CinemaWithShowtimes cinemaWithShowtimes);
    }

    public CinemaAdapter(Context context, List<CinemaWithShowtimes> cinemaWithShowtimesList, int movieId, LocalDate selectedDate) {
        this.context = context;
        this.cinemaWithShowtimesList = cinemaWithShowtimesList;
        this.movieId = movieId;
        this.selectedDate = selectedDate;
    }

    public void setOnCinemaClickListener(OnCinemaClickListener listener) {
        this.onCinemaClickListener = listener;
    }

    @NonNull
    @Override
    public CinemaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cinema, parent, false);
        return new CinemaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CinemaViewHolder holder, int position) {
        CinemaWithShowtimes cinemaWithShowtimes = cinemaWithShowtimesList.get(position);
        holder.bind(cinemaWithShowtimes);
    }

    @Override
    public int getItemCount() {
        return cinemaWithShowtimesList != null ? cinemaWithShowtimesList.size() : 0;
    }

    public void updateDate(LocalDate date) {
        this.selectedDate = date;
        notifyDataSetChanged(); // Refresh giao diện với ngày mới
    }

    class CinemaViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView nameTextView;
        TextView addressTextView;
        TextView cityTextView;
        TextView contactTextView;
        RecyclerView showtimesRecyclerView;
        boolean isExpanded = false; // Trạng thái mở rộng

        public CinemaViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cinema_card);
            nameTextView = itemView.findViewById(R.id.cinema_name_text);
            addressTextView = itemView.findViewById(R.id.cinema_address_text);
            cityTextView = itemView.findViewById(R.id.cinema_city_text);
            contactTextView = itemView.findViewById(R.id.cinema_contact_text);
            showtimesRecyclerView = itemView.findViewById(R.id.showtimes_recycler_view);
        }

        public void bind(CinemaWithShowtimes cinemaWithShowtimes) {
            nameTextView.setText(cinemaWithShowtimes.getCinema().getName());
            addressTextView.setText(cinemaWithShowtimes.getCinema().getAddress());

            if (cinemaWithShowtimes.getCinema().getCityName() != null && !cinemaWithShowtimes.getCinema().getCityName().isEmpty()) {
                cityTextView.setText(cinemaWithShowtimes.getCinema().getCityName());
                cityTextView.setVisibility(View.VISIBLE);
            } else {
                cityTextView.setVisibility(View.GONE);
            }

            if (cinemaWithShowtimes.getCinema().getContactInfo() != null && !cinemaWithShowtimes.getCinema().getContactInfo().isEmpty()) {
                contactTextView.setText(cinemaWithShowtimes.getCinema().getContactInfo());
                contactTextView.setVisibility(View.VISIBLE);
            } else {
                contactTextView.setVisibility(View.GONE);
            }

            // Cập nhật giờ chiếu cho ngày đã chọn
            updateShowtimes(cinemaWithShowtimes);

            cardView.setOnClickListener(v -> {
                isExpanded = !isExpanded;
                showtimesRecyclerView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
                if (onCinemaClickListener != null) {
                    onCinemaClickListener.onCinemaClick(cinemaWithShowtimes);
                }
            });
        }

        private void updateShowtimes(CinemaWithShowtimes cinemaWithShowtimes) {
            List<Showtime> showtimes = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                showtimes = cinemaWithShowtimes.getShowtimes().stream()
                        .filter(showtime -> showtime.getStartTime() != null && showtime.getStartTime().toLocalDate().equals(selectedDate))
                        .collect(Collectors.toList());
            }
            ShowtimeAdapter showtimeAdapter = new ShowtimeAdapter(context, showtimes);
            showtimesRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            showtimesRecyclerView.setAdapter(showtimeAdapter);
        }
    }
}

// Adapter cho giờ chiếu
class ShowtimeAdapter extends RecyclerView.Adapter<ShowtimeAdapter.ShowtimeViewHolder> {
    private Context context;
    private List<Showtime> showtimes;

    public ShowtimeAdapter(Context context, List<Showtime> showtimes) {
        this.context = context;
        this.showtimes = showtimes;
    }

    @NonNull
    @Override
    public ShowtimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_showtime, parent, false);
        return new ShowtimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShowtimeViewHolder holder, int position) {
        Showtime showtime = showtimes.get(position);
        holder.bind(showtime);
    }

    @Override
    public int getItemCount() {
        return showtimes.size();
    }

    class ShowtimeViewHolder extends RecyclerView.ViewHolder {
        TextView timeTextView;

        public ShowtimeViewHolder(@NonNull View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.showtime_text);
        }

        public void bind(Showtime showtime) {
            if (showtime.getStartTime() != null) {
                timeTextView.setText(showtime.getStartTime().toLocalTime().toString());
            } else {
                timeTextView.setText("N/A");
            }
        }
    }
}