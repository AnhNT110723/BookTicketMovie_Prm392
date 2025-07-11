package com.example.bookingticketmove_prm392.models;

public class CinemaHall {
    private int hallId;
    private int cinemaId;
    private String name;
    private int totalSeats;

    public CinemaHall() {
    }

    public CinemaHall(int hallId,int cinemaId, String name, int totalSeats) {
        this.hallId = hallId;
        this.cinemaId = cinemaId;
        this.name = name;
        this.totalSeats = totalSeats;
    }

    public CinemaHall(int cinemaId, String name, int totalSeats) {
        this.cinemaId = cinemaId;
        this.name = name;
        this.totalSeats = totalSeats;
    }

    public int getHallId() {
        return hallId;
    }

    public void setHallId(int hallId) {
        this.hallId = hallId;
    }

    public int getCinemaId() {
        return cinemaId;
    }

    public void setCinemaId(int cinemaId) {
        this.cinemaId = cinemaId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }
}
