package com.example.bookingticketmove_prm392.repositories;

import com.example.bookingticketmove_prm392.database.dao.BaseDAO;
import com.example.bookingticketmove_prm392.database.dao.UserDAO;
import com.example.bookingticketmove_prm392.models.User;
import java.util.List;

/**
 * UserRepository handles all user-related data operations
 * Implements Repository Pattern for User entity
 */
public class UserRepository extends BaseRepository {
    private UserDAO userDAO;
    
    public UserRepository() {
        this.userDAO = new UserDAO();
    }
      /**
     * Authenticate user with email and password
     */
    public void loginUser(String email, String password, RepositoryCallback<User> callback) {
        BaseDAO.DatabaseTask<User> task = new BaseDAO.DatabaseTask<User>(null) {
            @Override
            protected User doInBackground(Void... voids) {
                try {
                    // Note: In real implementation, password should be hashed before passing to DAO
                    // For now, assuming password is already hashed or DAO handles hashing
                    return userDAO.loginUser(email, password);
                } catch (Exception e) {
                    exception = e;
                    return null;
                }
            }
        };
        
        executeAsync(task, callback);
    }
    
    /**
     * Register a new user
     */
    public void registerUser(User user, RepositoryCallback<Boolean> callback) {
        BaseDAO.DatabaseTask<Boolean> task = new BaseDAO.DatabaseTask<Boolean>(null) {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    return userDAO.createUser(user);
                } catch (Exception e) {
                    exception = e;
                    return false;
                }
            }
        };
        
        executeAsync(task, callback);
    }
    
    /**
     * Get user by ID
     */
    public void getUserById(int userId, RepositoryCallback<User> callback) {
        BaseDAO.DatabaseTask<User> task = new BaseDAO.DatabaseTask<User>(null) {
            @Override
            protected User doInBackground(Void... voids) {
                try {
                    return userDAO.getUserById(userId);
                } catch (Exception e) {
                    exception = e;
                    return null;
                }
            }
        };
        
        executeAsync(task, callback);
    }
    
    /**
     * Update user profile
     */
    public void updateUser(User user, RepositoryCallback<Boolean> callback) {
        BaseDAO.DatabaseTask<Boolean> task = new BaseDAO.DatabaseTask<Boolean>(null) {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    return userDAO.updateUser(user);
                } catch (Exception e) {
                    exception = e;
                    return false;
                }
            }
        };
        
        executeAsync(task, callback);
    }
    
    /**
     * Check if email exists
     */
    public void isEmailExists(String email, RepositoryCallback<Boolean> callback) {
        BaseDAO.DatabaseTask<Boolean> task = new BaseDAO.DatabaseTask<Boolean>(null) {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    return userDAO.isEmailExists(email);
                } catch (Exception e) {
                    exception = e;
                    return false;
                }
            }
        };
        
        executeAsync(task, callback);
    }
}
