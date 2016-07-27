package com.jonlenes.app.Modelo;

import com.jonlenes.app.DataBase.Connection;
import com.mysql.jdbc.ResultSet;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by asus on 19/07/2016.
 */
public class LocalDao {

    public List<Local> getAll() throws SQLException {
        String sql = "SELECT id, description FROM Local ";
        ResultSet resultSet = (ResultSet) Connection.getInstance().getExecute(sql);

        List<Local> locals = new ArrayList<>();
        while (resultSet.next())
            locals.add(new Local(resultSet.getLong("id"), resultSet.getString("description")));

        return locals;
    }
}
