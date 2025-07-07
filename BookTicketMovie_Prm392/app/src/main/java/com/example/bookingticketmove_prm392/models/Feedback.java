package com.example.bookingticketmove_prm392.models;

public class Feedback {
    private String name;
    private int userId;
    private int movieId;
    private String commentText;
    private Integer ratingValue;

    public Feedback() {
    }

    public Feedback(Integer ratingValue, String commentText, int movieId, int userId, String name) {
        this.ratingValue = ratingValue;
        this.commentText = commentText;
        this.movieId = movieId;
        this.userId = userId;
        this.name = name;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public Integer getRatingValue() {
        return ratingValue;
    }

    public void setRatingValue(Integer ratingValue) {
        this.ratingValue = ratingValue;
    }

    @Override
    public String toString() {
        return "Feeback{" +
                "name='" + name + '\'' +
                ", userId=" + userId +
                ", movieId=" + movieId +
                ", commentText='" + commentText + '\'' +
                ", ratingValue=" + ratingValue +
                '}';
    }
}
