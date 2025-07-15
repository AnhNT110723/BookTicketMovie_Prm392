package com.example.bookingticketmove_prm392.models;

public class Vote {
    private int userId;
    private int movieId;
    private int ratingValue;
    private String voteTime;



    public Vote(int userId, int movieId, int ratingValue, String voteTime) {
        this.userId = userId;
        this.movieId = movieId;
        this.ratingValue = ratingValue;
        this.voteTime = voteTime;
    }

    public String getVoteTime() {
        return voteTime;
    }

    public void setVoteTime(String voteTime) {
        this.voteTime = voteTime;
    }

    public Vote() {
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

    public int getRatingValue() {
        return ratingValue;
    }

    public void setRatingValue(int ratingValue) {
        this.ratingValue = ratingValue;
    }

    @Override
    public String toString() {
        return "Vote{" +
                "userId=" + userId +
                ", movieId=" + movieId +
                ", ratingValue=" + ratingValue +
                '}';
    }
}
