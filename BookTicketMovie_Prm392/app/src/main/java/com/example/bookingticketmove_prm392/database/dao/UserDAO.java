package com.example.bookingticketmove_prm392.database.dao;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.example.bookingticketmove_prm392.database.DatabaseConfig;
import com.example.bookingticketmove_prm392.models.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserDAO extends BaseDAO {
    private static final String TAG = "UserDAO";

    /**
     * Get user by email
     */
    public User getUserByEmail(String email) throws SQLException {
        String sql = "SELECT UserID, Name, Email, Phone, PasswordHash, LoyaltyPoints, " +
                    "RegistrationDate, IsActive, RoleID FROM " + DatabaseConfig.TABLE_USER + 
                    " WHERE Email = ?";
        
        ResultSet rs = null;
        PreparedStatement statement = null;
        
        try {
            rs = executeQuery(sql, email);
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;
        } finally {
            closeResources(rs, statement);
        }
    }

    /**
     * Get user by ID
     */
    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT UserID, Name, Email, Phone, PasswordHash, LoyaltyPoints, " +
                    "RegistrationDate, IsActive, RoleID FROM " + DatabaseConfig.TABLE_USER + 
                    " WHERE UserID = ?";
        
        ResultSet rs = null;
        PreparedStatement statement = null;
        
        try {
            rs = executeQuery(sql, userId);
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;
        } finally {
            closeResources(rs, statement);
        }
    }

    /**
     * Create a new user
     */
    public boolean createUser(User user) throws SQLException {
        String sql = "INSERT INTO " + DatabaseConfig.TABLE_USER + 
                    " (Name, Email, Phone, PasswordHash, LoyaltyPoints, RegistrationDate, IsActive, RoleID) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        int result = executeUpdate(sql, 
            user.getName(),
            user.getEmail(),
            user.getPhone(),
            user.getPasswordHash(),
            user.getLoyaltyPoints(),
            new Timestamp(user.getRegistrationDate().getTime()),
            user.isActive() ? 1 : 0,
            user.getRoleID()
        );
        
        return result > 0;
    }

    /**
     * Update user information
     */
    public boolean updateUser(User user) throws SQLException {
        String sql = "UPDATE " + DatabaseConfig.TABLE_USER + 
                    " SET Name = ?, Phone = ?, LoyaltyPoints = ?, IsActive = ?, RoleID = ? " +
                    "WHERE UserID = ?";
        
        int result = executeUpdate(sql,
            user.getName(),
            user.getPhone(),
            user.getLoyaltyPoints(),
            user.isActive() ? 1 : 0,
            user.getRoleID(),
            user.getUserID()
        );
        
        return result > 0;
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() throws SQLException {
        String sql = "SELECT UserID, Name, Email, Phone, PasswordHash, LoyaltyPoints, " +
                    "RegistrationDate, IsActive, RoleID FROM " + DatabaseConfig.TABLE_USER;
        
        List<User> users = new ArrayList<>();
        ResultSet rs = null;
        PreparedStatement statement = null;
        
        try {
            rs = executeQuery(sql);
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            return users;
        } finally {
            closeResources(rs, statement);
        }
    }

    /**
     * Check if email exists
     */
    public boolean isEmailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + DatabaseConfig.TABLE_USER + " WHERE Email = ?";
        
        ResultSet rs = null;
        PreparedStatement statement = null;
        
        try {
            rs = executeQuery(sql, email);
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } finally {
            closeResources(rs, statement);
        }
    }    /**
     * Map ResultSet to User object
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
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

    /**
     * Login user with email and password
     */
    public User loginUser(String email, String passwordHash) throws SQLException {
        String sql = "SELECT UserID, Name, Email, Phone, PasswordHash, LoyaltyPoints, " +
                    "RegistrationDate, IsActive, RoleID FROM " + DatabaseConfig.TABLE_USER + 
                    " WHERE Email = ? AND PasswordHash = ? AND IsActive = 1";
        
        ResultSet rs = null;
        PreparedStatement statement = null;
        
        try {
            rs = executeQuery(sql, email, passwordHash);
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;
        } finally {
            closeResources(rs, statement);
        }
    }    /**
     * Modern approach for getting user by email
     */
    public static class GetUserByEmailTask {
        private String email;
        private UserDAO userDAO;
        private DatabaseTaskListener<User> listener;

        public GetUserByEmailTask(String email, DatabaseTaskListener<User> listener) {
            this.email = email;
            this.listener = listener;
            this.userDAO = new UserDAO();
        }

        public void execute() {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler mainHandler = new Handler(Looper.getMainLooper());
            
            executor.execute(() -> {
                User result = null;
                Exception exception = null;
                
                try {
                    result = userDAO.getUserByEmail(email);
                } catch (Exception e) {
                    Log.e(TAG, "Error getting user by email", e);
                    exception = e;
                }
                
                final User finalResult = result;
                final Exception finalException = exception;
                
                mainHandler.post(() -> {
                    if (listener != null) {
                        if (finalException != null) {
                            listener.onError(finalException);
                        } else {
                            listener.onSuccess(finalResult);
                        }
                    }
                });
            });
        }
    }

    /**
     * Modern approach for creating a user
     */
    public static class CreateUserTask {
        private User user;
        private UserDAO userDAO;
        private DatabaseTaskListener<Boolean> listener;

        public CreateUserTask(User user, DatabaseTaskListener<Boolean> listener) {
            this.user = user;
            this.listener = listener;
            this.userDAO = new UserDAO();
        }

        public void execute() {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler mainHandler = new Handler(Looper.getMainLooper());
            
            executor.execute(() -> {
                Boolean result = false;
                Exception exception = null;
                
                try {
                    result = userDAO.createUser(user);
                } catch (Exception e) {
                    Log.e(TAG, "Error creating user", e);
                    exception = e;
                }
                
                final Boolean finalResult = result;
                final Exception finalException = exception;
                  mainHandler.post(() -> {
                    if (listener != null) {
                        if (finalException != null) {
                            listener.onError(finalException);
                        } else {
                            listener.onSuccess(finalResult);
                        }
                    }
                });
            });
        }
    }

    /**
     * Modern approach for user login
     */
    public static class LoginUserTask {
        private String email;
        private String passwordHash;
        private UserDAO userDAO;
        private DatabaseTaskListener<User> listener;

        public LoginUserTask(String email, String passwordHash, DatabaseTaskListener<User> listener) {
            this.email = email;
            this.passwordHash = passwordHash;
            this.listener = listener;
            this.userDAO = new UserDAO();
        }

        public void execute() {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler mainHandler = new Handler(Looper.getMainLooper());
            
            executor.execute(() -> {
                User result = null;
                Exception exception = null;
                
                try {
                    result = userDAO.loginUser(email, passwordHash);
                } catch (Exception e) {
                    Log.e(TAG, "Error logging in user", e);
                    exception = e;
                }
                
                final User finalResult = result;
                final Exception finalException = exception;
                
                mainHandler.post(() -> {
                    if (listener != null) {
                        if (finalException != null) {
                            listener.onError(finalException);
                        } else {
                            listener.onSuccess(finalResult);
                        }
                    }
                });
            });
        }
    }
}
