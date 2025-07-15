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
import com.example.bookingticketmove_prm392.models.Cinema;

import java.util.List;

public class CinemaManagementAdapter extends RecyclerView.Adapter<CinemaManagementAdapter.CinemaManagementViewHolder>{
    private Context context;
    private List<Cinema> cinemaList;
    private OnCinemaEditListener onCinemaEditListener;
    private OnCinemaDeleteListener onCinemaDeleteListener;

    public interface OnCinemaEditListener {
        void onEditClick(Cinema cinema);
    }
    public interface OnCinemaDeleteListener {
        void onDeleteClick(Cinema cinema, int position);
    }
    public CinemaManagementAdapter(Context context, List<Cinema> cinemaList) {
        this.context = context;
        this.cinemaList = cinemaList;
    }

    public void setOnCinemaEditListener(OnCinemaEditListener listener) {
        this.onCinemaEditListener = listener;
    }
    public void setOnCinemaDeleteListener(OnCinemaDeleteListener listener) {
        this.onCinemaDeleteListener = listener;
    }

    @NonNull
    @Override
    public CinemaManagementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cinema_admin, parent, false);
        return new CinemaManagementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CinemaManagementViewHolder holder, int position) {
        Cinema cinema = cinemaList.get(position);
        holder.bind(cinema);
    }

    @Override
    public int getItemCount() {
        return cinemaList.size();
    }

    public void updateData(List<Cinema> newList) {
        this.cinemaList = newList;
        notifyDataSetChanged();
    }


    public class CinemaManagementViewHolder extends RecyclerView.ViewHolder {
        private TextView txt_name;
        private TextView txt_address;
        private TextView txt_city;
        private TextView txt_phone;
        private ImageView icon_edit;
        private ImageView icon_trash;

        public CinemaManagementViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_name = itemView.findViewById(R.id.txt_name);
            txt_address = itemView.findViewById(R.id.txt_address);
            txt_city = itemView.findViewById(R.id.txt_city);
            txt_phone= itemView.findViewById(R.id.txt_phone);
            icon_edit = itemView.findViewById(R.id.icon_edit);
            icon_trash = itemView.findViewById(R.id.icon_trash);

        }

        public void bind(Cinema cinema){
            txt_name.setText(cinema.getName());
            txt_address.setText(cinema.getAddress());
            txt_city.setText(cinema.getCityName());
            txt_phone.setText(cinema.getContactInfo());

            //click edit
            icon_edit.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (onCinemaEditListener != null && position != RecyclerView.NO_POSITION) {
                    onCinemaEditListener.onEditClick(cinemaList.get(position));
                }
            });

            //click delete
            icon_trash.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (onCinemaDeleteListener != null && position != RecyclerView.NO_POSITION) {
                    onCinemaDeleteListener.onDeleteClick(cinemaList.get(position), position);
                }
            });
        }
    }
}
