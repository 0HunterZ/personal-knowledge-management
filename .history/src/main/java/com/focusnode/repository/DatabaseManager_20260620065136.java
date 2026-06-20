package com.focusnode.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

public class DatabaseManager {
    private static final String DEFAULT_URL = "jdbc:sqlserver://localhost:1433;databaseName=FocusNodeDB;encrypt=true;trustServerCertificate=true;";
    private static final String DEFAULT_USER = "sa";
    private static final String DEFAULT_PASSWORD = "12345";

    private static String connectionUrl = DEFAULT_URL;
    private static String dbUser = DEFAULT_USER;
    private static String dbPassword = DEFAULT_PASSWORD;

    public static void setDatabaseConfig(String url, String user, String password) {
        connectionUrl = Objects.requireNonNullElse(url, DEFAULT_URL);
        dbUser = Objects.requireNonNullElse(user, DEFAULT_USER);
        dbPassword = Objects.requireNonNullElse(password, DEFAULT_PASSWORD);
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(connectionUrl, dbUser, dbPassword);
    }

    public static void initialize() {
        String url = firstNonNull(System.getProperty("focusnode.db.url"), System.getenv("FOCUSNODE_DB_URL"), connectionUrl);
        String user = firstNonNull(System.getProperty("focusnode.db.user"), System.getenv("FOCUSNODE_DB_USER"), dbUser);
        String password = firstNonNull(System.getProperty("focusnode.db.password"), System.getenv("FOCUSNODE_DB_PASSWORD"), dbPassword);

        setDatabaseConfig(url, user, password);

        try {
            org.flywaydb.core.Flyway flyway = org.flywaydb.core.Flyway.configure()
                .dataSource(connectionUrl, dbUser, dbPassword)
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

    private static String firstNonNull(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
