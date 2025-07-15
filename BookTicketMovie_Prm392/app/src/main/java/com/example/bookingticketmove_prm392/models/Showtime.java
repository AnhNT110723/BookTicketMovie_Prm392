package com.example.bookingticketmove_prm392.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Showtime {
    private int showtimeId;
    private int movieId;
    private int hallId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String timeSlot;

    private double ticketPrice;

    public Showtime () {

    }
    public Showtime(LocalDateTime endTime, int hallId, int movieId, int showtimeId, LocalDateTime startTime, double ticketPrice, String timeSlot) {
        this.endTime = endTime;
        this.hallId = hallId;
        this.movieId = movieId;
        this.showtimeId = showtimeId;
        this.startTime = startTime;
        this.ticketPrice = ticketPrice;
        this.timeSlot = timeSlot;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public int getHallId() {
        return hallId;
    }

    public void setHallId(int hallId) {
        this.hallId = hallId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public double getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(double ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public int getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(int showtimeId) {
        this.showtimeId = showtimeId;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }
}
