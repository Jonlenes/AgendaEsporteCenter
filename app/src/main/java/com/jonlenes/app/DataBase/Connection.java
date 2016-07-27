package com.jonlenes.app.DataBase;

import com.mysql.jdbc.CommunicationsException;
import com.mysql.jdbc.PreparedStatement;

import java.net.SocketException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Created by Jonlenes on 15/07/2016.
 */
public class Connection {
    private static Connection ourInstance = null;
    private java.sql.Connection connection = null;

    //Dados para conexão
    private final String url;
    private final String dataBaseName;
    private final String userName;
    private final String password;
    private final String driver;


    public static Connection getInstance() throws SQLException {
        if (ourInstance == null)
            ourInstance = new Connection();

        return ourInstance;
    }

    private void reconnect() throws SQLException {
        if (connection == null)
            ourInstance = new Connection();
        else {
            for (int i = 0; i < 2; ++i) {
                try {
                    connection.prepareStatement("SELECT 1").executeQuery();
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    ourInstance = new Connection();
                }
            }

            throw new RuntimeException("Conexão ainda não disponível.");
        }
    }

    private Connection() throws SQLException {

        this.url = "jdbc:mysql://stevie.heliohost.org/";
        this.dataBaseName = "jonlenes_locacao";
        this.userName = "jonlenes_root";
        this.password = "jonlenes_root";
        this.driver = "com.mysql.jdbc.Driver";

        try {
            
            Class.forName(driver);
            DriverManager.setLoginTimeout(15);
            connection = DriverManager.getConnection(url + dataBaseName, userName, password);

        } catch (Exception e ) {
            e.printStackTrace();
            throw new RuntimeException("Falha de comunicação. Verifique conexão com a internet.");
        }
        
    }

    public boolean execute(String sql) throws SQLException {
        reconnect();
        return connection.prepareStatement(sql).execute();
    }

    public ResultSet getExecute(String sql) throws SQLException {
        reconnect();
        return connection.prepareStatement(sql).executeQuery();
    }

    public void openTransaction() throws SQLException {
        reconnect();
        connection.setAutoCommit(false);
    }

    public void commit() throws SQLException {
        reconnect();
        connection.commit();
        connection.setAutoCommit(true);
    }

    public void rollback() throws SQLException {
        reconnect();
        connection.rollback();
        connection.setAutoCommit(true);
    }

    public java.sql.PreparedStatement preparedStatement(String sql) throws SQLException {
        reconnect();
        return connection.prepareStatement(sql);
    }
}
