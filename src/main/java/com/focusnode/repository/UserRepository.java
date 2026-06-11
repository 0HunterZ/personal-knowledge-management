package com.focusnode.repository;

import com.focusnode.model.User;

import java.sql.*;
import java.time.LocalDateTime;

public class UserRepository {
    
    public User findByUsername(String username) {
        String sql = "SELECT UserId, Username, Email, CreatedAt, PasswordHash FROM Users WHERE Username = ? AND IsDeleted = 0";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("UserId"),
                        rs.getString("Username"),
                        rs.getString("Email"),
                        rs.getTimestamp("CreatedAt").toLocalDateTime()
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean authenticate(String username, String passwordHash) {
        String sql = "SELECT PasswordHash FROM Users WHERE Username = ? AND IsDeleted = 0";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("PasswordHash");
                    // In a real app, use BCrypt or similar, but for now exact match
                    return storedHash.equals(passwordHash);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean createUser(String username, String email, String passwordHash) {
        String sql = "INSERT INTO Users (Username, Email, PasswordHash, CreatedAt, IsDeleted) VALUES (?, ?, ?, GETDATE(), 0)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, passwordHash);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
