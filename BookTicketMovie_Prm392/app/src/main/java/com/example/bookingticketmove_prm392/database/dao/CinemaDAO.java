package com.example.bookingticketmove_prm392.database.dao;

import android.util.Log;

import com.example.bookingticketmove_prm392.models.Cinema;
import com.example.bookingticketmove_prm392.models.CinemaWithShowtimes;
import com.example.bookingticketmove_prm392.models.Showtime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
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

    // Get city ID by name
    public int getCityIdByName(String cityName) throws SQLException {
        String query = "SELECT CityID FROM City WHERE Name = ?";
        ResultSet rs = null;
        PreparedStatement statement = null;
        int cityId = -1;

        try {
            rs = executeQuery(query, cityName);
            if (rs.next()) {
                cityId = rs.getInt("CityID");
            }
        } finally {
            closeResources(rs, statement);
        }
        return cityId;
    }

    // Get cinemas with showtimes by city and date
    public List<CinemaWithShowtimes> getCinemasWithShowtimesByCityAndDate(int cityId, int movieId, LocalDate date) throws SQLException {
        List<CinemaWithShowtimes> cinemaWithShowtimesList = new ArrayList<>();
        String cinemaQuery = "SELECT c.CinemaID, c.Name, c.Address, c.CityID, c.ContactInfo, ci.Name as CityName " +
                "FROM Cinema c " +
                "LEFT JOIN City ci ON c.CityID = ci.CityID " +
                "WHERE c.CityID = ? " +
                "ORDER BY c.Name";

        ResultSet rsCinema = null;
        PreparedStatement statementCinema = null;

        try {
            rsCinema = executeQuery(cinemaQuery, cityId);
            ShowtimeDAO showDAO = new ShowtimeDAO();

            while (rsCinema.next()) {
                Cinema cinema = mapResultSetToCinema(rsCinema);
                List<Showtime> showtimes = showDAO.getShowtimesByCinemaAndDate(cinema.getCinemaId(), movieId, date);
                CinemaWithShowtimes cinemaWithShowtimes = new CinemaWithShowtimes(cinema, showtimes);
                if(showtimes.size() > 0) {
                    cinemaWithShowtimesList.add(cinemaWithShowtimes);
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error fetching cinemas with showtimes", e);
            throw e;
        } finally {
            closeResources(rsCinema, statementCinema);
        }

        return cinemaWithShowtimesList;
    }


// Get city name
    public List<String> getCityNames() throws SQLException {
        List<String> cities = new ArrayList<>();
        String query = "SELECT Name FROM City ORDER BY Name"; // Truy vấn lấy tên thành phố
        ResultSet rs = null;
        PreparedStatement statement = null;

        try {
            rs = executeQuery(query);
            while (rs.next()) {
                String cityName = rs.getString("Name");
                if (cityName != null) {
                    cities.add(cityName);
                }
            }
        } catch (SQLException e) {
            Log.e("CinemaDAO", "Error fetching city names", e);
            throw e; // Ném ngoại lệ để xử lý ở tầng cao hơn
        } finally {
            closeResources(rs, statement); // Đóng tài nguyên
        }

        return cities;
    }

    //Add cninema
    public boolean addCinema(Cinema cinema) throws SQLException{
        String query = "INSERT INTO Cinema (Name, Address, CityID, ContactInfo) VALUES (?, ?, ?, ?)";
        int result = executeUpdate(query,
                cinema.getName(),
                cinema.getAddress(),
                cinema.getCityId(),
                cinema.getContactInfo());
        return result > 0;
    }

    //Delete cinema
    public boolean deleteCinema(int cinemaId) throws SQLException {
        String query = "DELETE FROM Cinema WHERE CinemaID = ?";
        int result = executeUpdate(query, cinemaId);
        return result > 0;
    }


    //update cinema
    public boolean updateCinema(Cinema cinema) throws SQLException {
        String query = "UPDATE Cinema SET Name = ?, Address = ?, CityID = ?, ContactInfo = ? WHERE CinemaID = ?";

        int result = executeUpdate(query,
                cinema.getName(),
                cinema.getAddress(),
                cinema.getCityId(),
                cinema.getContactInfo(),
                cinema.getCinemaId()
        );

        return result > 0;
    }
}
