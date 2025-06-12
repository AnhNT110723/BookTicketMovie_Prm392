package com.example.bookingticketmove_prm392.database;

public class DatabaseConfig {
    // Database configuration constants
    public static final String DB_DRIVER = "net.sourceforge.jtds.jdbc.Driver";    // Primary: Actual IP address, Fallback: 10.0.2.2 for emulator
    public static final String DB_URL = "jdbc:jtds:sqlserver://10.33.68.159:1433/MovieTicketBookingSystem";
    public static final String DB_URL_FALLBACK = "jdbc:jtds:sqlserver://10.0.2.2:1433/MovieTicketBookingSystem";
    public static final String DB_USERNAME = "sa";
    public static final String DB_PASSWORD = "123";
    
    // Connection timeout settings
    public static final int CONNECTION_TIMEOUT = 30; // seconds
    public static final int SOCKET_TIMEOUT = 60; // seconds
    
    // Connection pool settings (for future implementation)
    public static final int MAX_POOL_SIZE = 10;
    public static final int MIN_POOL_SIZE = 2;
    
    // Database schema info
    public static final String DATABASE_NAME = "MovieTicketBookingSystem";
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
