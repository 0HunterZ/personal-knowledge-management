package com.focusnode.repository;

import com.focusnode.model.LanTransferHistory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class LanTransferHistoryRepository {

    public void save(LanTransferHistory history) throws SQLException {
        String query = "INSERT INTO dbo.LanTransferHistory (TransferId, UserId, FileName, TargetName, SizeBytes, Status, CreatedAt) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
             
            pstmt.setString(1, history.getTransferId());
            pstmt.setInt(2, history.getUserId());
            pstmt.setString(3, history.getFileName());
            pstmt.setString(4, history.getTargetName());
            pstmt.setLong(5, history.getSizeBytes());
            pstmt.setString(6, history.getStatus());
            pstmt.setTimestamp(7, Timestamp.valueOf(history.getCreatedAt()));
            
            pstmt.executeUpdate();
        }
    }

    public List<LanTransferHistory> getHistoryByUserId(int userId) throws SQLException {
        List<LanTransferHistory> historyList = new ArrayList<>();
        String query = "SELECT TransferId, UserId, FileName, TargetName, SizeBytes, Status, CreatedAt " +
                       "FROM dbo.LanTransferHistory WHERE UserId = ? ORDER BY CreatedAt DESC";
                       
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
             
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    LanTransferHistory h = new LanTransferHistory();
                    h.setTransferId(rs.getString("TransferId"));
                    h.setUserId(rs.getInt("UserId"));
                    h.setFileName(rs.getString("FileName"));
                    h.setTargetName(rs.getString("TargetName"));
                    h.setSizeBytes(rs.getLong("SizeBytes"));
                    h.setStatus(rs.getString("Status"));
                    h.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
                    historyList.add(h);
                }
            }
        }
        return historyList;
    }
}
