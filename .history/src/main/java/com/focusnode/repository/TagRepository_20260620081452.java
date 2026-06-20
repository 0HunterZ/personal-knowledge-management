package com.focusnode.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class TagRepository {

    public int addTags(Collection<String> tagNames, int userId) {
        Set<String> normalizedTagNames = tagNames.stream()
                .map(this::normalizeTagName)
                .filter(name -> name != null && !name.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (normalizedTagNames.isEmpty()) {
            return 0;
        }

        int createdCount = 0;

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            for (String tagName : normalizedTagNames) {
                if (tagName.isBlank()) {
                    continue;
                }

                int tagId = findTagId(conn, tagName, userId);
                if (tagId != -1) {
                    continue;
                }

                try (PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO dbo.Tags(UserId, Name) VALUES(?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setInt(1, userId);
                    pstmt.setString(2, tagName);
                    pstmt.executeUpdate();
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            tagId = rs.getInt(1);
                        }
                    }
                }

                if (tagId != -1) {
                    createdCount++;
                }
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return createdCount;
    }

    private int findTagId(Connection conn, String tagName, int userId) throws SQLException {
        String sql = "SELECT TagId FROM dbo.Tags WHERE Name = ? AND UserId = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tagName);
            pstmt.setInt(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("TagId");
                }
            }
        }
        return -1;
    }

    private String normalizeTagName(String rawName) {
        if (rawName == null) {
            return null;
        }
        String normalized = rawName.trim();
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1).trim();
        }
        return normalized;
    }
}
