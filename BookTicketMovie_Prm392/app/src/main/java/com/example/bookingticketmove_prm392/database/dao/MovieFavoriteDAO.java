package com.example.bookingticketmove_prm392.database.dao;



import com.example.bookingticketmove_prm392.models.MovieFavorite;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class MovieFavoriteDAO extends BaseDAO{


    // Get all list movie favorite by UserId and MovieId
    public MovieFavorite getMovieFavorites(int userId, int movieId) throws SQLException {
        MovieFavorite movieFavorite = new MovieFavorite();
        String query = "SELECT * FROM MovieFavorite WHERE UserId = ? AND MovieId = ?";

        ResultSet rs = null;
        PreparedStatement statement = null;

        try{
            rs = executeQuery(query, userId, movieId);
            if (rs.next()){
                movieFavorite = mapResultSetToMovieFavorite(rs);
                return movieFavorite;
            }

        }finally {
            closeResources(rs, statement);
        }
        return null;
    }

    //Add movie favorite
    public boolean addMovieFavorite(MovieFavorite mf) throws SQLException{
        String query = "INSERT INTO MovieFavorite (UserId, MovieId) VALUES (?, ?)";
        int result = executeUpdate(query, mf.getUserId(), mf.getMovieId());
        return result > 0;
    }

    //Delete movie favorite
    public boolean deleteMovieFavorite(int userId, int movieId) throws SQLException{
        String query = "DELETE FROM MovieFavorite WHERE UserId = ? AND MovieId = ?";
        int result = executeUpdate(query, userId, movieId);
        return result > 0;
    }
    

    private MovieFavorite mapResultSetToMovieFavorite(ResultSet rs) throws SQLException {
        MovieFavorite mf = new MovieFavorite();
        mf.setMovieFavoriteId(rs.getInt("MovieFavoriteId"));
        mf.setUserId(rs.getInt("UserId"));
        mf.setMovieId(rs.getInt("MovieId"));
        return mf;
    }
}
