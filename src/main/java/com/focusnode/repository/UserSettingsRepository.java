package com.focusnode.repository;

import com.focusnode.model.UserSettings;
import java.sql.*;

public class UserSettingsRepository {

    public UserSettings findByUserId(int userId) {
        String sql = "SELECT UserId, Theme, DailyFocusGoalMinutes, Language, GeminiApiKey, DefaultStoragePath, GoogleDriveSyncEnabled FROM dbo.UserSettings WHERE UserId = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UserSettings settings = new UserSettings();
                    settings.setUserId(rs.getInt("UserId"));
                    settings.setTheme(rs.getString("Theme"));
                    settings.setDailyFocusGoalMinutes(rs.getInt("DailyFocusGoalMinutes"));
                    settings.setLanguage(rs.getString("Language"));
                    settings.setGeminiApiKey(rs.getString("GeminiApiKey"));
                    settings.setDefaultStoragePath(rs.getString("DefaultStoragePath"));
                    
                    boolean driveSync = rs.getBoolean("GoogleDriveSyncEnabled");
                    if (rs.wasNull()) {
                        settings.setGoogleDriveSyncEnabled(false);
                    } else {
                        settings.setGoogleDriveSyncEnabled(driveSync);
                    }
                    
                    return settings;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean upsert(UserSettings settings) {
        String sql = "MERGE dbo.UserSettings AS target " +
                     "USING (SELECT ? AS UserId, ? AS Theme, ? AS DailyFocusGoalMinutes, ? AS Language, ? AS GeminiApiKey, ? AS DefaultStoragePath, ? AS GoogleDriveSyncEnabled) AS source " +
                     "ON target.UserId = source.UserId " +
                     "WHEN MATCHED THEN " +
                     "    UPDATE SET Theme = source.Theme, DailyFocusGoalMinutes = source.DailyFocusGoalMinutes, Language = source.Language, GeminiApiKey = source.GeminiApiKey, DefaultStoragePath = source.DefaultStoragePath, GoogleDriveSyncEnabled = source.GoogleDriveSyncEnabled " +
                     "WHEN NOT MATCHED THEN " +
                     "    INSERT (UserId, Theme, DailyFocusGoalMinutes, Language, GeminiApiKey, DefaultStoragePath, GoogleDriveSyncEnabled) " +
                     "    VALUES (source.UserId, source.Theme, source.DailyFocusGoalMinutes, source.Language, source.GeminiApiKey, source.DefaultStoragePath, source.GoogleDriveSyncEnabled);";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, settings.getUserId());
            stmt.setString(2, settings.getTheme());
            stmt.setInt(3, settings.getDailyFocusGoalMinutes());
            stmt.setString(4, settings.getLanguage());
            stmt.setString(5, settings.getGeminiApiKey());
            stmt.setString(6, settings.getDefaultStoragePath());
            if (settings.getGoogleDriveSyncEnabled() != null) {
                stmt.setBoolean(7, settings.getGoogleDriveSyncEnabled());
            } else {
                stmt.setNull(7, java.sql.Types.BIT);
            }

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
