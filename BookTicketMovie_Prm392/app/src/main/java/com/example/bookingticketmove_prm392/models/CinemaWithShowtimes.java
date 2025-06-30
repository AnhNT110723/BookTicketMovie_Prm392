package com.example.bookingticketmove_prm392.models;

import java.util.List;

public class CinemaWithShowtimes {
    private Cinema cinema;
    private List<Showtime> showtimes;

    public CinemaWithShowtimes(Cinema cinema, List<Showtime> showtimes) {
        this.cinema = cinema;
        this.showtimes = showtimes;
    }

    public Cinema getCinema() {
        return cinema;
    }

    public void setCinema(Cinema cinema) {
        this.cinema = cinema;
    }

    public List<Showtime> getShowtimes() {
        return showtimes;
    }

    public void setShowtimes(List<Showtime> showtimes) {
        this.showtimes = showtimes;
    }
}
