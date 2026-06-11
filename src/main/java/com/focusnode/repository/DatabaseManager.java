package com.focusnode.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    // SQL Server Connection details
    // using user=sa;password=12345;databaseName=FocusNodeDB;
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=FocusNodeDB;user=sa;password=12345;encrypt=true;trustServerCertificate=true;";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initialize() {
        // Run Flyway Migrations
        try {
            org.flywaydb.core.Flyway flyway = org.flywaydb.core.Flyway.configure()
                .dataSource(URL, "sa", "12345")
                .baselineOnMigrate(true)
                .baselineVersion("2")
                .load();
            flyway.repair();
            flyway.migrate();
            System.out.println("Flyway migrations executed successfully!");
        } catch (Exception e) {
            System.err.println("Flyway migration failed: " + e.getMessage());
        }
        
        try (Connection conn = getConnection()) {
            System.out.println("Kết nối SQL Server FocusNodeDB thành công!");
        } catch (SQLException e) {
            System.err.println("Kết nối Database thất bại: " + e.getMessage());
        }
    }
}
