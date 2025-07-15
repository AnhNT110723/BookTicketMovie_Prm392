package com.example.bookingticketmove_prm392.models;

import com.example.bookingticketmove_prm392.Enums.SeatType;

public class Seat {
    private int seatID;
    private int hallID;
    private String rowNumber;
    private int columnNumber;
    private SeatType seatType;
    private String status;

    public Seat() {
    }


    public Seat(int seatID, int hallID, String rowNumber, int columnNumber, SeatType seatType, String status) {
        this.seatID = seatID;
        this.hallID = hallID;
        this.rowNumber = rowNumber;
        this.columnNumber = columnNumber;
        this.seatType = seatType;
        this.status = status;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    public int getHallID() {
        return hallID;
    }

    public void setHallID(int hallID) {
        this.hallID = hallID;
    }

    public int getSeatID() {
        return seatID;
    }

    public void setSeatID(int seatID) {
        this.seatID = seatID;
    }

    public String getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(String rowNumber) {
        this.rowNumber = rowNumber;
    }

    public SeatType getSeatType() {
        return seatType;
    }

    public void setSeatType(SeatType seatType) {
        this.seatType = seatType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

