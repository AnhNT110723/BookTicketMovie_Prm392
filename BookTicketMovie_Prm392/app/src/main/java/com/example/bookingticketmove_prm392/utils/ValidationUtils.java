package com.example.bookingticketmove_prm392.utils;

import android.util.Patterns;

public class ValidationUtils {
    
    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        return email != null && !email.trim().isEmpty() && 
               Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }
    
    /**
     * Validate password strength
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
    
    /**
     * Validate name
     */
    public static boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty() && name.trim().length() >= 2;
    }
    
    /**
     * Validate phone number
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && !phone.trim().isEmpty() && 
               phone.trim().matches("^[+]?[0-9]{10,15}$");
    }
    
    /**
     * Check if passwords match
     */
    public static boolean doPasswordsMatch(String password, String confirmPassword) {
        return password != null && confirmPassword != null && 
               password.equals(confirmPassword);
    }
    
    /**
     * Get password strength message
     */
    public static String getPasswordStrengthMessage(String password) {
        if (password == null || password.isEmpty()) {
            return "Password cannot be empty";
        }
        if (password.length() < 6) {
            return "Password must be at least 6 characters long";
        }
        if (password.length() < 8) {
            return "Consider using a longer password for better security";
        }
        return "Password looks good";
    }
}
