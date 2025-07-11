package com.example.bookingticketmove_prm392.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingticketmove_prm392.R;
import com.example.bookingticketmove_prm392.models.CinemaHall;

import java.util.List;

public class CinemaHallAdapter extends RecyclerView.Adapter<CinemaHallAdapter.CinemaHallHolder> {
    private Context context;
    private List<CinemaHall> cinemaHallList;
    private OnHallEditClickListener editItem;
    private OnHallDeleteClickListener deleteItem;
    public interface OnHallEditClickListener {
        void onHallEditClick(CinemaHall cinemaHall);
    }
    public interface OnHallDeleteClickListener {
        void onHallDeleteClick(CinemaHall cinemaHall, int position);
    }

    public CinemaHallAdapter(Context context, List<CinemaHall> cinemaHallList){
        this.context = context;
        this.cinemaHallList = cinemaHallList;
    }
    @NonNull
    @Override
    public CinemaHallHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_hall_cinema, parent, false);
        return new CinemaHallHolder(view);
    }

    public void setOnHallEditClickListener(OnHallEditClickListener listener) {
        this.editItem = listener;
    }
    public void setOnHallDeleteClickListener(OnHallDeleteClickListener listener) {
        this.deleteItem = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull CinemaHallHolder holder, int position) {
        CinemaHall cinemaHall = cinemaHallList.get(position);
        holder.bind(cinemaHall);
    }

    @Override
    public int getItemCount() {
        return cinemaHallList.size();
    }

    class CinemaHallHolder extends RecyclerView.ViewHolder {
        TextView txt_room;
        TextView txt_seats;
        ImageView icon_edit;
        ImageView icon_trash;

        public CinemaHallHolder(View itemView) {
            super(itemView);
            txt_room = itemView.findViewById(R.id.txt_room);
            txt_seats = itemView.findViewById(R.id.txt_seats);
            icon_edit = itemView.findViewById(R.id.icon_edit);
            icon_trash = itemView.findViewById(R.id.icon_trash);
        }

        public void bind(CinemaHall cinemaHall) {
            txt_room.setText(cinemaHall.getName());
            txt_seats.setText(cinemaHall.getTotalSeats() + " seats");

            icon_edit.setOnClickListener(v  -> {
                // Handle edit icon click
            });

            icon_trash.setOnClickListener(v -> {
                // Handle trash icon click
            });
        }
    }
}
