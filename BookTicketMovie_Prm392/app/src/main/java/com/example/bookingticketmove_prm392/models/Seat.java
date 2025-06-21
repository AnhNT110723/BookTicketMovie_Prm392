package com.example.bookingticketmove_prm392.models;

import java.io.Serializable;

public class Seat implements Serializable {
    private String row;
    private int number;
    private String type;

    public Seat(String row, int number, String type) {
        this.row = row;
        this.number = number;
        this.type = type;
    }

    public String getRow() {
        return row;
    }

    public void setRow(String row) {
        this.row = row;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return row + number;
    }
} 