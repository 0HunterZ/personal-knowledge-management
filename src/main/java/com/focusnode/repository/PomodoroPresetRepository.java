package com.focusnode.repository;

import com.focusnode.model.PomodoroPreset;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PomodoroPresetRepository {

    public List<PomodoroPreset> findByUserId(int userId) {
        List<PomodoroPreset> presets = new ArrayList<>();
        // Select presets for this user or system defaults (UserId IS NULL)
        String sql = "SELECT PresetId, UserId, Name, FocusTimeMinutes, ShortBreakMinutes, LongBreakMinutes, LongBreakInterval " +
                     "FROM dbo.PomodoroPresets WHERE UserId = ? OR UserId IS NULL";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PomodoroPreset preset = new PomodoroPreset();
                    preset.setId(rs.getInt("PresetId"));
                    preset.setUserId(rs.getObject("UserId") != null ? rs.getInt("UserId") : null);
                    preset.setName(rs.getString("Name"));
                    preset.setFocusTimeMinutes(rs.getInt("FocusTimeMinutes"));
                    preset.setShortBreakMinutes(rs.getInt("ShortBreakMinutes"));
                    preset.setLongBreakMinutes(rs.getInt("LongBreakMinutes"));
                    preset.setLongBreakInterval(rs.getInt("LongBreakInterval"));
                    presets.add(preset);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return presets;
    }

    public boolean create(PomodoroPreset preset) {
        String sql = "INSERT INTO dbo.PomodoroPresets (UserId, Name, FocusTimeMinutes, ShortBreakMinutes, LongBreakMinutes, LongBreakInterval) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (preset.getUserId() != null) {
                stmt.setInt(1, preset.getUserId());
            } else {
                stmt.setNull(1, java.sql.Types.INTEGER);
            }
            stmt.setString(2, preset.getName());
            stmt.setInt(3, preset.getFocusTimeMinutes());
            stmt.setInt(4, preset.getShortBreakMinutes());
            stmt.setInt(5, preset.getLongBreakMinutes());
            stmt.setInt(6, preset.getLongBreakInterval());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        preset.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
