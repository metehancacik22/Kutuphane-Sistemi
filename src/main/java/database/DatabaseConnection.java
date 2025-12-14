package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//SİNGLETON tasarım deseni

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {

            String url = "jdbc:sqlite:kutuphane_final.db";

            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}