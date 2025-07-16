package com.example.bookingticketmove_prm392.database.dao;

import com.example.bookingticketmove_prm392.models.Show;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class ShowDAO extends BaseDAO {
    // Lấy danh sách ngày chiếu distinct của 1 phim tại 1 rạp
    public List<Date> getShowDatesByCinemaAndMovie(int cinemaId, int movieId) throws SQLException {
        List<Date> dates = new ArrayList<>();
        String query = "SELECT DISTINCT CAST(s.StartTime AS DATE) as ShowDate " +
                "FROM Show s " +
                "JOIN CinemaHall ch ON s.HallID = ch.HallID " +
                "WHERE ch.CinemaID = ? AND s.MovieID = ? " +
                "ORDER BY ShowDate ASC";
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            rs = executeQuery(query, cinemaId, movieId);
            while (rs.next()) {
                dates.add(rs.getDate("ShowDate"));
            }
        } finally {
            closeResources(rs, statement);
        }
        return dates;
    }

    // Lấy các suất chiếu theo ngày
    public List<Show> getShowsByCinemaMovieAndDate(int cinemaId, int movieId, Date date) throws SQLException {
        List<Show> shows = new ArrayList<>();
        String query = "SELECT s.* FROM Show s " +
                "JOIN CinemaHall ch ON s.HallID = ch.HallID " +
                "WHERE ch.CinemaID = ? AND s.MovieID = ? " +
                "AND CAST(s.StartTime AS DATE) = ? " +
                "ORDER BY s.StartTime ASC";
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            rs = executeQuery(query, cinemaId, movieId, date);
            while (rs.next()) {
                Show show = new Show();
                show.setShowId(rs.getInt("ShowID"));
                show.setMovieId(rs.getInt("MovieID"));
                show.setHallId(rs.getInt("HallID"));
                show.setStartTime(rs.getTimestamp("StartTime"));
                show.setEndTime(rs.getTimestamp("EndTime"));
                show.setPrice(rs.getDouble("TicketPrice"));
                shows.add(show);
            }
        } finally {
            closeResources(rs, statement);
        }
        return shows;
    }
} 