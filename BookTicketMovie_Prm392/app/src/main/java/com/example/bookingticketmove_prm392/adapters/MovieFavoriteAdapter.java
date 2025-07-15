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
import com.example.bookingticketmove_prm392.models.Movie;
import com.example.bookingticketmove_prm392.utils.ImageUtils;

import java.util.List;

public class MovieFavoriteAdapter extends RecyclerView.Adapter<MovieFavoriteAdapter.MovieFavoriteHolder> {
    private Context context;
    private List<Movie> movieList;
    private OnItemClickListener onItemClickListener;
    private OnItemDeleteClickListener deleteItem;

    public interface OnItemClickListener {
        void onItemClick(Movie movie);
    }

    public interface OnItemDeleteClickListener {
        void onItemDeleteClick(Movie movie, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    public void setOnItemDeleteClickListener(OnItemDeleteClickListener listener) {
        this.deleteItem = listener;
    }

    public MovieFavoriteAdapter(Context context, List<Movie> movieList) {
        this.context = context;
        this.movieList = movieList;
    }

    @NonNull
    @Override
    public MovieFavoriteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.movie_favorite_item, parent, false);

        return new MovieFavoriteHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieFavoriteHolder holder, int position) {
        Movie movie = movieList.get(position);
        holder.bind(movie);
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }


    public class MovieFavoriteHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView txt_title;
        TextView txt_genre;
        TextView txt_price;
        ImageView icon_trash;


        public MovieFavoriteHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img);
            txt_title = itemView.findViewById(R.id.txt_title);
            txt_genre = itemView.findViewById(R.id.txt_genre);
            txt_price = itemView.findViewById(R.id.txt_price);
            icon_trash = itemView.findViewById(R.id.icon_trash);

            //click item
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (onItemClickListener != null && position != RecyclerView.NO_POSITION) {
                    onItemClickListener.onItemClick(movieList.get(position));
                }
            });

            //click delete item
            icon_trash.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (deleteItem != null && position != RecyclerView.NO_POSITION) {
                    deleteItem.onItemDeleteClick(movieList.get(position), position);
                }
            });
        }

        public void bind(Movie movie) {
            txt_title.setText(movie.getTitle());
            txt_genre.setText(movie.getGenre());
            txt_price.setText("$" + String.format("%.2f", movie.getPrice()));

            //Load image of movie
            ImageUtils.loadMoviePoster(context, img, movie.getPosterUrl());
        }


    }

}