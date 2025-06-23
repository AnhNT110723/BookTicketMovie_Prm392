package com.example.bookingticketmove_prm392.models;

import java.math.BigDecimal;
import java.util.Date;

public class User {
    private int userID;
    private String name;
    private String email;
    private String phone;
    private String passwordHash;
    private BigDecimal loyaltyPoints;
    private Date registrationDate;
    private boolean isActive;
    private int roleID;    // Default constructor
    public User() {
        this.loyaltyPoints = BigDecimal.ZERO;
        this.isActive = true;
        this.roleID = 2; // Default to Customer role
        this.registrationDate = new Date(); // Set registration date to current time
    }

    // Constructor with essential fields
    public User(String name, String email, String phone, String passwordHash) {
        this();
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.registrationDate = new Date();
    }

    // Getters and Setters
    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public BigDecimal getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(BigDecimal loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getRoleID() {
        return roleID;
    }    public void setRoleID(int roleID) {
        this.roleID = roleID;
    }

    // Convenience methods for UserManagementAdapter compatibility
    public int getUserId() {
        return this.userID;
    }

    public String getUsername() {
        return this.name;
    }

    public String getFirstName() {
        // For simplicity, we'll split the name field
        if (this.name != null && this.name.contains(" ")) {
            return this.name.split(" ")[0];
        }
        return this.name;
    }

    public String getLastName() {
        // For simplicity, we'll split the name field
        if (this.name != null && this.name.contains(" ")) {
            String[] parts = this.name.split(" ");
            if (parts.length > 1) {
                return parts[parts.length - 1];
            }
        }
        return null;
    }

    public Date getJoinDate() {
        return this.registrationDate;
    }

    public int getRole() {
        return this.roleID;
    }

    public void setRole(int role) {
        this.roleID = role;
    }

    @Override
    public String toString() {
        return "User{" +
                "userID=" + userID +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", loyaltyPoints=" + loyaltyPoints +
                ", registrationDate=" + registrationDate +
                ", isActive=" + isActive +
                ", roleID=" + roleID +
                '}';
    }
}
