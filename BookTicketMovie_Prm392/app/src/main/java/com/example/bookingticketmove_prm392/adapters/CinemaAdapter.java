package com.example.bookingticketmove_prm392.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingticketmove_prm392.R;
import com.example.bookingticketmove_prm392.models.Cinema;

import java.util.List;

public class CinemaAdapter extends RecyclerView.Adapter<CinemaAdapter.CinemaViewHolder> {
    private Context context;
    private List<Cinema> cinemaList;
    private OnCinemaClickListener onCinemaClickListener;

    public interface OnCinemaClickListener {
        void onCinemaClick(Cinema cinema);
    }

    public CinemaAdapter(Context context, List<Cinema> cinemaList) {
        this.context = context;
        this.cinemaList = cinemaList;
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
        Cinema cinema = cinemaList.get(position);
        holder.bind(cinema);
    }

    @Override
    public int getItemCount() {
        return cinemaList.size();
    }

    class CinemaViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView nameTextView;
        TextView addressTextView;
        TextView cityTextView;
        TextView contactTextView;

        public CinemaViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cinema_card);
            nameTextView = itemView.findViewById(R.id.cinema_name_text);
            addressTextView = itemView.findViewById(R.id.cinema_address_text);
            cityTextView = itemView.findViewById(R.id.cinema_city_text);
            contactTextView = itemView.findViewById(R.id.cinema_contact_text);
        }

        public void bind(Cinema cinema) {
            nameTextView.setText(cinema.getName());
            addressTextView.setText(cinema.getAddress());
            
            if (cinema.getCityName() != null && !cinema.getCityName().isEmpty()) {
                cityTextView.setText(cinema.getCityName());
                cityTextView.setVisibility(View.VISIBLE);
            } else {
                cityTextView.setVisibility(View.GONE);
            }
            
            if (cinema.getContactInfo() != null && !cinema.getContactInfo().isEmpty()) {
                contactTextView.setText(cinema.getContactInfo());
                contactTextView.setVisibility(View.VISIBLE);
            } else {
                contactTextView.setVisibility(View.GONE);
            }

            cardView.setOnClickListener(v -> {
                if (onCinemaClickListener != null) {
                    onCinemaClickListener.onCinemaClick(cinema);
                }
            });
        }
    }
}
