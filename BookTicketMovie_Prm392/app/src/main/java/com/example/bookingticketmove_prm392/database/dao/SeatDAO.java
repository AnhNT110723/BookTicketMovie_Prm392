package com.example.bookingticketmove_prm392.database.dao;

import android.util.Log;

import com.example.bookingticketmove_prm392.Enums.SeatType;
import com.example.bookingticketmove_prm392.models.Seat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SeatDAO extends BaseDAO{


    public List<Seat> getSeatsForBooking(int hallId, int showId) throws SQLException {
        List<Seat> seats = new ArrayList<>();
        String sql = "SELECT s.SeatID, s.HallID, s.RowNumber, s.ColumnNumber, s.SeatType, " +
                "CASE WHEN bs.SeatID IS NOT NULL THEN 'SOLD' ELSE 'AVAILABLE' END AS SeatStatus " +
                "FROM [MovieTicketBookingSystem].[dbo].[Seat] s " +
                "LEFT JOIN [MovieTicketBookingSystem].[dbo].[BookedSeat] bs " +
                "ON s.SeatID = bs.SeatID AND bs.ShowID = ? " +
                "WHERE s.HallID = ? " +
                "ORDER BY s.RowNumber, s.ColumnNumber";

        ResultSet rs = null;
        try {
            rs = executeQuery(sql, showId, hallId);

            while (rs.next()) {
                int seatID = rs.getInt("SeatID");
                int fetchedHallID = rs.getInt("HallID");
                String rowNumber = rs.getString("RowNumber");
                int columnNumber = rs.getInt("ColumnNumber");
                String seatTypeString = rs.getString("SeatType");
                String status = rs.getString("SeatStatus");
                SeatType seatType = SeatType.fromString(seatTypeString);

                if (seatType != null) {
                    seats.add(new Seat(seatID, fetchedHallID, rowNumber, columnNumber, seatType, status));
                } else {
                    Log.e(TAG, "Unknown SeatType: " + seatTypeString + " for SeatID: " + seatID);
                }
            }
        } finally {
            closeResources(rs, null); // Chỉ cần đóng ResultSet ở đây
            // PreparedStatement được quản lý bởi executeQuery trong BaseDAO
        }
        return seats;
    }

    public static class GetSeatsForBookingTask extends DatabaseTask<List<Seat>> {
        private int hallId;
        private int showId; // Thêm showId
        private SeatDAO seatDAO;

        public GetSeatsForBookingTask(int hallId, int showId, BaseDAO.DatabaseTaskListener<List<Seat>> listener) {
            super(listener);
            this.hallId = hallId;
            this.showId = showId;
            this.seatDAO = new SeatDAO();
        }

        @Override
        protected List<Seat> doInBackground(Void... voids) {
            try {
                return seatDAO.getSeatsForBooking(hallId, showId); // Gọi phương thức mới
            } catch (Exception e) {
                exception = e;
                Log.e(TAG, "Error fetching seats for booking: " + e.getMessage(), e);
                return null;
            }
        }
    }
}
