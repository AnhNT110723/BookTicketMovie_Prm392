package com.example.bookingticketmove_prm392.database.dao;

import android.os.AsyncTask;
import android.util.Log;
import com.example.bookingticketmove_prm392.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class BaseDAO {
    protected static final String TAG = "BaseDAO";
    protected DatabaseConnection databaseConnection;

    public BaseDAO() {
        this.databaseConnection = DatabaseConnection.getInstance();
    }

    /**
     * Execute a query and return ResultSet
     * Should be called from background thread
     */
    protected ResultSet executeQuery(String sql, Object... parameters) throws SQLException {
        Connection conn = databaseConnection.getConnection();
        if (conn == null) {
            throw new SQLException("Database connection is null");
        }

        PreparedStatement statement = conn.prepareStatement(sql);
        
        // Set parameters
        for (int i = 0; i < parameters.length; i++) {
            statement.setObject(i + 1, parameters[i]);
        }

        Log.d(TAG, "Executing query: " + sql);
        return statement.executeQuery();
    }

    /**
     * Execute an update (INSERT, UPDATE, DELETE) query
     * Should be called from background thread
     */
    protected int executeUpdate(String sql, Object... parameters) throws SQLException {
        Connection conn = databaseConnection.getConnection();
        if (conn == null) {
            throw new SQLException("Database connection is null");
        }

        PreparedStatement statement = conn.prepareStatement(sql);
        
        // Set parameters
        for (int i = 0; i < parameters.length; i++) {
            statement.setObject(i + 1, parameters[i]);
        }

        Log.d(TAG, "Executing update: " + sql);
        int result = statement.executeUpdate();
        statement.close();
        return result;
    }

    /**
     * Close ResultSet and Statement safely
     */
    protected void closeResources(ResultSet rs, PreparedStatement statement) {
        try {
            if (rs != null) rs.close();
            if (statement != null) statement.close();
        } catch (SQLException e) {
            Log.e(TAG, "Error closing database resources", e);
        }
    }    /**
     * Base AsyncTask for database operations
     * Enhanced for MVC pattern integration
     */
    public abstract static class DatabaseTask<T> extends AsyncTask<Void, Void, T> {
        protected Exception exception;
        public DatabaseTaskListener<T> listener; // Made public for Repository access

        public DatabaseTask(DatabaseTaskListener<T> listener) {
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            // Can be overridden by subclasses for loading indicators
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(T result) {
            if (listener != null) {
                if (exception != null) {
                    listener.onError(exception);
                } else {
                    listener.onSuccess(result);
                }
            }
        }
        
        /**
         * Helper method to safely execute database tasks
         */
        public void executeOnExecutor() {
            this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    /**
     * Interface for database operation callbacks
     */
    public interface DatabaseTaskListener<T> {
        void onSuccess(T result);
        void onError(Exception error);
    }
}
