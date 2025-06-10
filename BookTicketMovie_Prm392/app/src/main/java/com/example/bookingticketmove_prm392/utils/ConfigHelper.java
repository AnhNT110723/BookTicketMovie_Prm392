package com.example.bookingticketmove_prm392.utils;

import com.example.bookingticketmove_prm392.database.DatabaseConfig;

public class ConfigHelper {
      /**
     * Check if database configuration is set up
     */
    public static boolean isDatabaseConfigured() {
        return !DatabaseConfig.DB_URL.contains("YOUR_SERVER_IP") &&
               !DatabaseConfig.DB_USERNAME.equals("YOUR_USERNAME") &&
               !DatabaseConfig.DB_PASSWORD.equals("YOUR_PASSWORD");
    }
      /**
     * Get configuration status message
     */
    public static String getConfigurationStatus() {
        if (isDatabaseConfigured()) {
            return "✅ Database configuration is set up:\n" +
                   "Server: " + extractServerFromUrl() + "\n" +
                   "Database: " + DatabaseConfig.DATABASE_NAME + "\n" +
                   "Username: " + DatabaseConfig.DB_USERNAME;
        } else {
            return "⚠️ Database configuration needed:\n" +
                   "• Update server IP in DatabaseConfig.java\n" +
                   "• Set username and password\n" +
                   "• Ensure SQL Server is running";
        }
    }
    
    /**
     * Extract server info from URL for display
     */
    private static String extractServerFromUrl() {
        String url = DatabaseConfig.DB_URL;
        if (url.contains("localhost")) {
            return "localhost:1433 (Local SQL Server)";
        } else if (url.contains("://")) {
            String[] parts = url.split("://");
            if (parts.length > 1) {
                String serverPart = parts[1].split("/")[0];
                return serverPart;
            }
        }
        return "Unknown";
    }
    
    /**
     * Get current configuration details (for debugging)
     */
    public static String getCurrentConfig() {
        return "Current Configuration:\n" +
               "Driver: " + DatabaseConfig.DB_DRIVER + "\n" +
               "URL: " + DatabaseConfig.DB_URL + "\n" +
               "Username: " + DatabaseConfig.DB_USERNAME + "\n" +
               "Password: " + (DatabaseConfig.DB_PASSWORD.equals("YOUR_PASSWORD") ? "Not Set" : "Set");
    }
}
