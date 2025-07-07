package com.example.bookingticketmove_prm392.models;

public class Comment {
    private int userId;
    private int movieId;
    private String commentText;

    public Comment(int userId, int movieId, String commentText) {
        this.userId = userId;
        this.movieId = movieId;
        this.commentText = commentText;
    }

    public Comment() {
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

    @Override
    public String toString() {
        return "Comment{" +
                "userId=" + userId +
                ", movieId=" + movieId +
                ", commentText='" + commentText + '\'' +
                '}';
    }
}
