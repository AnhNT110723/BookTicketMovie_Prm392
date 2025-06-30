package com.example.bookingticketmove_prm392.database.dao;

import com.example.bookingticketmove_prm392.database.DatabaseConfig;
import com.example.bookingticketmove_prm392.models.Booking;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO extends BaseDAO {
    
    public List<Booking> getBookingsByUserId(int userId) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String query = "SELECT b.*, m.Title as MovieTitle, m.PosterURL, c.Name as CinemaName, s.StartTime " +
                      "FROM " + DatabaseConfig.TABLE_BOOKING + " b " +
                      "JOIN " + DatabaseConfig.TABLE_SHOW + " s ON b.ShowID = s.ShowID " +
                      "JOIN " + DatabaseConfig.TABLE_MOVIE + " m ON s.MovieID = m.MovieID " +
                      "JOIN " + DatabaseConfig.TABLE_CINEMA_HALL + " ch ON s.HallID = ch.HallID " +
                      "JOIN " + DatabaseConfig.TABLE_CINEMA + " c ON ch.CinemaID = c.CinemaID " +
                      "WHERE b.UserID = ? " +
                      "ORDER BY b.BookingTime DESC";

        ResultSet rs = null;
        PreparedStatement statement = null;
        
        try {
            rs = executeQuery(query, userId);
            while (rs.next()) {
                bookings.add(extractBookingFromResultSet(rs));
            }
        } finally {
            closeResources(rs, statement);
        }
        
        return bookings;
    }
    
    public Booking getBookingById(int bookingId) throws SQLException {
        String query = "SELECT b.*, m.Title as MovieTitle, m.PosterURL, c.Name as CinemaName, s.StartTime " +
                      "FROM " + DatabaseConfig.TABLE_BOOKING + " b " +
                      "JOIN " + DatabaseConfig.TABLE_SHOW + " s ON b.ShowID = s.ShowID " +
                      "JOIN " + DatabaseConfig.TABLE_MOVIE + " m ON s.MovieID = m.MovieID " +
                      "JOIN " + DatabaseConfig.TABLE_CINEMA_HALL + " ch ON s.HallID = ch.HallID " +
                      "JOIN " + DatabaseConfig.TABLE_CINEMA + " c ON ch.CinemaID = c.CinemaID " +
                      "WHERE b.BookingID = ?";

        ResultSet rs = null;
        PreparedStatement statement = null;
        
        try {
            rs = executeQuery(query, bookingId);
            if (rs.next()) {
                return extractBookingFromResultSet(rs);
            }
        } finally {
            closeResources(rs, statement);
        }
        
        return null;
    }

    public boolean updateBookingStatus(int bookingId, String newStatus) throws SQLException {
        String query = "UPDATE " + DatabaseConfig.TABLE_BOOKING + 
                      " SET Status = ? WHERE BookingID = ?";
        
        try {
            int rowsAffected = executeUpdate(query, newStatus, bookingId);
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new SQLException("Failed to update booking status: " + e.getMessage(), e);
        }
    }

    public boolean deleteBooking(int bookingId) throws SQLException {
        String query = "DELETE FROM " + DatabaseConfig.TABLE_BOOKING + 
                      " WHERE BookingID = ?";
        
        try {
            int rowsAffected = executeUpdate(query, bookingId);
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new SQLException("Failed to delete booking: " + e.getMessage(), e);
        }
    }

    private Booking extractBookingFromResultSet(ResultSet rs) throws SQLException {
        Booking booking = new Booking();
        booking.setBookingID(rs.getInt("BookingID"));
        booking.setUserID(rs.getInt("UserID"));
        booking.setShowID(rs.getInt("ShowID"));
        booking.setBookingTime(rs.getTimestamp("BookingTime"));
        booking.setNumberOfSeats(rs.getInt("NumberOfSeats"));
        booking.setTotalPrice(rs.getBigDecimal("TotalPrice"));
        booking.setStatus(rs.getString("Status"));
        booking.setQrCodeData(rs.getString("QRCodeData"));
        
        // Additional display information
        booking.setMovieTitle(rs.getString("MovieTitle"));
        booking.setPosterURL(rs.getString("PosterURL"));
        booking.setCinemaName(rs.getString("CinemaName"));
        booking.setShowTime(rs.getTimestamp("StartTime").toString());
        
        return booking;
    }
} 