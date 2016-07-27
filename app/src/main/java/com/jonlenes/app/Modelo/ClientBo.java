/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jonlenes.app.Modelo;


import com.jonlenes.app.DataBase.Connection;
import com.mysql.jdbc.ResultSet;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jonlenes on 16/07/2016.
 */

public class ClientBo {

    public void delete(Long id) throws SQLException {
        String sql = "SELECT id FROM ScheduledTime \n" +
                "WHERE idClient = " + id;

        ResultSet resultSet = (ResultSet) Connection.getInstance().getExecute(sql);
        if (resultSet.next()) {
            throw new RuntimeException("Não foi possível excluir o cliente, pois o mesmo possui horário reservado.");
        }

        new ClientDao().delete(id);
    }
}
