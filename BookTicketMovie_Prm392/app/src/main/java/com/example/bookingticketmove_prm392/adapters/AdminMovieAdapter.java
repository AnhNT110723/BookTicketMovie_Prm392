package com.example.bookingticketmove_prm392.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingticketmove_prm392.R;
import com.example.bookingticketmove_prm392.models.Movie;
import com.example.bookingticketmove_prm392.utils.ImageUtils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AdminMovieAdapter extends RecyclerView.Adapter<AdminMovieAdapter.AdminMovieViewHolder> {
    private Context context;
    private List<Movie> movieList;
    private OnMovieActionListener onMovieActionListener;

    public interface OnMovieActionListener {
        void onMovieAction(Movie movie, String action);
    }

    public AdminMovieAdapter(Context context, List<Movie> movieList, OnMovieActionListener listener) {
        this.context = context;
        this.movieList = movieList;
        this.onMovieActionListener = listener;
    }

    @NonNull
    @Override
    public AdminMovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_movie, parent, false);
        return new AdminMovieViewHolder(view);
    }    @Override
    public void onBindViewHolder(@NonNull AdminMovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        
        // Set movie data
        holder.titleText.setText(movie.getTitle());
        holder.descriptionText.setText(movie.getDescription());
        holder.genreText.setText(movie.getGenre());
        holder.durationText.setText(movie.getDuration() + " min");
        holder.directorText.setText("Director: " + movie.getDirector());
        holder.ratingText.setText(String.format(Locale.getDefault(), "%.1f", movie.getRating()));
        
        // Load poster image using ImageUtils
        ImageUtils.loadMoviePoster(context, holder.posterImage, movie.getPosterUrl());
        
        // Format release date
        if (movie.getReleaseDate() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            holder.releaseDateText.setText(dateFormat.format(movie.getReleaseDate()));
        }
        
        // Set status indicators
        holder.statusActiveText.setText(movie.isActive() ? "Active" : "Inactive");
        holder.statusActiveText.setTextColor(context.getResources().getColor(
            movie.isActive() ? R.color.success : R.color.error
        ));
        
        holder.statusTrendingText.setText(movie.isTrending() ? "Trending" : "Regular");
        holder.statusTrendingText.setTextColor(context.getResources().getColor(
            movie.isTrending() ? R.color.primary : R.color.text_secondary
        ));
        
        // Set up action buttons
        holder.editButton.setOnClickListener(v -> {
            if (onMovieActionListener != null) {
                onMovieActionListener.onMovieAction(movie, "edit");
            }
        });
        
        holder.deleteButton.setOnClickListener(v -> {
            if (onMovieActionListener != null) {
                onMovieActionListener.onMovieAction(movie, "delete");
            }
        });
        
        holder.toggleActiveButton.setOnClickListener(v -> {
            if (onMovieActionListener != null) {
                onMovieActionListener.onMovieAction(movie, "toggle_active");
            }
        });
        
        holder.toggleTrendingButton.setOnClickListener(v -> {
            if (onMovieActionListener != null) {
                onMovieActionListener.onMovieAction(movie, "toggle_trending");
            }
        });
        
        // Update button icons based on status
        holder.toggleActiveButton.setImageResource(
            movie.isActive() ? R.drawable.ic_visibility_off : R.drawable.ic_visibility
        );
        
        holder.toggleTrendingButton.setImageResource(
            movie.isTrending() ? R.drawable.ic_trending_down : R.drawable.ic_trending_up
        );
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public class AdminMovieViewHolder extends RecyclerView.ViewHolder {
        CardView movieCard;
        ImageView posterImage;
        TextView titleText;
        TextView descriptionText;
        TextView genreText;
        TextView durationText;
        TextView directorText;
        TextView ratingText;
        TextView releaseDateText;
        TextView statusActiveText;
        TextView statusTrendingText;
        ImageButton editButton;
        ImageButton deleteButton;
        ImageButton toggleActiveButton;
        ImageButton toggleTrendingButton;

        public AdminMovieViewHolder(@NonNull View itemView) {
            super(itemView);
            
            movieCard = itemView.findViewById(R.id.movie_card);
            posterImage = itemView.findViewById(R.id.poster_image);
            titleText = itemView.findViewById(R.id.title_text);
            descriptionText = itemView.findViewById(R.id.description_text);
            genreText = itemView.findViewById(R.id.genre_text);
            durationText = itemView.findViewById(R.id.duration_text);
            directorText = itemView.findViewById(R.id.director_text);
            ratingText = itemView.findViewById(R.id.rating_text);
            releaseDateText = itemView.findViewById(R.id.release_date_text);
            statusActiveText = itemView.findViewById(R.id.status_active_text);
            statusTrendingText = itemView.findViewById(R.id.status_trending_text);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
            toggleActiveButton = itemView.findViewById(R.id.toggle_active_button);
            toggleTrendingButton = itemView.findViewById(R.id.toggle_trending_button);
        }
    }
}
