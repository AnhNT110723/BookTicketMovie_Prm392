package com.example.bookingticketmove_prm392.database;

import com.example.bookingticketmove_prm392.BuildConfig;

public class DatabaseConfig {
    // Database configuration constants
    public static final String DB_DRIVER = "net.sourceforge.jtds.jdbc.Driver";
    
    // Database connection details from BuildConfig (secure)
    private static final String DB_HOST = BuildConfig.DB_HOST;
    private static final String DB_HOST_FALLBACK = BuildConfig.DB_HOST_FALLBACK;
    private static final String DB_PORT = BuildConfig.DB_PORT;
    private static final String DB_NAME = BuildConfig.DB_NAME;
    private static final String DB_USERNAME = BuildConfig.DB_USERNAME;
    private static final String DB_PASSWORD = BuildConfig.DB_PASSWORD;
    
    // Construct URLs dynamically from secure config
    public static final String DB_URL = String.format("jdbc:jtds:sqlserver://%s:%s/%s", 
                                                      DB_HOST, DB_PORT, DB_NAME);
    public static final String DB_URL_FALLBACK = String.format("jdbc:jtds:sqlserver://%s:%s/%s", 
                                                               DB_HOST_FALLBACK, DB_PORT, DB_NAME);
    
    // Connection timeout settings
    public static final int CONNECTION_TIMEOUT = 30; // seconds
    public static final int SOCKET_TIMEOUT = 60; // seconds
    
    // Connection pool settings (for future implementation)
    public static final int MAX_POOL_SIZE = 10;
    public static final int MIN_POOL_SIZE = 2;
    
    // Database schema info
    public static final String DATABASE_NAME = DB_NAME;
    
    // Getters for secure access (if needed elsewhere)
    public static String getDbUsername() {
        return DB_USERNAME;
    }
    
    public static String getDbPassword() {
        return DB_PASSWORD;
    }
    
    public static String getDbHost() {
        return DB_HOST;
    }
    
    public static String getDbPort() {
        return DB_PORT;
    }    
    // Table names - matching your database schema
    public static final String TABLE_USER = "[User]";
    public static final String TABLE_ROLE = "Role";
    public static final String TABLE_MOVIE = "Movie";
    public static final String TABLE_CINEMA = "Cinema";
    public static final String TABLE_CINEMA_HALL = "CinemaHall";
    public static final String TABLE_SHOW = "Show";
    public static final String TABLE_BOOKING = "Booking";
    public static final String TABLE_SEAT = "Seat";
    public static final String TABLE_BOOKED_SEAT = "BookedSeat";
    public static final String TABLE_PAYMENT = "Payment";
    public static final String TABLE_VOTE = "Vote";
    public static final String TABLE_COMMENT = "Comment";
    public static final String TABLE_CITY = "City";
    public static final String TABLE_GENRE = "Genre";
    public static final String TABLE_PERSON = "Person";
    public static final String TABLE_MOVIE_DIRECTOR = "MovieDirector";
    public static final String TABLE_MOVIE_ACTOR = "MovieActor";
    public static final String TABLE_MOVIE_GENRE = "MovieGenre";
    public static final String TABLE_MOVIE_FAVORITE = "MovieFavorite";
    public static final String TABLE_TOKENS = "Tokens";
}
