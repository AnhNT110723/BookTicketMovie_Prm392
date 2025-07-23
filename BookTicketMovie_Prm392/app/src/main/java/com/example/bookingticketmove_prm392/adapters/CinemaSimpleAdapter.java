package com.example.bookingticketmove_prm392.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bookingticketmove_prm392.R;
import com.example.bookingticketmove_prm392.models.Cinema;
import java.util.List;

public class CinemaSimpleAdapter extends RecyclerView.Adapter<CinemaSimpleAdapter.CinemaViewHolder> {
    private Context context;
    private List<Cinema> cinemaList;
    private OnCinemaClickListener listener;

    public interface OnCinemaClickListener {
        void onCinemaClick(Cinema cinema);
    }

    public CinemaSimpleAdapter(Context context, List<Cinema> cinemaList, OnCinemaClickListener listener) {
        this.context = context;
        this.cinemaList = cinemaList;
        this.listener = listener;
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
        holder.nameTextView.setText(cinema.getName());
        holder.addressTextView.setText(cinema.getAddress());
        holder.itemView.setOnClickListener(v -> listener.onCinemaClick(cinema));
    }

    @Override
    public int getItemCount() {
        return cinemaList != null ? cinemaList.size() : 0;
    }

    static class CinemaViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, addressTextView;
        CinemaViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.cinema_name_text);
            addressTextView = itemView.findViewById(R.id.cinema_address_text);
        }
    }
} 