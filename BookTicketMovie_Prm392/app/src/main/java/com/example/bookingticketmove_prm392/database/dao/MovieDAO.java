package com.example.bookingticketmove_prm392.database.dao;

import com.example.bookingticketmove_prm392.models.Movie;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MovieDAO extends BaseDAO {    // Get all active movies
    public List<Movie> getAllMovies() throws SQLException {
        List<Movie> movies = new ArrayList<>();
        String query = "SELECT * FROM Movie ORDER BY ReleaseDate DESC";

        ResultSet rs = null;
        PreparedStatement statement = null;
        
        try {
            rs = executeQuery(query);
            while (rs.next()) {
                Movie movie = mapResultSetToMovie(rs);
                movies.add(movie);
            }
        } finally {
            closeResources(rs, statement);
        }

        return movies;
    }// Get trending movies
    public List<Movie> getTrendingMovies() throws SQLException {
        List<Movie> movies = new ArrayList<>();
        String query = "SELECT TOP 6 * FROM Movie WHERE IsActive = 1 AND IsTrending = 1 ORDER BY Rating DESC, ReleaseDate DESC";

        ResultSet rs = null;
        PreparedStatement statement = null;
        
        try {
            rs = executeQuery(query);
            while (rs.next()) {
                Movie movie = mapResultSetToMovie(rs);
                movies.add(movie);
            }
        } finally {
            closeResources(rs, statement);
        }

        return movies;
    }    
    
    // Get all trending movies (for browse page)
    public List<Movie> getAllTrendingMovies() throws SQLException {
        List<Movie> movies = new ArrayList<>();
        String query = "SELECT * FROM Movie WHERE IsActive = 1 AND IsTrending = 1 ORDER BY Rating DESC, ReleaseDate DESC";

        ResultSet rs = null;
        PreparedStatement statement = null;
        
        try {
            rs = executeQuery(query);
            while (rs.next()) {
                Movie movie = mapResultSetToMovie(rs);
                movies.add(movie);
            }
        } finally {
            closeResources(rs, statement);
        }

        return movies;
    }    // Get featured movies (latest releases)
    public List<Movie> getFeaturedMovies() throws SQLException {
        List<Movie> movies = new ArrayList<>();
        String query = "SELECT TOP 5 * FROM Movie WHERE IsActive = 1 AND IsTrending = 0 ORDER BY ReleaseDate DESC";

        ResultSet rs = null;
        PreparedStatement statement = null;
        
        try {
            rs = executeQuery(query);
            while (rs.next()) {
                Movie movie = mapResultSetToMovie(rs);
                movies.add(movie);
            }
        } finally {
            closeResources(rs, statement);
        }

        return movies;
    }    
    
    // Get all featured movies (for browse page)
    public List<Movie> getAllFeaturedMovies() throws SQLException {
        List<Movie> movies = new ArrayList<>();
        String query = "SELECT * FROM Movie WHERE IsActive = 1 AND IsTrending = 0 ORDER BY ReleaseDate DESC";

        ResultSet rs = null;
        PreparedStatement statement = null;
        
        try {
            rs = executeQuery(query);
            while (rs.next()) {
                Movie movie = mapResultSetToMovie(rs);
                movies.add(movie);
            }
        } finally {
            closeResources(rs, statement);
        }

        return movies;
    }// Get movie by ID
    public Movie getMovieById(int movieId) throws SQLException {
        String query = "SELECT * FROM Movie WHERE MovieID = ?";
        
        ResultSet rs = null;
        PreparedStatement statement = null;
        
        try {
            rs = executeQuery(query, movieId);
            if (rs.next()) {
                return mapResultSetToMovie(rs);
            }
        } finally {
            closeResources(rs, statement);
        }
        
        return null;
    }    // Search movies by title or genre
    public List<Movie> searchMovies(String searchQuery) throws SQLException {
        List<Movie> movies = new ArrayList<>();
        String query = "SELECT * FROM Movie WHERE IsActive = 1 AND (Title LIKE ? OR Genre LIKE ?) ORDER BY Rating DESC";

        ResultSet rs = null;
        PreparedStatement statement = null;
        
        try {
            String searchPattern = "%" + searchQuery + "%";
            rs = executeQuery(query, searchPattern, searchPattern);
            while (rs.next()) {
                Movie movie = mapResultSetToMovie(rs);
                movies.add(movie);
            }
        } finally {
            closeResources(rs, statement);
        }

        return movies;
    }    // Get movies by genre
    public List<Movie> getMoviesByGenre(String genre) throws SQLException {
        List<Movie> movies = new ArrayList<>();
        String query = "SELECT * FROM Movie WHERE IsActive = 1 AND Genre LIKE ? ORDER BY Rating DESC";

        ResultSet rs = null;
        PreparedStatement statement = null;
        
        try {
            String genrePattern = "%" + genre + "%";
            rs = executeQuery(query, genrePattern);
            while (rs.next()) {
                Movie movie = mapResultSetToMovie(rs);
                movies.add(movie);
            }
        } finally {
            closeResources(rs, statement);
        }

        return movies;
    }// Add new movie
    public boolean addMovie(Movie movie) throws SQLException {
        String query = "INSERT INTO [Movie] (title, description, genre, duration, director, releaseDate, rating, posterUrl, trailerUrl, price, isActive, isTrending, createdDate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        int result = executeUpdate(query,
            movie.getTitle(),
            movie.getDescription(),
            movie.getGenre(),
            movie.getDuration(),
            movie.getDirector(),
            new java.sql.Date(movie.getReleaseDate().getTime()),
            movie.getRating(),
            movie.getPosterUrl(),
            movie.getTrailerUrl(),
            movie.getPrice(),
            movie.isActive() ? 1 : 0,
            movie.isTrending() ? 1 : 0,
            new java.sql.Date(movie.getCreatedDate().getTime())
        );

        return result > 0;
    }    // Update movie
    public boolean updateMovie(Movie movie) throws SQLException {
        String query = "UPDATE [Movie] SET title = ?, description = ?, genre = ?, duration = ?, director = ?, releaseDate = ?, rating = ?, posterUrl = ?, trailerUrl = ?, price = ?, isActive = ?, isTrending = ? WHERE movieId = ?";

        int result = executeUpdate(query,
            movie.getTitle(),
            movie.getDescription(),
            movie.getGenre(),
            movie.getDuration(),
            movie.getDirector(),
            new java.sql.Date(movie.getReleaseDate().getTime()),
            movie.getRating(),
            movie.getPosterUrl(),
            movie.getTrailerUrl(),
            movie.getPrice(),
            movie.isActive() ? 1 : 0,
            movie.isTrending() ? 1 : 0,
            movie.getMovieId()
        );

        return result > 0;
    }    // Delete movie (soft delete)
    public boolean deleteMovie(int movieId) throws SQLException {
        String query = "UPDATE [Movie] SET isActive = 0 WHERE movieId = ?";

        int result = executeUpdate(query, movieId);
        return result > 0;
    }// Get movies by cinemaId
    public List<Movie> getMoviesByCinemaId(int cinemaId) throws SQLException {
        List<Movie> movies = new ArrayList<>();
        String query = "SELECT DISTINCT m.* FROM Movie m " +
                "JOIN Show s ON m.MovieID = s.MovieID " +
                "JOIN CinemaHall ch ON s.HallID = ch.HallID " +
                "WHERE ch.CinemaID = ? ORDER BY m.Title";
        ResultSet rs = null;
        PreparedStatement statement = null;
        try {
            rs = executeQuery(query, cinemaId);
            while (rs.next()) {
                Movie movie = mapResultSetToMovie(rs);
                movies.add(movie);
            }
        } finally {
            closeResources(rs, statement);
        }
        return movies;
    }// Helper method to map ResultSet to Movie object
    private Movie mapResultSetToMovie(ResultSet rs) throws SQLException {
        Movie movie = new Movie();
        movie.setMovieId(rs.getInt("MovieID"));
        movie.setTitle(rs.getString("Title"));
        movie.setDescription(rs.getString("Description"));
        movie.setGenre(rs.getString("Genre"));
        movie.setDuration(rs.getInt("Duration"));
        movie.setDirector(rs.getString("Director"));
        movie.setReleaseDate(rs.getDate("ReleaseDate"));
        movie.setRating(rs.getDouble("Rating"));
        movie.setPosterUrl(rs.getString("PosterURL"));
        movie.setTrailerUrl(rs.getString("TrailerURL"));
        movie.setPrice(rs.getDouble("Price"));
        movie.setActive(rs.getBoolean("IsActive"));
        movie.setTrending(rs.getBoolean("IsTrending"));
        movie.setCreatedDate(rs.getDate("CreatedDate"));
        return movie;
    }
}
