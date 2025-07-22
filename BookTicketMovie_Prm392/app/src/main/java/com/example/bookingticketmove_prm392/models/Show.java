package com.example.bookingticketmove_prm392.models;

import java.util.Date;

public class Show {
    private int showId;
    private int movieId;
    private int hallId;
    private Date startTime;
    private Date endTime;
    private double price;

    public Show() {}

    public Show(int showId, int movieId, int hallId, Date startTime, Date endTime, double price) {
        this.showId = showId;
        this.movieId = movieId;
        this.hallId = hallId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
    }

    public int getShowId() { return showId; }
    public void setShowId(int showId) { this.showId = showId; }
    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }
    public int getHallId() { return hallId; }
    public void setHallId(int hallId) { this.hallId = hallId; }
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    @Override
    public String toString() {
        return "Show{" +
                "showId=" + showId +
                ", movieId=" + movieId +
                ", hallId=" + hallId +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", price=" + price +
                '}';
    }
} 