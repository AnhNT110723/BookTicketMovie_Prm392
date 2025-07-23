package com.example.bookingticketmove_prm392.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private Context context;
    private List<Movie> movieList;
    private OnMovieClickListener onMovieClickListener;
    private OnMovieDetailClickListener onMovieDetailClickListener;
    private OnMovieShowtimesClickListener onMovieShowtimesClickListener;
    private boolean isHorizontal;
    private boolean isCinemaDetail = false;

    public interface OnMovieClickListener {
        void onMovieClick(Movie movie);
    }

    public interface OnMovieDetailClickListener {
        void onMovieDetailClick(Movie movie);
    }

    public interface OnMovieShowtimesClickListener {
        void onMovieShowtimesClick(Movie movie);
    }

    public MovieAdapter(Context context, List<Movie> movieList, boolean isHorizontal, boolean isCinemaDetail) {
        this.context = context;
        this.movieList = movieList;
        this.isHorizontal = isHorizontal;
        this.isCinemaDetail = isCinemaDetail;
    }

    public MovieAdapter(Context context, List<Movie> movieList, boolean isHorizontal) {
        this(context, movieList, isHorizontal, false);
    }

    public void setOnMovieClickListener(OnMovieClickListener listener) {
        this.onMovieClickListener = listener;
    }

    public void setOnMovieDetailClickListener(OnMovieDetailClickListener listener) {
        this.onMovieDetailClickListener = listener;
    }

    public void setOnMovieShowtimesClickListener(OnMovieShowtimesClickListener listener) {
        this.onMovieShowtimesClickListener = listener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId;
        if (isCinemaDetail) {
            layoutId = R.layout.item_movie_cinema;
        } else {
            layoutId = isHorizontal ? R.layout.item_movie_horizontal : R.layout.item_movie_grid;
        }
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        holder.bind(movie);
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    // Method to update the movie list
    public void updateMovies(List<Movie> newMovies) {
        this.movieList.clear();
        this.movieList.addAll(newMovies);
        notifyDataSetChanged();
    }

    class MovieViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView posterImageView;
        TextView titleTextView;
        TextView genreTextView;
        TextView durationTextView;
        TextView ratingTextView;
        TextView priceTextView;
        TextView releaseDateTextView;
        View trendingBadge;
        Button btnViewDetail;
        Button btnViewShowtimes;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.movie_card);
            posterImageView = itemView.findViewById(R.id.poster_image);
            titleTextView = itemView.findViewById(R.id.title_text);
            genreTextView = itemView.findViewById(R.id.genre_text);
            durationTextView = itemView.findViewById(R.id.duration_text);
            ratingTextView = itemView.findViewById(R.id.rating_text);
            priceTextView = itemView.findViewById(R.id.price_text);
            releaseDateTextView = itemView.findViewById(R.id.release_date_text);
            trendingBadge = itemView.findViewById(R.id.trending_badge);
            btnViewDetail = itemView.findViewById(R.id.btn_view_detail);
            btnViewShowtimes = itemView.findViewById(R.id.btn_view_showtimes);
        }

        public void bind(Movie movie) {
            titleTextView.setText(movie.getTitle());
            genreTextView.setText(movie.getGenre());
            durationTextView.setText(movie.getFormattedDuration());
            ratingTextView.setText("⭐ " + movie.getFormattedRating());
            priceTextView.setText("$" + String.format("%.2f", movie.getPrice()));

            // Format release date
            if (movie.getReleaseDate() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                releaseDateTextView.setText(dateFormat.format(movie.getReleaseDate()));
            }            // Show/hide trending badge
            if (trendingBadge != null) {
                trendingBadge.setVisibility(movie.isTrending() ? View.VISIBLE : View.GONE);
            }

            // Load poster image using ImageUtils
            ImageUtils.loadMoviePoster(context, posterImageView, movie.getPosterUrl());

            // Set click listener
            cardView.setOnClickListener(v -> {
                if (onMovieClickListener != null) {
                    onMovieClickListener.onMovieClick(movie);
                }
            });

            // Set click cho nút xem chi tiết
            if (btnViewDetail != null) {
                btnViewDetail.setOnClickListener(v -> {
                    if (onMovieDetailClickListener != null) {
                        onMovieDetailClickListener.onMovieDetailClick(movie);
                    }
                });
            }

            // Set click cho nút xem lịch chiếu
            if (btnViewShowtimes != null) {
                btnViewShowtimes.setOnClickListener(v -> {
                    if (onMovieShowtimesClickListener != null) {
                        onMovieShowtimesClickListener.onMovieShowtimesClick(movie);
                    }
                });
            }
        }
    }
}
