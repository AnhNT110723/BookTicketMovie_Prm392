package com.example.bookingticketmove_prm392.database.dao;

import com.example.bookingticketmove_prm392.database.DatabaseConfig;
import com.example.bookingticketmove_prm392.models.Booking;
import com.example.bookingticketmove_prm392.models.BookingSeat;
import com.example.bookingticketmove_prm392.models.Payment;

import java.math.BigDecimal;
import java.sql.Connection;
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


    //ADD BOOKING BY ANHNT
// Insert a new booking and return the generated BookingID
    public void createBooking(Booking booking, DatabaseTaskListener<Integer> listener) {
        new DatabaseTask<Integer>(listener) {
            @Override
            protected Integer doInBackground(Void... voids) {
                String query = "INSERT INTO Booking (UserID, ShowID, BookingTime, NumberOfSeats, TotalPrice, Status, QRCodeData) VALUES (?, ?, ?, ?, ?, ?, ?); SELECT SCOPE_IDENTITY() AS BookingID";
                PreparedStatement statement = null;
                ResultSet rs = null;
                try {
                    Connection conn = databaseConnection.getConnection();
                    statement = conn.prepareStatement(query);
                    statement.setInt(1, booking.getUserID());
                    statement.setInt(2, booking.getShowID());
                    statement.setTimestamp(3, new java.sql.Timestamp(booking.getBookingTime().getTime()));
                    statement.setInt(4, booking.getNumberOfSeats());
                    statement.setBigDecimal(5, booking.getTotalPrice());
                    statement.setString(6, booking.getStatus());
                    statement.setString(7, booking.getQrCodeData());
                    rs = statement.executeQuery();
                    if (rs.next()) {
                        return rs.getInt("BookingID");
                    }
                    return -1;
                } catch (SQLException e) {
                    exception = e;
                    return -1;
                } finally {
                    closeResources(rs, statement);
                }
            }
        }.execute();
    }
    // Insert booked seats
    // Insert booked seats
    public void createBookedSeats(List<BookingSeat> bookedSeats, DatabaseTaskListener<Boolean> listener) {
        new DatabaseTask<Boolean>(listener) {
            @Override
            protected Boolean doInBackground(Void... voids) {
                String query = "INSERT INTO BookedSeat (BookingID, SeatID, ShowID, IsReserved, ReservedByUserID, ReservationTimestamp) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement statement = null;
                try {
                    Connection conn = databaseConnection.getConnection();
                    statement = conn.prepareStatement(query);
                    for (BookingSeat bookedSeat : bookedSeats) {
                        statement.setInt(1, bookedSeat.getBookingId());
                        statement.setInt(2, bookedSeat.getSeatId());
                        statement.setInt(3, bookedSeat.getShowId());
                        statement.setBoolean(4, bookedSeat.isReserved());
                        statement.setInt(5, bookedSeat.getReservedByUserId());
                        statement.setTimestamp(6, new java.sql.Timestamp(bookedSeat.getReservationTimestamp().getTime()));
                        statement.addBatch();
                    }
                    statement.executeBatch();
                    return true;
                } catch (SQLException e) {
                    exception = e;
                    return false;
                } finally {
                    closeResources(null, statement);
                }
            }
        }.execute();
    }

    // Insert a payment
    public void createPayment(Payment payment, DatabaseTaskListener<Boolean> listener) {
        new DatabaseTask<Boolean>(listener) {
            @Override
            protected Boolean doInBackground(Void... voids) {
                String query = "INSERT INTO Payment (BookingID, Amount, PaymentTime, PaymentStatus, PaymentMethod, TransactionID) VALUES (?, ?, ?, ?, ?, ?)";
                try {
                    executeUpdate(query,
                            payment.getBookingId(),
                            payment.getAmount(),
                            new java.sql.Timestamp(payment.getPaymentTime().getTime()),
                            payment.getPaymentStatus(),
                            payment.getPaymentMethod(),
                            payment.getTransactionId());
                    return true;
                } catch (SQLException e) {
                    exception = e;
                    return false;
                }
            }
        }.execute();
    }

    // Get ShowID by showDate, startTime, and cinemaName
    public void getShowId(String showDate, String startTime, String cinemaName, DatabaseTaskListener<Integer> listener) {
        new DatabaseTask<Integer>(listener) {
            @Override
            protected Integer doInBackground(Void... voids) {
                String query = "SELECT ShowID FROM Show WHERE ShowDate = ? AND StartTime = ? AND CinemaID = (SELECT CinemaID FROM Cinema WHERE CinemaName = ?)";
                ResultSet rs = null;
                try {
                    rs = executeQuery(query, showDate, startTime, cinemaName);
                    if (rs.next()) {
                        return rs.getInt("ShowID");
                    }
                    return -1;
                } catch (SQLException e) {
                    exception = e;
                    return -1;
                } finally {
                    closeResources(rs, null);
                }
            }
        }.execute();
    }

    // Get SeatID by seatName and cinemaName
    public void getSeatId(String seatName, String cinemaName, DatabaseTaskListener<Integer> listener) {
        new DatabaseTask<Integer>(listener) {
            @Override
            protected Integer doInBackground(Void... voids) {
                String query = "SELECT SeatID FROM Seat WHERE SeatName = ? AND CinemaID = (SELECT CinemaID FROM Cinema WHERE CinemaName = ?)";
                ResultSet rs = null;
                try {
                    rs = executeQuery(query, seatName, cinemaName);
                    if (rs.next()) {
                        return rs.getInt("SeatID");
                    }
                    return -1;
                } catch (SQLException e) {
                    exception = e;
                    return -1;
                } finally {
                    closeResources(rs, null);
                }
            }
        }.execute();
    }

} 