package com.example.bookingticketmove_prm392.models;

import java.util.Date;

public class BookingSeat {
    private int bookedSeatId;
    private int bookingId;
    private int seatId;
    private int showId;
    private boolean isReserved;
    private int reservedByUserId;
    private Date reservationTimestamp;
    public BookingSeat() {
    }
    public BookingSeat(int bookingId, int bookedSeatId, boolean isReserved, Date reservationTimestamp, int reservedByUserId, int seatId, int showId) {
        this.bookingId = bookingId;
        this.bookedSeatId = bookedSeatId;
        this.isReserved = isReserved;
        this.reservationTimestamp = reservationTimestamp;
        this.reservedByUserId = reservedByUserId;
        this.seatId = seatId;
        this.showId = showId;
    }

    public int getBookedSeatId() {
        return bookedSeatId;
    }

    public void setBookedSeatId(int bookedSeatId) {
        this.bookedSeatId = bookedSeatId;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public boolean isReserved() {
        return isReserved;
    }

    public void setReserved(boolean reserved) {
        isReserved = reserved;
    }

    public Date getReservationTimestamp() {
        return reservationTimestamp;
    }

    public void setReservationTimestamp(Date reservationTimestamp) {
        this.reservationTimestamp = reservationTimestamp;
    }

    public int getReservedByUserId() {
        return reservedByUserId;
    }

    public void setReservedByUserId(int reservedByUserId) {
        this.reservedByUserId = reservedByUserId;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public int getShowId() {
        return showId;
    }

    public void setShowId(int showId) {
        this.showId = showId;
    }
}
