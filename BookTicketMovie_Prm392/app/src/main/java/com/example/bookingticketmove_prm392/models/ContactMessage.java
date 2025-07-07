package com.example.bookingticketmove_prm392.models;

public class ContactMessage {
    private String email;
    private String subject;
    private String message;

    public ContactMessage(String email, String subject, String message) {
        this.email = email;
        this.subject = subject;
        this.message = message;
    }

    public ContactMessage() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ContactMessage{" +
                "email='" + email + '\'' +
                ", subject='" + subject + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
