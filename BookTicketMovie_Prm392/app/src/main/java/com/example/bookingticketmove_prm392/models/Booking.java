package com.example.bookingticketmove_prm392.models;

import java.math.BigDecimal;
import java.util.Date;
import java.io.Serializable;

public class Booking implements Serializable {
    private int bookingId;
    private String movieTitle;
    private String cinemaName;
    private String hallName;
    private Date showtime;
    private int numberOfSeats;
    private BigDecimal totalPrice;
    private String status;
    private String posterUrl;
    private String qrcode;


    public Booking(int bookingId, String movieTitle, String cinemaName, String hallName, Date showtime, int numberOfSeats, BigDecimal totalPrice, String status, String posterUrl, String qrcode) {
        this.bookingId = bookingId;
        this.movieTitle = movieTitle;
        this.cinemaName = cinemaName;
        this.hallName = hallName;
        this.showtime = showtime;
        this.numberOfSeats = numberOfSeats;
        this.totalPrice = totalPrice;
        this.status = status;
        this.posterUrl = posterUrl;
        this.qrcode = qrcode;
    }


    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getCinemaName() {
        return cinemaName;
    }

    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName;
    }

    public String getHallName() {
        return hallName;
    }

    public void setHallName(String hallName) {
        this.hallName = hallName;
    }

    public Date getShowtime() {
        return showtime;
    }

    public void setShowtime(Date showtime) {
        this.showtime = showtime;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public void setNumberOfSeats(int numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

     public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }
} 