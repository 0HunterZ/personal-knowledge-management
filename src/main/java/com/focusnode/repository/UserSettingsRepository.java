package com.focusnode.repository;

import com.focusnode.model.UserSettings;
import java.sql.*;

public class UserSettingsRepository {

    public UserSettings findByUserId(int userId) {
        String sql = "SELECT UserId, Theme, DailyFocusGoalMinutes, Language FROM dbo.UserSettings WHERE UserId = ?";

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
                     "USING (SELECT ? AS UserId, ? AS Theme, ? AS DailyFocusGoalMinutes, ? AS Language) AS source " +
                     "ON target.UserId = source.UserId " +
                     "WHEN MATCHED THEN " +
                     "    UPDATE SET Theme = source.Theme, DailyFocusGoalMinutes = source.DailyFocusGoalMinutes, Language = source.Language " +
                     "WHEN NOT MATCHED THEN " +
                     "    INSERT (UserId, Theme, DailyFocusGoalMinutes, Language) " +
                     "    VALUES (source.UserId, source.Theme, source.DailyFocusGoalMinutes, source.Language);";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, settings.getUserId());
            stmt.setString(2, settings.getTheme());
            stmt.setInt(3, settings.getDailyFocusGoalMinutes());
            stmt.setString(4, settings.getLanguage());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
