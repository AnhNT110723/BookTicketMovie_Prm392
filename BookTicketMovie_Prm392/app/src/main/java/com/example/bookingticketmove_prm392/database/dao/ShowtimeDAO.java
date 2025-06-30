package com.example.bookingticketmove_prm392.database.dao;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.bookingticketmove_prm392.models.Cinema;
import com.example.bookingticketmove_prm392.models.CinemaWithShowtimes;
import com.example.bookingticketmove_prm392.models.Showtime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.sql.Date;
import java.util.List;

public class ShowtimeDAO extends BaseDAO{
    private static final String TAG = "ShowTimeDAO";

    public List<Showtime> getShowtimesByCinemaIdAndMovieId(int cinemaId, int movieId) throws SQLException {
        List<Showtime> shows = new ArrayList<>();
        String query = "SELECT s.ShowID, s.HallID, s.StartTime, s.EndTime, s.MovieID " +
                "FROM Show s JOIN CinemaHall ch ON s.HallID = ch.HallID " +
                "WHERE ch.CinemaID = ? AND s.MovieID = ?";
        ResultSet rs = null;

        try {
            rs = executeQuery(query, cinemaId, movieId);
            while (rs.next()) {
                Showtime show = new Showtime();
                show.setShowtimeId(rs.getInt("ShowID"));
                show.setHallId(rs.getInt("HallID"));
                Timestamp startTimestamp = rs.getTimestamp("StartTime");
                Timestamp endTimestamp = rs.getTimestamp("EndTime");
                show.setStartTime(convertTimestampToLocalDateTime(startTimestamp));
                show.setEndTime(convertTimestampToLocalDateTime(endTimestamp));
                show.setMovieId(rs.getInt("MovieID"));
                shows.add(show);
            }
        } finally {
            closeResources(rs, null); // Không cần statement vì executeQuery đã quản lý
        }
        return shows;
    }

    public List<LocalDateTime> getAvailableDates() throws SQLException {
        List<LocalDateTime> dates = new ArrayList<>();
        String query = "SELECT DISTINCT CAST(StartTime AS DATE) AS ShowDate FROM Show ORDER BY ShowDate";
        ResultSet rs = null;

        try {
            rs = executeQuery(query);
            while (rs.next()) {
                Timestamp timestamp = rs.getTimestamp("ShowDate");
                if (timestamp != null) {
                    LocalDateTime dateTime = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        dateTime = convertTimestampToLocalDateTime(timestamp);
                    }
                    if (dateTime != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            dates.add(dateTime.toLocalDate().atStartOfDay());
                        }
                    }
                }
            }
        } finally {
            closeResources(rs, null);
        }
        return dates;
    }

    // Get showtimes by cinema and date
    public List<Showtime> getShowtimesByCinemaAndDate(int cinemaId, int movieId, LocalDate date) throws SQLException {
        List<Showtime> showtimes = new ArrayList<>();
        List<Integer> hallIds = getHallIdsByCinemaId(cinemaId);

        if (hallIds.isEmpty()) {
            return showtimes; // Trả về danh sách rỗng nếu không có phòng chiếu
        }

        // Tạo query động để lọc theo danh sách HallID
        StringBuilder queryBuilder = new StringBuilder("SELECT s.ShowID, s.HallID, s.StartTime, s.EndTime, s.MovieID " +
                "FROM Show s WHERE s.MovieID = ? AND CONVERT(date, s.StartTime) = ? AND s.HallID IN (");
        for (int i = 0; i < hallIds.size(); i++) {
            queryBuilder.append("?");
            if (i < hallIds.size() - 1) queryBuilder.append(",");
        }
        queryBuilder.append(") ORDER BY s.StartTime");

        ResultSet rs = null;
        try {
            // Chuẩn bị danh sách tham số
            List<Object> parameters = new ArrayList<>();
            parameters.add(movieId);
            String d = String.valueOf(date);
            java.sql.Date sqlDate = java.sql.Date.valueOf(d);
            parameters.add(sqlDate); // Thêm java.sql.Date thay vì LocalDate // Sử dụng LocalDate trực tiếp
            parameters.addAll(hallIds);

            rs = executeQuery(queryBuilder.toString(), parameters.toArray());
            while (rs.next()) {
                Showtime showtime = new Showtime();
                showtime.setShowtimeId(rs.getInt("ShowID"));
                showtime.setHallId(rs.getInt("HallID"));
                Timestamp startTimestamp = rs.getTimestamp("StartTime");
                Timestamp endTimestamp = rs.getTimestamp("EndTime");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    showtime.setStartTime(convertTimestampToLocalDateTime(startTimestamp));
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    showtime.setEndTime(convertTimestampToLocalDateTime(endTimestamp));
                }
                showtime.setMovieId(rs.getInt("MovieID"));
                showtimes.add(showtime);
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error fetching showtimes", e);
            throw e;
        } finally {
            closeResources(rs, null);
        }

        return showtimes;
    }

    // Phương thức phụ để lấy danh sách HallID theo CinemaID
    private List<Integer> getHallIdsByCinemaId(int cinemaId) throws SQLException {
        List<Integer> hallIds = new ArrayList<>();
        String query = "SELECT HallID FROM CinemaHall WHERE CinemaID = ?";
        ResultSet rs = null;

        try {
            rs = executeQuery(query, cinemaId);
            while (rs.next()) {
                hallIds.add(rs.getInt("HallID"));
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error fetching hall IDs for cinemaId=" + cinemaId, e);
            throw e;
        } finally {
            closeResources(rs, null);
        }

        return hallIds;
    }


// Phương thức chuyển đổi Timestamp sang LocalDateTime
    @RequiresApi(api = Build.VERSION_CODES.O)
    private LocalDateTime convertTimestampToLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }

            return new Date(timestamp.getTime()).toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
    }

}
