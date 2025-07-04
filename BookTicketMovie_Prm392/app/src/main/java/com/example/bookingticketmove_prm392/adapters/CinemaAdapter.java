package com.example.bookingticketmove_prm392.adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
    private OnShowtimeSelectedListener onShowtimeSelectedListener;

    public interface OnShowtimeSelectedListener {
        // Thêm tham số 'isHeaderClick' để phân biệt click vào header và click vào khung giờ
        void onShowtimeSelected(CinemaWithShowtimes cinema, Showtime showtime, LocalDate selectedDate, boolean isHeaderClick);
    }

    public CinemaAdapter(Context context, List<CinemaWithShowtimes> cinemaWithShowtimesList, int movieId, LocalDate selectedDate) {
        this.context = context;
        this.cinemaWithShowtimesList = cinemaWithShowtimesList;
        this.movieId = movieId;
        this.selectedDate = selectedDate;
    }


    // <-- THÊM SETTER CHO LISTENER MỚI -->
    public void setOnShowtimeSelectedListener(OnShowtimeSelectedListener listener) {
        this.onShowtimeSelectedListener = listener;
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
        ImageView expandCollapseArrow;
        RecyclerView showtimesRecyclerView;
       // boolean isExpanded = false; // Trạng thái mở rộng

        public CinemaViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cinema_card);
            nameTextView = itemView.findViewById(R.id.cinema_name_text);
            addressTextView = itemView.findViewById(R.id.cinema_address_text);
//            cityTextView = itemView.findViewById(R.id.cinema_city_text);
//            contactTextView = itemView.findViewById(R.id.cinema_contact_text);
            showtimesRecyclerView = itemView.findViewById(R.id.showtimes_recycler_view);
            expandCollapseArrow = itemView.findViewById(R.id.expand_collapse_arrow);
        }

        public void bind(CinemaWithShowtimes cinemaWithShowtimes) {
            nameTextView.setText(cinemaWithShowtimes.getCinema().getName());
            addressTextView.setText(cinemaWithShowtimes.getCinema().getAddress());

            // Cập nhật trạng thái hiển thị của showtimesRecyclerView và icon mũi tên dựa vào isExpanded
            showtimesRecyclerView.setVisibility(cinemaWithShowtimes.isExpanded() ? View.VISIBLE : View.GONE);
            expandCollapseArrow.setImageResource(cinemaWithShowtimes.isExpanded() ? R.drawable.ic_arrow_up : R.drawable.ic_arrow_down);


            // Lấy danh sách giờ chiếu cho ngày đã chọn
            List<Showtime> showtimes = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                showtimes = cinemaWithShowtimes.getShowtimes().stream()
                        .filter(showtime -> showtime.getStartTime() != null && showtime.getStartTime().toLocalDate().equals(selectedDate))
                        .collect(Collectors.toList());
            }

            // Khởi tạo và thiết lập ShowtimeAdapter
            ShowtimeAdapter showtimeAdapter = new ShowtimeAdapter(context, showtimes);
            showtimeAdapter.setOnShowtimeClickListener(selectedShowtime -> {
                if (onShowtimeSelectedListener != null) {
                    onShowtimeSelectedListener.onShowtimeSelected(cinemaWithShowtimes, selectedShowtime, selectedDate, false); // isHeaderClick = false
                }
            });
            showtimesRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            showtimesRecyclerView.setAdapter(showtimeAdapter);

            // Xử lý khi không có suất chiếu cho ngày đã chọn
            if (showtimes == null || showtimes.isEmpty()) {
                showtimesRecyclerView.setVisibility(View.GONE);
                expandCollapseArrow.setVisibility(View.GONE);
                cardView.setClickable(false);
                cardView.setFocusable(false);
                cinemaWithShowtimes.setExpanded(false); // Đảm bảo đóng nếu không có suất chiếu
            } else {
                expandCollapseArrow.setVisibility(View.VISIBLE);
                cardView.setClickable(true);
                cardView.setFocusable(true);
            }
            // Bắt sự kiện click vào CardView (toàn bộ item rạp)
            cardView.setOnClickListener(v -> {
                // Đảo ngược trạng thái TRONG MODEL
                cinemaWithShowtimes.setExpanded(!cinemaWithShowtimes.isExpanded());

                // Cập nhật lại UI dựa trên trạng thái mới của MODEL
                showtimesRecyclerView.setVisibility(cinemaWithShowtimes.isExpanded() ? View.VISIBLE : View.GONE);
                expandCollapseArrow.setImageResource(cinemaWithShowtimes.isExpanded() ? R.drawable.ic_arrow_up : R.drawable.ic_arrow_down);

//                // Nếu bạn muốn xử lý thêm click của rạp ở Activity
//                if (onCinemaClickListener != null) {
//                    onCinemaClickListener.onCinemaClick(cinemaWithShowtimes);
//                }
            });
        }
    }
}

// Adapter cho giờ chiếu
class ShowtimeAdapter extends RecyclerView.Adapter<ShowtimeAdapter.ShowtimeViewHolder> {
    private Context context;
    private List<Showtime> showtimes;
    private OnShowtimeClickListener onShowtimeClickListener;
    // Interface này bây giờ chỉ cần Showtime
    public interface OnShowtimeClickListener {
        void onShowtimeClick(Showtime showtime); // Chỉ truyền Showtime
    }

    public ShowtimeAdapter(Context context, List<Showtime> showtimes) {
        this.context = context;
        this.showtimes = showtimes;

    }
    public void setOnShowtimeClickListener(OnShowtimeClickListener listener) {
        this.onShowtimeClickListener = listener;
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
        // --- VÀ CẢ DÒNG NÀY ---
        holder.itemView.setOnClickListener(v -> {
            if (onShowtimeClickListener != null) {
                onShowtimeClickListener.onShowtimeClick(showtime);
            }
        });
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