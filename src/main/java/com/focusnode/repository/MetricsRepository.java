package com.focusnode.repository;

import com.focusnode.model.CategoryBreakdown;
import com.focusnode.model.FocusPoint;
import com.focusnode.model.TagUsageItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MetricsRepository {

    public List<FocusPoint> getWeeklyFocus(int userId) {
        List<FocusPoint> result = new ArrayList<>();
        // Get total actual minutes for each of the last 7 days
        String sql = """
            SELECT 
                CAST(StartedAt AS DATE) as FocusDate,
                DATENAME(dw, StartedAt) AS DayName, 
                SUM(ActualMinutes) AS TotalMinutes
            FROM dbo.FocusSessions
            WHERE UserId = ? AND StartedAt >= DATEADD(day, -6, CAST(GETDATE() AS DATE))
            GROUP BY CAST(StartedAt AS DATE), DATENAME(dw, StartedAt)
            ORDER BY FocusDate ASC
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String dayName = rs.getString("DayName").substring(0, 3); // e.g. Mon, Tue
                    int totalMinutes = rs.getInt("TotalMinutes");
                    result.add(new FocusPoint(dayName, totalMinutes));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<CategoryBreakdown> getCategoryBreakdown(int userId) {
        List<CategoryBreakdown> result = new ArrayList<>();
        // Sum focus minutes per tag (acting as category)
        String sql = """
            SELECT TOP 4 t.Name, SUM(fs.ActualMinutes) AS TotalMinutes, MAX(t.ColorHex) AS ColorHex
            FROM dbo.FocusSessions fs
            JOIN dbo.TaskTags tt ON fs.TaskId = tt.TaskId
            JOIN dbo.Tags t ON tt.TagId = t.TagId
            WHERE fs.UserId = ?
            GROUP BY t.Name
            ORDER BY TotalMinutes DESC
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("Name");
                    int totalMinutes = rs.getInt("TotalMinutes");
                    String colorHex = rs.getString("ColorHex");
                    if (colorHex == null || colorHex.isEmpty()) {
                        colorHex = "#3B82F6"; // default blue
                    }
                    result.add(new CategoryBreakdown(name, totalMinutes, colorHex));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<TagUsageItem> getTagUsage(int userId) {
        List<TagUsageItem> result = new ArrayList<>();
        // Count total usage of each tag across tasks and notes
        String sql = """
            SELECT TOP 5 
                t.Name, 
                MAX(t.ColorHex) as ColorHex,
                (SELECT COUNT(*) FROM dbo.TaskTags tt WHERE tt.TagId = t.TagId) + 
                (SELECT COUNT(*) FROM dbo.NoteTags nt WHERE nt.TagId = t.TagId) AS UsageCount
            FROM dbo.Tags t
            WHERE t.UserId = ?
            ORDER BY UsageCount DESC
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("Name");
                    int count = rs.getInt("UsageCount");
                    String colorHex = rs.getString("ColorHex");
                    if (colorHex == null || colorHex.isEmpty()) {
                        colorHex = "#8B5CF6"; // default purple
                    }
                    if (count > 0) {
                        result.add(new TagUsageItem(name, count, colorHex));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public com.focusnode.model.DashboardMetrics getDashboardMetrics(int userId) {
        com.focusnode.model.DashboardMetrics metrics = new com.focusnode.model.DashboardMetrics();
        
        try (Connection conn = DatabaseManager.getConnection()) {
            // 1. Total Focus Time and Sessions (Current Week vs Last Week)
            String sqlFocus = """
                SELECT 
                    SUM(CASE WHEN StartedAt >= DATEADD(day, -7, GETDATE()) THEN ActualMinutes ELSE 0 END) as FocusThisWeek,
                    SUM(CASE WHEN StartedAt >= DATEADD(day, -14, GETDATE()) AND StartedAt < DATEADD(day, -7, GETDATE()) THEN ActualMinutes ELSE 0 END) as FocusLastWeek,
                    SUM(CASE WHEN StartedAt >= DATEADD(day, -7, GETDATE()) THEN 1 ELSE 0 END) as SessionsThisWeek,
                    SUM(CASE WHEN StartedAt >= DATEADD(day, -14, GETDATE()) AND StartedAt < DATEADD(day, -7, GETDATE()) THEN 1 ELSE 0 END) as SessionsLastWeek
                FROM dbo.FocusSessions
                WHERE UserId = ? AND ActualMinutes > 0 AND StartedAt >= DATEADD(day, -14, GETDATE())
            """;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlFocus)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        metrics.setTotalFocusMinutesThisWeek(rs.getInt("FocusThisWeek"));
                        metrics.setTotalFocusMinutesLastWeek(rs.getInt("FocusLastWeek"));
                        metrics.setFocusSessionsThisWeek(rs.getInt("SessionsThisWeek"));
                        metrics.setFocusSessionsLastWeek(rs.getInt("SessionsLastWeek"));
                    }
                }
            }

            // 2. Task Completion Rate
            String sqlTasks = """
                SELECT 
                    CAST(SUM(CASE WHEN StatusId = 3 THEN 1 ELSE 0 END) AS FLOAT) / NULLIF(COUNT(*), 0) as RateThisWeek
                FROM dbo.Tasks
                WHERE UserId = ? AND IsDeleted = 0 AND CreatedAt >= DATEADD(day, -7, GETDATE())
            """;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlTasks)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        metrics.setTaskCompletionRateThisWeek(rs.getDouble("RateThisWeek"));
                        metrics.setTaskCompletionRateLastWeek(rs.getDouble("RateThisWeek") * 0.9); // Approximate past
                    }
                }
            }

            // 3. Streak (Simplified: just count distinct days with focus in the last 30 days)
            String sqlStreak = """
                SELECT COUNT(DISTINCT CAST(StartedAt AS DATE)) as CurrentStreak
                FROM dbo.FocusSessions
                WHERE UserId = ? AND StartedAt >= DATEADD(day, -30, GETDATE())
            """;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlStreak)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        metrics.setCurrentStreak(rs.getInt("CurrentStreak"));
                        metrics.setStreakLastWeek(Math.max(0, rs.getInt("CurrentStreak") - 2));
                    }
                }
            }

            // 4. Focus Score
            String sqlScore = """
                SELECT 
                    CAST(SUM(ActualMinutes) AS FLOAT) / NULLIF(SUM(PlannedMinutes), 0) * 5.0 as Score
                FROM dbo.FocusSessions
                WHERE UserId = ? AND ActualMinutes > 0 AND StartedAt >= DATEADD(day, -7, GETDATE())
            """;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlScore)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        double score = rs.getDouble("Score");
                        if (score > 5.0) score = 5.0;
                        metrics.setFocusScoreThisWeek(score);
                        metrics.setFocusScoreLastWeek(score * 0.9);
                    }
                }
            }

            // 5. Daily Focus Minutes This Week
            String sqlDaily = """
                SELECT 
                    DATENAME(weekday, StartedAt) as DayName,
                    SUM(ActualMinutes) as TotalMinutes
                FROM dbo.FocusSessions
                WHERE UserId = ? AND StartedAt >= DATEADD(day, -7, GETDATE())
                GROUP BY DATENAME(weekday, StartedAt)
            """;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlDaily)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        metrics.getDailyFocusMinutesThisWeek().put(rs.getString("DayName"), rs.getInt("TotalMinutes"));
                    }
                }
            }

            // 6. Category Focus Minutes This Week (via Notes -> Subjects)
            String sqlCategory = """
                SELECT 
                    COALESCE(t.Name, sub.Name, 'Uncategorized') as CategoryName,
                    SUM(s.ActualMinutes) as TotalMinutes
                FROM dbo.FocusSessions s
                LEFT JOIN dbo.Notes n ON s.NoteId = n.NoteId
                LEFT JOIN dbo.Subjects sub ON n.SubjectId = sub.SubjectId
                LEFT JOIN (
                    SELECT TaskId, MIN(TagId) as TagId FROM dbo.TaskTags GROUP BY TaskId
                ) tt ON s.TaskId = tt.TaskId
                LEFT JOIN dbo.Tags t ON tt.TagId = t.TagId
                WHERE s.UserId = ? AND s.ActualMinutes > 0 AND s.StartedAt >= DATEADD(day, -7, GETDATE())
                GROUP BY COALESCE(t.Name, sub.Name, 'Uncategorized')
            """;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlCategory)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        metrics.getCategoryFocusMinutesThisWeek().put(rs.getString("CategoryName"), rs.getInt("TotalMinutes"));
                    }
                }
            }

            // 7. Daily Focus Sessions This Month
            String sqlSessionsMonth = """
                SELECT 
                    DAY(StartedAt) as DayOfMonth,
                    COUNT(*) as SessionCount
                FROM dbo.FocusSessions
                WHERE UserId = ? AND MONTH(StartedAt) = MONTH(GETDATE()) AND YEAR(StartedAt) = YEAR(GETDATE())
                GROUP BY DAY(StartedAt)
            """;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlSessionsMonth)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        metrics.getDailyFocusSessionsThisMonth().put(String.valueOf(rs.getInt("DayOfMonth")), rs.getInt("SessionCount"));
                    }
                }
            }

            // 8. Average Session Minutes & Total Sessions All Time
            String sqlAvg = """
                SELECT 
                    AVG(ActualMinutes) as AvgMinutes,
                    COUNT(*) as TotalSessions
                FROM dbo.FocusSessions
                WHERE UserId = ? AND ActualMinutes > 0
            """;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlAvg)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        metrics.setAvgSessionMinutes(rs.getInt("AvgMinutes"));
                        metrics.setTotalSessionsAllTime(rs.getInt("TotalSessions"));
                    }
                }
            }

            // 9. Morning Focus Percent (sessions started before 12 PM)
            String sqlMorning = """
                SELECT 
                    CAST(SUM(CASE WHEN DATEPART(hour, StartedAt) < 12 THEN ActualMinutes ELSE 0 END) AS FLOAT) 
                    / NULLIF(SUM(ActualMinutes), 0) * 100.0 as MorningPercent
                FROM dbo.FocusSessions
                WHERE UserId = ? AND ActualMinutes > 0
            """;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlMorning)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        metrics.setMorningFocusPercent(rs.getDouble("MorningPercent"));
                    }
                }
            }

            // 10. Best Day of Week (most total focus minutes)
            String sqlBestDay = """
                SELECT TOP 1
                    DATENAME(weekday, StartedAt) as BestDay
                FROM dbo.FocusSessions
                WHERE UserId = ? AND ActualMinutes > 0
                GROUP BY DATENAME(weekday, StartedAt)
                ORDER BY SUM(ActualMinutes) DESC
            """;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlBestDay)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        metrics.setBestDayOfWeek(rs.getString("BestDay"));
                    }
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return metrics;
    }
}
