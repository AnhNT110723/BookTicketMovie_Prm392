package com.example.bookingticketmove_prm392.models;

public class City {
    private int cityId;
    private String cityName;

    public City(int cityId, String cityName) {
        this.cityId = cityId;
        this.cityName = cityName;
    }

    public City() {
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    @Override
    public String toString() {
        return cityName;
    }
}
