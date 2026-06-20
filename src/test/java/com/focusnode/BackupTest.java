package com.focusnode;

import com.focusnode.repository.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class BackupTest {
    public static void main(String[] args) {
        DatabaseManager.initialize();
        String sql = "BACKUP DATABASE [FocusNodeDB] TO DISK = N'C:\\temp\\focusnode_backup\\FocusNodeDB.bak' WITH INIT";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
            System.out.println("Backup successful!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
