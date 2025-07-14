package com.example.bookingticketmove_prm392.database.dao;

import com.example.bookingticketmove_prm392.models.City;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CityDAO extends BaseDAO {
    public List<City> getAllCities() throws SQLException {
        List<City> cities = new ArrayList<>();
        String query = "SELECT CityID, Name FROM City ORDER BY Name";
        ResultSet rs = null;
        PreparedStatement statement = null;
        try {
            rs = executeQuery(query);
            while (rs.next()) {
                City city = new City();
                city.setCityId(rs.getInt("CityID"));
                city.setName(rs.getString("Name"));
                cities.add(city);
            }
        } finally {
            closeResources(rs, statement);
        }
        return cities;
    }
} 