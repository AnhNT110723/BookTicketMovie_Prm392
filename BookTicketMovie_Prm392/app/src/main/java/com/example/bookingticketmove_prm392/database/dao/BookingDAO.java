package com.example.bookingticketmove_prm392.database.dao;

import android.os.AsyncTask;
import android.util.Log;

import com.example.bookingticketmove_prm392.database.DatabaseConnection;
import com.example.bookingticketmove_prm392.models.Booking;
import com.example.bookingticketmove_prm392.models.Seat;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BookingDAO extends BaseDAO {

    public interface GetBookingsListener {
        void onSuccess(List<Booking> bookings);
        void onError(Exception e);
    }

    public void getBookingsByUserId(String userId, GetBookingsListener listener) {
        new GetBookingsTask(userId, listener).execute();
    }

    private static class GetBookingsTask extends AsyncTask<Void, Void, List<Booking>> {
        private final String userId;
        private final GetBookingsListener listener;
        private Exception exception;

        public GetBookingsTask(String userId, GetBookingsListener listener) {
            this.userId = userId;
            this.listener = listener;
        }

        @Override
        protected List<Booking> doInBackground(Void... voids) {
            List<Booking> bookings = new ArrayList<>();
            String sql = "SELECT " +
                         "    b.BookingID, " +
                         "    m.Title AS MovieTitle, " +
                         "    c.Name AS CinemaName, " +
                         "    ch.Name AS HallName, " +
                         "    s.StartTime, " +
                         "    b.NumberOfSeats, " +
                         "    b.TotalPrice, " +
                         "    b.Status, " +
                         "    m.PosterURL, " +
                         "    b.QRCodeData " +
                         "FROM " +
                         "    Booking b " +
                         "JOIN " +
                         "    Show s ON b.ShowID = s.ShowID " +
                         "JOIN " +
                         "    Movie m ON s.MovieID = m.MovieID " +
                         "JOIN " +
                         "    CinemaHall ch ON s.HallID = ch.HallID " +
                         "JOIN " +
                         "    Cinema c ON ch.CinemaID = c.CinemaID " +
                         "WHERE " +
                         "    b.UserID = ? " +
                         "ORDER BY " +
                         "    b.BookingTime DESC";

            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, Integer.parseInt(userId));
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int bookingId = rs.getInt("BookingID");
                        String movieTitle = rs.getString("MovieTitle");
                        String cinemaName = rs.getString("CinemaName");
                        String hallName = rs.getString("HallName");
                        Date showtime = rs.getTimestamp("StartTime");
                        int numberOfSeats = rs.getInt("NumberOfSeats");
                        BigDecimal totalPrice = rs.getBigDecimal("TotalPrice");
                        String status = rs.getString("Status");
                        String posterUrl = rs.getString("PosterURL");
                        String qrcode = rs.getString("QRCodeData");

                        Booking booking = new Booking(bookingId, movieTitle, cinemaName, hallName, showtime, numberOfSeats, totalPrice, status, posterUrl, qrcode);
                        bookings.add(booking);
                    }
                }
            } catch (SQLException e) {
                exception = e;
                Log.e("GetBookingsTask", "Database error", e);
            }
            return bookings;
        }

        @Override
        protected void onPostExecute(List<Booking> bookings) {
            if (exception != null) {
                listener.onError(exception);
            } else {
                listener.onSuccess(bookings);
            }
        }
    }

    public interface GetSeatsListener {
        void onSuccess(List<Seat> seats);
        void onError(Exception e);
    }

    public void getSeatsForBooking(int bookingId, GetSeatsListener listener) {
        new GetSeatsTask(bookingId, listener).execute();
    }

    private static class GetSeatsTask extends AsyncTask<Void, Void, List<Seat>> {
        private final int bookingId;
        private final GetSeatsListener listener;
        private Exception exception;

        public GetSeatsTask(int bookingId, GetSeatsListener listener) {
            this.bookingId = bookingId;
            this.listener = listener;
        }

        @Override
        protected List<Seat> doInBackground(Void... voids) {
            List<Seat> seats = new ArrayList<>();
            String sql = "SELECT s.RowNumber, s.ColumnNumber, s.SeatType " +
                         "FROM BookedSeat bs " +
                         "JOIN Seat s ON bs.SeatID = s.SeatID " +
                         "WHERE bs.BookingID = ?";
            
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, bookingId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String row = rs.getString("RowNumber");
                        int number = rs.getInt("ColumnNumber");
                        String type = rs.getString("SeatType");
                        seats.add(new Seat(row, number, type));
                    }
                }
            } catch (SQLException e) {
                exception = e;
                Log.e("GetSeatsTask", "Database error", e);
            }
            return seats;
        }

        @Override
        protected void onPostExecute(List<Seat> seats) {
            if (exception != null) {
                listener.onError(exception);
            } else {
                listener.onSuccess(seats);
            }
        }
    }
} 