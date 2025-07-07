package com.example.bookingticketmove_prm392.database.dao;

import com.example.bookingticketmove_prm392.models.ContactMessage;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ContactDAO extends BaseDAO {

    public boolean addContactMessage(ContactMessage contact) throws SQLException{
        String query = "INSERT INTO ContactMessage (Email, Subject, Message) VALUES (?, ?, ?)";
        int result = executeUpdate(
                query,
                contact.getEmail(),
                contact.getSubject(),
                contact.getMessage()
        );
        return result > 0;
    }


    private ContactMessage mapResultSetToContact(ResultSet rs) throws SQLException {
        ContactMessage contact = new ContactMessage();
        contact.setEmail(rs.getString("Email"));
        contact.setSubject(rs.getString("Subject"));
        contact.setMessage(rs.getString("Message"));
        return contact;
    }

}
