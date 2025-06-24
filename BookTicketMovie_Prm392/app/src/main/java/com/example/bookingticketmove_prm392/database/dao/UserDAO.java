package com.example.bookingticketmove_prm392.database.dao;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.example.bookingticketmove_prm392.database.DatabaseConfig;
import com.example.bookingticketmove_prm392.models.User;
import com.example.bookingticketmove_prm392.utils.PasswordUtils;

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
     * saveResetToken
     */
    public void saveResetToken(String email, String token, Timestamp expiresAt, DatabaseTaskListener<Boolean> listener) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            Boolean result = false;
            Exception error = null;

            try {
                User user = getUserByEmail(email);
                if (user == null) {
                    throw new Exception("Không tìm thấy người dùng");
                }

                int userId = user.getUserID();
                // 1. Kiểm tra xem đã có token reset cho user chưa
                String checkSql = "SELECT COUNT(*) FROM Tokens WHERE UserID = ? AND TokenType = 'password_reset'";
                ResultSet rs = executeQuery(checkSql, userId);
                boolean tokenExists = false;
                if (rs != null && rs.next()) {
                    tokenExists = rs.getInt(1) > 0;
                }
                rs.close();

                if (tokenExists) {
                    result = updateResetToken(userId, token, expiresAt);
                } else {
                    // 3. Nếu chưa thì insert
                    result = insertResetToken(userId, token, expiresAt);
                }
            } catch (Exception e) {
                error = e;
                Log.e(TAG, "Lỗi khi lưu token reset", e);
            }

            Boolean finalResult = result;
            Exception finalError = error;

            mainHandler.post(() -> {
                if (listener != null) {
                    if (finalError != null) {
                        listener.onError(finalError);
                    } else {
                        listener.onSuccess(finalResult);
                    }
                }
            });
        });
    }

    /**
     * isTokenExists
     */
    private boolean isTokenExists(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Tokens WHERE UserID = ? AND TokenType = 'password_reset'";
        ResultSet rs = executeQuery(sql, userId);
        boolean exists = false;
        if (rs != null && rs.next()) {
            exists = rs.getInt(1) > 0;
        }
        if (rs != null) rs.close();
        return exists;
    }
    /**
     * insertResetToken
     */
    private boolean insertResetToken(int userId, String token, Timestamp expiresAt) throws SQLException {
        String sql = "INSERT INTO Tokens (UserID, TokenValue, TokenType, IssuedAt, ExpiresAt, IsRevoked, Version) " +
                "VALUES (?, ?, 'password_reset', GETDATE(), ?, 0, 1)";
        return executeUpdate(sql, userId, token, expiresAt) > 0;
    }
    /**
     * updateResetToken
     */
    private boolean updateResetToken(int userId, String token, Timestamp expiresAt) throws SQLException {
        String sql = "UPDATE Tokens SET TokenValue = ?, IssuedAt = GETDATE(), ExpiresAt = ?, IsRevoked = 0, Version = Version + 1 " +
                "WHERE UserID = ? AND TokenType = 'password_reset'";
        return executeUpdate(sql, token, expiresAt, userId) > 0;
    }

    /**
     * verify ResetToken
     */
    public void verifyResetToken(String token, DatabaseTaskListener<String> listener) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            String email = null;
            Exception error = null;

            try {
                String sql = "SELECT u.Email FROM Tokens t " +
                        "JOIN [User] u ON t.UserID = u.UserID " +
                        "WHERE t.TokenValue = ? AND t.TokenType = 'password_reset' " +
                        "AND t.ExpiresAt > GETDATE() AND t.IsRevoked = 0";

                ResultSet rs = executeQuery(sql, token);
                if (rs != null && rs.next()) {
                    email = rs.getString("Email");
                } else {
                    throw new Exception("Token không hợp lệ hoặc đã hết hạn");
                }
                if (rs != null) rs.close();
            } catch (Exception e) {
                error = e;
            }

            String finalEmail = email;
            Exception finalError = error;

            mainHandler.post(() -> {
                if (listener != null) {
                    if (finalError != null) {
                        listener.onError(finalError);
                    } else {
                        listener.onSuccess(finalEmail);
                    }
                }
            });
        });
    }

    /**
     * update Password
     */
    public boolean updatePassword(String email, String newPassword) throws SQLException {
        String sql = "UPDATE [User] SET PasswordHash = ? WHERE Email = ?";
        String hashedPassword = PasswordUtils.simpleHash(newPassword); // hoặc BCrypt nếu bạn có
        int rows = executeUpdate(sql, hashedPassword, email);
        return rows > 0;
    }

    /**
     * Update Password Task
     */
    public static class UpdatePasswordTask {
        private final String email;
        private final String newPassword;
        private final UserDAO userDAO = new UserDAO();
        private final DatabaseTaskListener<Boolean> listener;

        public UpdatePasswordTask(String email, String newPassword, DatabaseTaskListener<Boolean> listener) {
            this.email = email;
            this.newPassword = newPassword;
            this.listener = listener;
        }

        public void execute() {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                boolean result = false;
                Exception error = null;

                try {
                    result = userDAO.updatePassword(email, newPassword);
                } catch (Exception e) {
                    error = e;
                }

                boolean finalResult = result;
                Exception finalError = error;

                handler.post(() -> {
                    if (finalError != null) {
                        listener.onError(finalError);
                    } else {
                        listener.onSuccess(finalResult);
                    }
                });
            });
        }
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
    public void isEmailExists(String email, DatabaseTaskListener<Boolean> databaseTaskListener) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            Boolean result = false;
            Exception exception = null;

            try {
                String sql = "SELECT COUNT(*) FROM " + DatabaseConfig.TABLE_USER + " WHERE Email = ?";
                ResultSet rs = null;
                PreparedStatement statement = null;

                try {
                    Log.d(TAG, "Executing query for email: " + email); // Thêm log để kiểm tra
                    rs = executeQuery(sql, email);
                    if (rs != null && rs.next()) {
                        result = rs.getInt(1) > 0;
                        Log.d(TAG, "Query result: " + result); // Kiểm tra kết quả
                    }
                } finally {
                    closeResources(rs, statement);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error checking email existence", e); // Log lỗi chi tiết
                exception = e;
            }

            final Boolean finalResult = result;
            final Exception finalException = exception;

            mainHandler.post(() -> {
                if (databaseTaskListener != null) {
                    if (finalException != null) {
                        Log.e(TAG, "Calling onError with exception: " + finalException.getMessage());
                        databaseTaskListener.onError(finalException);
                    } else {
                        Log.d(TAG, "Calling onSuccess with result: " + finalResult);
                        databaseTaskListener.onSuccess(finalResult);
                    }
                }
            });
        });
    }
       /**
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

    /**
     * Modern approach for updating user
     */
    public static class UpdateUserTask {
        private User user;
        private UserDAO userDAO;
        private DatabaseTaskListener<Boolean> listener;

        public UpdateUserTask(User user, DatabaseTaskListener<Boolean> listener) {
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
                    result = userDAO.updateUser(user);
                } catch (Exception e) {
                    Log.e(TAG, "Error updating user", e);
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
}
