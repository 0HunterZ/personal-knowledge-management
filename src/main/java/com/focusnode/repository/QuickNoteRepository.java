package com.focusnode.repository;

import com.focusnode.model.QuickNote;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QuickNoteRepository {

    public List<QuickNote> findByUserId(int userId) {
        List<QuickNote> notes = new ArrayList<>();
        String sql = "SELECT QuickNoteId, UserId, Content, CreatedAt FROM dbo.QuickNotes WHERE UserId = ? ORDER BY CreatedAt DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    QuickNote note = new QuickNote();
                    note.setId(rs.getInt("QuickNoteId"));
                    note.setUserId(rs.getInt("UserId"));
                    note.setContent(rs.getString("Content"));
                    Timestamp ts = rs.getTimestamp("CreatedAt");
                    if (ts != null) {
                        note.setCreatedAt(ts.toLocalDateTime());
                    }
                    notes.add(note);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }

    public boolean create(QuickNote note) {
        String sql = "INSERT INTO dbo.QuickNotes (UserId, Content, CreatedAt) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, note.getUserId());
            stmt.setString(2, note.getContent());
            stmt.setTimestamp(3, Timestamp.valueOf(note.getCreatedAt() != null ? note.getCreatedAt() : LocalDateTime.now()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        note.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int quickNoteId, int userId) {
        String sql = "DELETE FROM dbo.QuickNotes WHERE QuickNoteId = ? AND UserId = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quickNoteId);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
