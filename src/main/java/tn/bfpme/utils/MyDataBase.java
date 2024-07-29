package tn.bfpme.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {
    private static MyDataBase instance;

    private final String URL = "jdbc:mysql://192.168.1.137:3306/bfpmeconge";
    private final String USERNAME = "ala";
    private final String PASSWORD = "ala";

    private Connection cnx;

    private MyDataBase() {
        try {
            System.out.println("Attempting to connect to database...");
            cnx = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("DATABASE: Connection Successful");
        } catch (SQLException e) {
            System.err.println("DATABASE: Connection Failed");
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static synchronized MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    public synchronized Connection getCnx() {
        try {
            if (cnx == null || cnx.isClosed()) {
                System.out.println("Attempting to reconnect to database...");
                cnx = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            }
        } catch (SQLException e) {
            System.err.println("DATABASE: Reconnection Failed");
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
        }
        return cnx;
    }
}
