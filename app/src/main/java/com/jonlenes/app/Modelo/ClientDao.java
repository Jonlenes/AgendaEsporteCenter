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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jonlenes on 16/07/2016.
 */

public class ClientDao {

    public void insert(Client client) throws SQLException {
        String sql = "INSERT INTO Client (name, telephone, image) VALUES (?, ?, ?)";

        PreparedStatement preparedStatement = Connection.getInstance().preparedStatement(sql);

        preparedStatement.setString(1, client.getName());
        preparedStatement.setString(2, client.getTelephone());
        preparedStatement.setBytes(3, client.getImage());

        preparedStatement.execute();
    }

    public void update(Client client) throws SQLException {
        String sql = "UPDATE Client SET name = ?, telephone = ?, image = ?\n" +
                "WHERE Id = " + client.getId();

        PreparedStatement preparedStatement = Connection.getInstance().preparedStatement(sql);

        preparedStatement.setString(1, client.getName());
        preparedStatement.setString(2, client.getTelephone());
        preparedStatement.setBytes(3, client.getImage());

        preparedStatement.execute();
    }

    public List<Client> getAll() throws SQLException {
        String sql = "SELECT Id, name, telephone, image FROM Client \n" +
                "ORDER BY name";

        ResultSet resultSet;
        List<Client> clients = new ArrayList<>();

        resultSet = Connection.getInstance().getExecute(sql);

        while (resultSet.next()) {
            clients.add(new Client(resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("telephone"),
                    resultSet.getBytes("image")));
        }

        return clients;
    }

    public void delete(Long id) throws SQLException {
        Connection.getInstance().execute("DELETE FROM Client WHERE id = " + id);
    }

    public Client getClient(Long idClient) throws SQLException {
        String sql = "SELECT id, name, telephone, image FROM Client \n" +
                "WHERE id = " + idClient;

        ResultSet resultSet;
        resultSet = Connection.getInstance().getExecute(sql);

        if (resultSet.next()) {
            return new Client(resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("telephone"),
                    resultSet.getBytes("image"));
        }

        return null;
    }

    public List<Client> getAllWithoutImage() throws SQLException {
        String sql = "SELECT Id, name, telephone FROM Client \n" +
                "ORDER BY name";

        ResultSet resultSet;
        List<Client> clients = new ArrayList<>();

        resultSet = Connection.getInstance().getExecute(sql);

        while (resultSet.next()) {
            clients.add(new Client(resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("telephone")));
        }

        return clients;
    }
}
