package com.example.bookingticketmove_prm392.database.dao;

import com.example.bookingticketmove_prm392.models.Cinema;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CinemaDAO extends BaseDAO {    // Get all cinemas
    public List<Cinema> getAllCinemas() throws SQLException {
        List<Cinema> cinemas = new ArrayList<>();
        String query = "SELECT c.CinemaID, c.Name, c.Address, c.CityID, c.ContactInfo, ci.Name as CityName " +
                      "FROM Cinema c " +
                      "LEFT JOIN City ci ON c.CityID = ci.CityID " +
                      "ORDER BY c.Name";

        ResultSet rs = null;
        PreparedStatement statement = null;
        
        try {
            rs = executeQuery(query);
            while (rs.next()) {
                Cinema cinema = mapResultSetToCinema(rs);
                cinemas.add(cinema);
            }
        } finally {
            closeResources(rs, statement);
        }

        return cinemas;
    }    // Get cinema by ID
    public Cinema getCinemaById(int cinemaId) throws SQLException {
        String query = "SELECT c.CinemaID, c.Name, c.Address, c.CityID, c.ContactInfo, ci.Name as CityName " +
                      "FROM Cinema c " +
                      "LEFT JOIN City ci ON c.CityID = ci.CityID " +
                      "WHERE c.CinemaID = ?";

        ResultSet rs = null;
        PreparedStatement statement = null;
        
        try {
            rs = executeQuery(query, cinemaId);
            if (rs.next()) {
                return mapResultSetToCinema(rs);
            }
        } finally {
            closeResources(rs, statement);
        }
        
        return null;
    }    // Get cinemas by city
    public List<Cinema> getCinemasByCity(int cityId) throws SQLException {
        List<Cinema> cinemas = new ArrayList<>();
        String query = "SELECT c.CinemaID, c.Name, c.Address, c.CityID, c.ContactInfo, ci.Name as CityName " +
                      "FROM Cinema c " +
                      "LEFT JOIN City ci ON c.CityID = ci.CityID " +
                      "WHERE c.CityID = ? " +
                      "ORDER BY c.Name";

        ResultSet rs = null;
        PreparedStatement statement = null;
        
        try {
            rs = executeQuery(query, cityId);
            while (rs.next()) {
                Cinema cinema = mapResultSetToCinema(rs);
                cinemas.add(cinema);
            }
        } finally {
            closeResources(rs, statement);
        }

        return cinemas;
    }

    // Helper method to map ResultSet to Cinema object
    private Cinema mapResultSetToCinema(ResultSet rs) throws SQLException {
        Cinema cinema = new Cinema();
        cinema.setCinemaId(rs.getInt("CinemaID"));
        cinema.setName(rs.getString("Name"));
        cinema.setAddress(rs.getString("Address"));
        cinema.setCityId(rs.getInt("CityID"));
        cinema.setContactInfo(rs.getString("ContactInfo"));
        cinema.setCityName(rs.getString("CityName"));
        return cinema;
    }
}
