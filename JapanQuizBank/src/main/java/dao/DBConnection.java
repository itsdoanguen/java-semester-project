package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String DB_URL = "jdbc:sqlserver://NUYEN:1433;databaseName=javaDb;encrypt=true;trustServerCertificate=true";
    private static final String USER = "sa";
    private static final String PASS = "nguyen2509upu";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }
}
