package com.example.bookingticketmove_prm392.database.dao;

import com.example.bookingticketmove_prm392.models.CinemaHall;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HallCinemaDAO extends BaseDAO{

    public List<CinemaHall> getAllCinemaHall(int cinemaId) throws SQLException {
        List<CinemaHall> cinemaHallList = new ArrayList<>();
        String query = "SELECT * FROM CinemaHall WHERE CinemaId = ?";

        ResultSet rs = null;
        PreparedStatement statement = null;
        try{
            rs = executeQuery(query, cinemaId);
            while (rs.next()){
                CinemaHall cinemaHall = mapResultSetToCinemaHall(rs);
                cinemaHallList.add(cinemaHall);
            }

        }finally {
            closeResources(rs, statement);
        }

        return cinemaHallList;
    }

    //add hall cinema
    public boolean addCinemaHall(CinemaHall cinemaHall) throws SQLException{
        String query = "INSERT INTO CinemaHall (CinemaId, Name, TotalSeats) VALUES (?, ?, ?)";
        int result = executeUpdate(query, cinemaHall.getCinemaId(), cinemaHall.getName(), cinemaHall.getTotalSeats());
        return result > 0;
    }

    //delete hall cinema
    public boolean deleteCinemaHall(int cinemaHallId) throws SQLException{
        String query = "DELETE FROM CinemaHall WHERE HallID = ?";
        int result = executeUpdate(query, cinemaHallId);
        return result > 0;
    }



    private CinemaHall mapResultSetToCinemaHall(ResultSet rs) throws SQLException {
        CinemaHall cinemaHall = new CinemaHall();
        cinemaHall.setCinemaId(rs.getInt("CinemaId"));
        cinemaHall.setName(rs.getString("Name"));
        cinemaHall.setTotalSeats(rs.getInt("TotalSeats"));
        return cinemaHall;
    }
}
