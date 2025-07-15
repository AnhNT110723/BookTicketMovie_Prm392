package com.example.bookingticketmove_prm392.database.dao;

import com.example.bookingticketmove_prm392.models.City;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CityDAO extends BaseDAO{
    //get list city
    public List<City> getAllCities() throws SQLException {
        List<City> cities = new ArrayList<>();
        String query = "SELECT * FROM City";
        ResultSet rs = null;
        PreparedStatement statement = null;

        try {
            rs = executeQuery(query);
            while (rs.next()) {
                City city = mapResultSetToCity(rs);
                cities.add(city);
            }
        } finally {
            closeResources(rs, statement);
        }
        return cities;
    }

    //get city by id
    public City getCityById(int cityId) throws SQLException {
        String query = "SELECT * FROM City WHERE CityID = ?";
        ResultSet rs = null;
        PreparedStatement statement = null;

        try {
            rs = executeQuery(query, cityId);
            if (rs.next()) {
               City city = mapResultSetToCity(rs);
               return city;
            }
        } finally {
            closeResources(rs, statement);
            }
        return null;
    }

    private City mapResultSetToCity(ResultSet rs) throws SQLException {
        City city = new City();
        city.setCityId(rs.getInt("CityID"));
        city.setCityName(rs.getString("Name"));
        return city;
    }
}
