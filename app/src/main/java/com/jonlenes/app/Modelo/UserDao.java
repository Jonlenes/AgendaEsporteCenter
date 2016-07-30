/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jonlenes.app.Modelo;


import com.jonlenes.app.DataBase.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Administrador
 */
public class UserDao {

    public Long insert(User user) throws SQLException {
        String sql = "INSERT INTO User (name, password) VALUES (?, ?)";

        PreparedStatement preparedStatement = Connection.getInstance().preparedStatement(sql);

        preparedStatement.setString(1, user.getName());
        preparedStatement.setString(2, user.getPassword());

        preparedStatement.execute();

        return seachIdUserActive();
    }

    public void update(User user) throws SQLException {
        String sql = "UPDATE User SET name = ?, password = ? \n" +
            "WHERE id = ?";

        PreparedStatement preparedStatement = Connection.getInstance().preparedStatement(sql);

        preparedStatement.setString(1, user.getName());
        preparedStatement.setString(2, user.getPassword());
        preparedStatement.setLong(3, user.getId());

        preparedStatement.execute();
    }

    public User getUser(Long id) throws SQLException {
        String sql = "SELECT id, name, password FROM User \n" +
                "WHERE id = " + id;

        ResultSet resultSet = Connection.getInstance().getExecute(sql);

        if (resultSet.next())
            return new User(resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("password"));

        return null;
    }


    public User getUserByName(String name) throws SQLException {
        String sql = "SELECT id, name, password FROM User \n" +
                "HAVING UCASE(name) = ?";

        PreparedStatement preparedStatement = Connection.getInstance().preparedStatement(sql);
        preparedStatement.setString(1, name);
        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            return new User(resultSet.getLong("id"), resultSet.getString("name"), resultSet.getString("password"));
        }

        return null;
    }

    public User getUserByName(String name, Long descartThisId) throws SQLException {
        String sql = "SELECT id, name, password FROM User \n" +
                "HAVING UCASE(name) = ? AND id != ?";

        PreparedStatement preparedStatement = Connection.getInstance().preparedStatement(sql);
        preparedStatement.setString(1, name);
        preparedStatement.setLong(2, descartThisId);
        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            return new User(resultSet.getLong("id"), resultSet.getString("name"), resultSet.getString("password"));
        }

        return null;
    }


    private Long seachIdUserActive() throws SQLException {
        String sql = "SELECT MAX(id) as id FROM Client";
        ResultSet resultSet = Connection.getInstance().getExecute(sql);
        resultSet.next();
        return  resultSet.getLong("id");
    }
}
