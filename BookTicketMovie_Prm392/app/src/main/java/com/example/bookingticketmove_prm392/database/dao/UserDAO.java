package com.example.bookingticketmove_prm392.database.dao;

import android.os.AsyncTask;
import android.util.Log;

import com.example.bookingticketmove_prm392.database.DatabaseConnection;
import com.example.bookingticketmove_prm392.models.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class UserDAO extends BaseDAO {
    private static final String TAG = "UserDAO";

    public interface DatabaseTaskListener<T> {
        void onSuccess(T result);
        void onError(Exception error);
    }
    
    // This is a synchronous method
    public User getUserByEmail(String email) throws SQLException {
        String sql = "SELECT UserID, Name, Email, Phone, PasswordHash, LoyaltyPoints, RegistrationDate, IsActive, RoleID FROM [User] WHERE Email = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    // This is an asynchronous method that wraps the synchronous one
    public void getUserByEmail(String email, DatabaseTaskListener<User> listener) {
        new GetUserByEmailTask(email, listener).execute();
    }

    public static class GetUserByEmailTask extends AsyncTask<Void, Void, User> {
        private final String email;
        private final DatabaseTaskListener<User> listener;
        private final UserDAO userDAO;
        private Exception exception;

        public GetUserByEmailTask(String email, DatabaseTaskListener<User> listener) {
            this.email = email;
            this.listener = listener;
            this.userDAO = new UserDAO();
        }

        @Override
        protected User doInBackground(Void... voids) {
            try {
                return userDAO.getUserByEmail(email);
            } catch (Exception e) {
                Log.e(TAG, "Error getting user by email in background", e);
                exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(User user) {
            if (listener != null) {
                if (exception != null) {
                    listener.onError(exception);
                } else {
                    listener.onSuccess(user);
                }
            }
        }
    }

    private static User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserID(rs.getInt("UserID"));
        user.setName(rs.getString("Name"));
        user.setEmail(rs.getString("Email"));
        user.setPhone(rs.getString("Phone"));
        user.setPasswordHash(rs.getString("PasswordHash"));
        user.setLoyaltyPoints(rs.getBigDecimal("LoyaltyPoints"));
        user.setRegistrationDate(rs.getTimestamp("RegistrationDate"));
        user.setActive(rs.getBoolean("IsActive"));
        user.setRoleID(rs.getInt("RoleID"));
        return user;
    }

    public User loginUser(String email, String passwordHash) throws SQLException {
        String sql = "SELECT * FROM [User] WHERE Email = ? AND PasswordHash = ? AND IsActive = 1";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, passwordHash);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    public boolean createUser(User user) throws SQLException {
        String sql = "INSERT INTO [User] (Name, Email, Phone, PasswordHash, LoyaltyPoints, RegistrationDate, IsActive, RoleID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPhone());
            stmt.setString(4, user.getPasswordHash());
            stmt.setBigDecimal(5, user.getLoyaltyPoints());
            stmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            stmt.setBoolean(7, true);
            stmt.setInt(8, user.getRoleID());
            return stmt.executeUpdate() > 0;
        }
    }

    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT * FROM [User] WHERE UserID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    public boolean updateUser(User user) throws SQLException {
        String sql = "UPDATE [User] SET Name = ?, Phone = ? WHERE UserID = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getPhone());
            stmt.setInt(3, user.getUserID());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean isEmailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM [User] WHERE Email = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public static class LoginUserTask extends AsyncTask<Void, Void, User> {
        private final String email;
        private final String passwordHash;
        private final DatabaseTaskListener<User> listener;
        private final UserDAO userDAO;
        private Exception exception;

        public LoginUserTask(String email, String passwordHash, DatabaseTaskListener<User> listener) {
            this.email = email;
            this.passwordHash = passwordHash;
            this.listener = listener;
            this.userDAO = new UserDAO();
        }

        @Override
        protected User doInBackground(Void... voids) {
            try {
                return userDAO.loginUser(email, passwordHash);
            } catch (Exception e) {
                Log.e(TAG, "Error logging in user", e);
                exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(User user) {
            if (listener != null) {
                if (exception != null) {
                    listener.onError(exception);
                } else {
                    listener.onSuccess(user);
                }
            }
        }
    }

    public static class CreateUserTask extends AsyncTask<Void, Void, Boolean> {
        private final User user;
        private final DatabaseTaskListener<Boolean> listener;
        private final UserDAO userDAO;
        private Exception exception;

        public CreateUserTask(User user, DatabaseTaskListener<Boolean> listener) {
            this.user = user;
            this.listener = listener;
            this.userDAO = new UserDAO();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                return userDAO.createUser(user);
            } catch (Exception e) {
                Log.e(TAG, "Error creating user", e);
                exception = e;
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (listener != null) {
                if (exception != null) {
                    listener.onError(exception);
                } else {
                    listener.onSuccess(success);
                }
            }
        }
    }

    public static class UpdateUserTask extends AsyncTask<Void, Void, Boolean> {
        private final User user;
        private final DatabaseTaskListener<Boolean> listener;
        private final UserDAO userDAO;
        private Exception exception;

        public UpdateUserTask(User user, DatabaseTaskListener<Boolean> listener) {
            this.user = user;
            this.listener = listener;
            this.userDAO = new UserDAO();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                return userDAO.updateUser(user);
            } catch (Exception e) {
                Log.e(TAG, "Error updating user", e);
                exception = e;
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (listener != null) {
                if (exception != null) {
                    listener.onError(exception);
                } else {
                    listener.onSuccess(success);
                }
            }
        }
    }
    
    // Other synchronous methods can be added here...
}
