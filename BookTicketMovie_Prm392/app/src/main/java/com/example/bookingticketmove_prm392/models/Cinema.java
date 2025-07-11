package com.example.bookingticketmove_prm392.models;

public class Cinema {
    private int cinemaId;
    private String name;
    private String address;
    private int cityId;
    private String contactInfo;
    private String cityName; // For display purposes

    // Default constructor
    public Cinema() {
    }

    // Constructor with essential fields
    public Cinema(String name, String address, int cityId, String contactInfo) {
        this.name = name;
        this.address = address;
        this.cityId = cityId;
        this.contactInfo = contactInfo;
    }

    // Full constructor
    public Cinema(int cinemaId, String name, String address, int cityId, String contactInfo) {
        this.cinemaId = cinemaId;
        this.name = name;
        this.address = address;
        this.cityId = cityId;
        this.contactInfo = contactInfo;
    }

    //Constructor for manage cinema
    public Cinema(String name, String address, String cityName, String contactInfo){
        this.name = name;
        this.address = address;
        this.cityName = cityName;
        this.contactInfo = contactInfo;
    }

    //Constructor for fragment add hall
    public Cinema(int cinemaId, String name){
        this.cinemaId = cinemaId;
        this.name = name;
    }

    // Getters and Setters
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    @Override
    public String toString() {
        return name;
    }
}
