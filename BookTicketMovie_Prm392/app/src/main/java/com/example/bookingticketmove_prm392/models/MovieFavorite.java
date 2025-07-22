package com.example.bookingticketmove_prm392.models;

public class MovieFavorite {
    private int movieFavoriteId;
    private int userId;
    private int movieId;

    public MovieFavorite(int movieFavoriteId, int userId, int movieId) {
        this.movieFavoriteId = movieFavoriteId;
        this.userId = userId;
        this.movieId = movieId;
    }

    public MovieFavorite(int userId, int movieId) {
        this.userId = userId;
        this.movieId = movieId;
    }

    public MovieFavorite() {
    }

    public int getMovieFavoriteId() {
        return movieFavoriteId;
    }

    public void setMovieFavoriteId(int movieFavoriteId) {
        this.movieFavoriteId = movieFavoriteId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    @Override
    public String toString() {
        return "MovieFavorite{" +
                "userId=" + userId +
                ", movieId=" + movieId +
                '}';
    }
}
