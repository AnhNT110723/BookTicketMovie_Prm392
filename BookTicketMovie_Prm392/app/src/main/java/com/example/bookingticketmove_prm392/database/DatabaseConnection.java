package com.example.bookingticketmove_prm392.database;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseConnection {
    private static final String TAG = "DatabaseConnection";
    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        // Private constructor to prevent direct instantiation
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }    /**
     * Establishes connection to SQL Server database
     * This should be called from a background thread
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Log.d(TAG, "Creating new database connection...");
                Log.d(TAG, "Driver: " + DatabaseConfig.DB_DRIVER);
                Log.d(TAG, "URL: " + DatabaseConfig.DB_URL);
                Log.d(TAG, "Username: " + DatabaseConfig.DB_USERNAME);
                
                // Load the JTDS driver
                Class.forName(DatabaseConfig.DB_DRIVER);
                Log.d(TAG, "JTDS Driver loaded successfully");
                  // Set connection properties
                Properties props = new Properties();
                props.setProperty("user", DatabaseConfig.getDbUsername());
                props.setProperty("password", DatabaseConfig.getDbPassword());
                props.setProperty("loginTimeout", String.valueOf(DatabaseConfig.CONNECTION_TIMEOUT));
                props.setProperty("socketTimeout", String.valueOf(DatabaseConfig.SOCKET_TIMEOUT));
                props.setProperty("TDS", "8.0"); // Specify TDS version
                props.setProperty("ssl", "off"); // Disable SSL for localhost
                props.setProperty("trustServerCertificate", "true"); // Trust server certificateLog.d(TAG, "Attempting to connect with properties...");
                Log.d(TAG, "=== CONNECTING TO: " + DatabaseConfig.DB_URL + " ===");
                
                // Try primary connection first
                try {
                    connection = DriverManager.getConnection(DatabaseConfig.DB_URL, props);
                    Log.d(TAG, "Primary connection successful!");
                } catch (SQLException e) {
                    Log.w(TAG, "Primary connection failed, trying fallback: " + e.getMessage());
                    Log.d(TAG, "=== TRYING FALLBACK: " + DatabaseConfig.DB_URL_FALLBACK + " ===");
                    connection = DriverManager.getConnection(DatabaseConfig.DB_URL_FALLBACK, props);
                    Log.d(TAG, "Fallback connection successful!");
                }
                  if (connection != null && !connection.isClosed()) {
                    Log.d(TAG, "Database connection established successfully");
                    Log.d(TAG, "Connection established and ready to use");
                } else {
                    Log.e(TAG, "Connection created but is null or closed");
                    return null;
                }
            }
            return connection;
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "JTDS Driver not found - check if jtds-1.3.1.jar is in libs folder", e);
            return null;
        } catch (SQLException e) {
            Log.e(TAG, "SQL Exception while connecting to database", e);
            Log.e(TAG, "Error Code: " + e.getErrorCode());
            Log.e(TAG, "SQL State: " + e.getSQLState());
            Log.e(TAG, "Error Message: " + e.getMessage());
            
            // Common SQL Server connection issues
            if (e.getMessage().contains("Connection refused")) {
                Log.e(TAG, "HINT: SQL Server might not be running or port 1433 is blocked");
            } else if (e.getMessage().contains("Login failed")) {
                Log.e(TAG, "HINT: Check username/password or SQL Server authentication mode");
            } else if (e.getMessage().contains("Cannot open database")) {
                Log.e(TAG, "HINT: Database 'MovieTicketBookingSystem' might not exist");
            }
            
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected exception while connecting to database", e);
            return null;
        }
    }

    /**
     * Closes the database connection
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                Log.d(TAG, "Database connection closed");
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error closing database connection", e);
        }
    }    /**
     * Tests the database connection using modern threading
     */
    public static void testConnection(DatabaseConnectionListener listener) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());
        
        executor.execute(() -> {
            String errorMessage = null;
            boolean success = false;
            
            try {
                Log.d(TAG, "Starting database connection test...");
                DatabaseConnection dbConnection = DatabaseConnection.getInstance();
                Connection conn = dbConnection.getConnection();
                
                if (conn != null && !conn.isClosed()) {
                    Log.d(TAG, "Database connection test successful");
                    success = true;
                } else {
                    errorMessage = "Failed to establish connection - connection is null or closed";
                    Log.e(TAG, errorMessage);
                }
            } catch (Exception e) {
                Log.e(TAG, "Connection test failed", e);
                errorMessage = e.getMessage();
            }
            
            // Post results back to main thread
            final boolean finalSuccess = success;
            final String finalErrorMessage = errorMessage;
            
            mainHandler.post(() -> {
                if (listener != null) {
                    if (finalSuccess) {
                        listener.onConnectionSuccess();
                    } else {
                        listener.onConnectionFailed(finalErrorMessage);
                    }
                }
            });
        });
    }

    /**
     * Interface for database connection callbacks
     */
    public interface DatabaseConnectionListener {
        void onConnectionSuccess();
        void onConnectionFailed(String error);
    }
}
