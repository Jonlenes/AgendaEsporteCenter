package com.jonlenes.app.DataBase;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Created by Jonlenes on 15/07/2016.
 */
public class Connection {
    private static Connection ourInstance = null;
    //Dados para conexão
    private final String url;
    private final String dataBaseName;
    private final String userName;
    private final String password;
    private final String driver;
    private java.sql.Connection connection = null;


    private Connection() throws SQLException {

        this.url = "jdbc:mysql://stevie.heliohost.org/";
        this.dataBaseName = "jonlenes_locacao";
        this.userName = "jonlenes_root";
        this.password = "jonlenes_root";
        this.driver = "com.mysql.jdbc.Driver";

        connect();
    }

    public static Connection getInstance() throws SQLException {
        if (ourInstance == null)
            ourInstance = new Connection();

        return ourInstance;
    }

    private void checkConnection() throws SQLException {
        if (ourInstance == null)
            ourInstance = new Connection();
        else if (connection == null || connection.isClosed()) {
            connect();
        } else {
            try {
                connection.prepareStatement("SELECT 1").executeQuery();
            } catch (Exception e) {
                e.printStackTrace();
                connect();
            }
        }
    }

    private void connect() {
        try {

            if (connection != null && !connection.isClosed())
                connection.close();

            Class.forName(driver);
            connection = DriverManager.getConnection(url + dataBaseName, userName, password);

        } catch (Exception e) {
            e.printStackTrace();
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e1) {
                }
            }
            throw new RuntimeException("Falha de comunicação. Verifique conexão com a internet.");
        }
    }

    public boolean execute(String sql) throws SQLException {
        checkConnection();
        java.sql.Statement statement = connection.createStatement();
        return statement.execute(sql);
    }

    public ResultSet getExecute(String sql) throws SQLException {
        checkConnection();
        java.sql.Statement statement = connection.createStatement();
        return statement.executeQuery(sql);
    }

    public java.sql.PreparedStatement preparedStatement(String sql) throws SQLException {
        checkConnection();
        return connection.prepareStatement(sql);
    }
}
