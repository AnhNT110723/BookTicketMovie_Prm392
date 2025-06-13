package com.example.bookingticketmove_prm392.controllers;

import com.example.bookingticketmove_prm392.models.User;
import com.example.bookingticketmove_prm392.repositories.UserRepository;
import com.example.bookingticketmove_prm392.utils.ValidationUtils;

/**
 * UserController handles all user-related business logic
 * Acts as the Controller in MVC pattern for User operations
 */
public class UserController extends BaseController {
    private UserRepository userRepository;
    
    public UserController() {
        this.userRepository = new UserRepository();
    }
    
    /**
     * Handle user login with validation
     */
    public void login(String email, String password, ControllerCallback<User> callback) {
        // Input validation
        if (!ValidationUtils.isValidEmail(email)) {
            if (callback != null) {
                callback.onError("Please enter a valid email address");
            }
            return;
        }
        
        if (!ValidationUtils.isValidPassword(password)) {
            if (callback != null) {
                callback.onError("Password must be at least 6 characters long");
            }
            return;
        }
        
        notifyLoading(callback, true);
        
        userRepository.loginUser(email, password, new UserRepository.RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (callback != null) {
                    callback.onLoading(false);
                    if (user != null) {
                        callback.onSuccess(user);
                    } else {
                        callback.onError("Invalid email or password");
                    }
                }
            }
            
            @Override
            public void onError(Exception error) {
                handleError(callback, error, "Login failed. Please try again.");
            }
        });
    }
    
    /**
     * Handle user registration with validation
     */
    public void register(String fullName, String email, String password, String confirmPassword, ControllerCallback<Boolean> callback) {
        // Input validation
        if (fullName == null || fullName.trim().isEmpty()) {
            if (callback != null) {
                callback.onError("Please enter your full name");
            }
            return;
        }
        
        if (!ValidationUtils.isValidEmail(email)) {
            if (callback != null) {
                callback.onError("Please enter a valid email address");
            }
            return;
        }
        
        if (!ValidationUtils.isValidPassword(password)) {
            if (callback != null) {
                callback.onError("Password must be at least 6 characters long");
            }
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            if (callback != null) {
                callback.onError("Passwords do not match");
            }
            return;
        }
        
        notifyLoading(callback, true);
        
        // Check if email already exists
        userRepository.isEmailExists(email, new UserRepository.RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean exists) {
                if (exists) {
                    if (callback != null) {
                        callback.onLoading(false);
                        callback.onError("Email already exists. Please use a different email.");
                    }
                } else {
                    // Create new user
                    User newUser = new User();
                    newUser.setName(fullName);
                    newUser.setEmail(email);
                    newUser.setPasswordHash(password); // Note: Password should be hashed in UserDAO
                    
                    userRepository.registerUser(newUser, new UserRepository.RepositoryCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean success) {
                            if (callback != null) {
                                callback.onLoading(false);
                                if (success) {
                                    callback.onSuccess(true);
                                } else {
                                    callback.onError("Registration failed. Please try again.");
                                }
                            }
                        }
                        
                        @Override
                        public void onError(Exception error) {
                            handleError(callback, error, "Registration failed. Please try again.");
                        }
                    });
                }
            }
            
            @Override
            public void onError(Exception error) {
                handleError(callback, error, "Unable to check email availability. Please try again.");
            }
        });
    }
    
    /**
     * Get user profile
     */
    public void getUserProfile(int userId, ControllerCallback<User> callback) {
        if (userId <= 0) {
            if (callback != null) {
                callback.onError("Invalid user ID");
            }
            return;
        }
        
        notifyLoading(callback, true);
        
        userRepository.getUserById(userId, new UserRepository.RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (callback != null) {
                    callback.onLoading(false);
                    if (user != null) {
                        callback.onSuccess(user);
                    } else {
                        callback.onError("User not found");
                    }
                }
            }
            
            @Override
            public void onError(Exception error) {
                handleError(callback, error, "Failed to load user profile");
            }
        });
    }
    
    /**
     * Update user profile
     */
    public void updateProfile(User user, ControllerCallback<Boolean> callback) {
        // Validation
        if (user == null) {
            if (callback != null) {
                callback.onError("Invalid user data");
            }
            return;
        }
        
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            if (callback != null) {
                callback.onError("Please enter your full name");
            }
            return;
        }
        
        if (!ValidationUtils.isValidEmail(user.getEmail())) {
            if (callback != null) {
                callback.onError("Please enter a valid email address");
            }
            return;
        }
        
        notifyLoading(callback, true);
        
        userRepository.updateUser(user, new UserRepository.RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                if (callback != null) {
                    callback.onLoading(false);
                    if (success) {
                        callback.onSuccess(true);
                    } else {
                        callback.onError("Failed to update profile");
                    }
                }
            }
            
            @Override
            public void onError(Exception error) {
                handleError(callback, error, "Failed to update profile");
            }
        });
    }
}
