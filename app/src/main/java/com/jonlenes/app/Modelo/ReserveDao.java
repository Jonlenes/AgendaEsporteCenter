package com.jonlenes.app.Modelo;

import com.jonlenes.app.DataBase.Connection;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jonlenes on 17/07/2016.
 */
public class ReserveDao {

    public void insert(Reserve time) throws SQLException {
        String sql = "INSERT INTO Reserve (dateDay, startTime, endTime, \n" +
                "duration, idLocal, idUser, idClient) VALUES (?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement preparedStatement = Connection.getInstance().preparedStatement(sql);

        preparedStatement.setDate(1, time.getDateDay());
        preparedStatement.setTime(2, time.getStartTime());
        preparedStatement.setTime(3, time.getEndTime());
        preparedStatement.setLong(4, time.getDuration());
        preparedStatement.setLong(5, time.getLocal().getId());
        preparedStatement.setLong(6, time.getUser().getId());
        preparedStatement.setLong(7, time.getClient().getId());

        preparedStatement.execute();
    }

    public void updade(Reserve time) throws SQLException {
        String sql = "UPDATE Reserve SET dateDay = ?, startTime = ?, endTime = ?, \n" +
                "duration = ?, idLocal = ?, idUser = ?, idClient = ? \n" +
                "WHERE id = ?";

        PreparedStatement preparedStatement = Connection.getInstance().preparedStatement(sql);

        preparedStatement.setDate(1, time.getDateDay());
        preparedStatement.setTime(2, time.getStartTime());
        preparedStatement.setTime(3, time.getEndTime());
        preparedStatement.setLong(4, time.getDuration());
        preparedStatement.setLong(5, time.getLocal().getId());
        preparedStatement.setLong(6, time.getUser().getId());
        preparedStatement.setLong(7, time.getClient().getId());
        preparedStatement.setLong(8, time.getId());

        preparedStatement.execute();
    }

    public void delete(Long id) throws SQLException {
        Connection.getInstance().execute("DELETE FROM Reserve WHERE id = " + id);
    }

    public List<Reserve> getReserveByUser(Long idUser) throws SQLException {

        String sql = "SELECT Reserve.id, dateDay, startTime, endTime, duration, \n" +
                "idUser, Local.description, Local.id AS idLocal FROM Reserve\n" +
                "INNER JOIN Local\n" +
                "   ON Local.id = Reserve.idLocal\n" +
                "WHERE idUser = " + idUser + "\n" +
                "ORDER BY Local.description, dateDay, startTime";

        ResultSet resultSet = Connection.getInstance().getExecute(sql);
        List<Reserve> Reserves = new ArrayList<>();

        while (resultSet.next()) {
            Reserves.add(new Reserve(resultSet.getLong("id"),
                    resultSet.getDate("dateDay"),
                    resultSet.getTime("startTime"),
                    resultSet.getTime("endTime"),
                    resultSet.getLong("duration"),
                    new User(resultSet.getLong("idUser")),
                    new Local(resultSet.getLong("idLocal"), resultSet.getString("description"))));
        }

        return Reserves;
    }

    public List<Reserve> getReserves(String locals, Date dateBegin, Date dateEnd) throws SQLException {
        String sql = "SELECT Reserve.id, dateDay, startTime, endTime, \n" +
                "duration, idLocal, idUser, User.name AS nameUser, \n" +
                "Client.id AS idClient, Client.name As nameClient  FROM Reserve\n" +
                "INNER JOIN User\n" +
                "   ON User.id = Reserve.idUser\n" +
                "INNER JOIN Client\n" +
                "   ON Client.id = Reserve.idClient\n" +
                "WHERE idLocal IN " + locals + "\n" +
                "   AND dateDay BETWEEN '" + dateBegin + "' AND '" + dateEnd + "'\n" +
                "ORDER BY dateDay, startTime";

        ResultSet resultSet = Connection.getInstance().getExecute(sql);
        List<Reserve> Reserves = new ArrayList<>();

        while (resultSet.next()) {
            Reserves.add(new Reserve(resultSet.getLong("id"),
                    resultSet.getDate("dateDay"),
                    resultSet.getTime("startTime"),
                    resultSet.getTime("endTime"),
                    resultSet.getLong("duration"),
                    new User(resultSet.getLong("idUser"), resultSet.getString("nameUser")),
                    new Local(resultSet.getLong("idLocal")),
                    new Client(resultSet.getLong("idClient"), resultSet.getString("nameClient"))));
        }

        return Reserves;
    }

    public Reserve getReserve(Long id) throws SQLException {
        String sql = "SELECT Reserve.id, Reserve.dateDay, Reserve.startTime, Reserve.endTime, \n" +
                "Reserve.duration, Local.id AS idLocal, Local.description AS descriptionLocal, idUser, \n" +
                "Client.id AS idClient, Client.name AS nameClient \n" +
                "FROM Reserve\n" +
                "INNER JOIN Local\n" +
                "   ON Local.id = Reserve.idLocal\n" +
                "INNER JOIN Client\n" +
                "   ON Client.id = Reserve.idClient\n" +
                "WHERE Reserve.id = " + id;

        ResultSet resultSet = Connection.getInstance().getExecute(sql);

        if (resultSet.next()) {
            return new Reserve(resultSet.getLong("id"),
                    resultSet.getDate("dateDay"),
                    resultSet.getTime("startTime"),
                    resultSet.getTime("endTime"),
                    resultSet.getLong("duration"),
                    new User(resultSet.getLong("idUser")),
                    new Local(resultSet.getLong("idLocal"), resultSet.getString("descriptionLocal")),
                    new Client(resultSet.getLong("idClient"), resultSet.getString("nameClient")));
        }

        return null;
    }
}
