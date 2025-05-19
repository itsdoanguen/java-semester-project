package dao;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static final String DB_URL;
    private static final String USER;
    private static final String PASS;
    static {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(".env")) {
            props.load(fis);
        } catch (Exception e) {
            e.printStackTrace();
        }
        DB_URL = props.getProperty("DB_URL", "");
        USER = props.getProperty("DB_USER", "");
        PASS = props.getProperty("DB_PASS", "");
    }
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }
}
