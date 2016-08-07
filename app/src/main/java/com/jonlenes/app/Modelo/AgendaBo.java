package com.jonlenes.app.Modelo;

import android.util.Log;

import com.jonlenes.app.DataBase.Connection;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jonlenes on 17/07/2016.
 */
public class AgendaBo {

    private final Time beginTimeValid = Time.valueOf("08:00:00");
    private final Time endTimeValid = Time.valueOf("24:00:00");

    public Map<Date, List<Reserve>> getReserves(Date dateBegin, Date dateEnd, List<Local> locals, Boolean isReserved) throws SQLException {

        ReserveDao reserveDao = new ReserveDao();
        Map<Date, List<Reserve>> map = new LinkedHashMap<>();
		Time timeInitial = new Time(beginTimeValid.getTime());
        Date datePrevius;
        String strLocals = "";

        //Monto a string com os ids dos locais
        //------------------------------------------------------------------------------------------
        for (Local local : locals)
            strLocals += "," + local.getId();
        strLocals = strLocals.substring(1);
        //------------------------------------------------------------------------------------------

        if (isReserved) {

            List<Reserve> reserveList = reserveDao.getReserves(strLocals, dateBegin, dateEnd);
            int indexBegin = 0;

            datePrevius = new Date(reserveList.get(0).getDateDay().getTime());
            for (int i = 0; i < reserveList.size(); ++i) {
                Reserve reserve = reserveList.get(i);
                if (reserve.getDateDay().getTime() != datePrevius.getTime()) {
                    map.put(datePrevius, reserveList.subList(indexBegin, i - 1));
                    indexBegin = i;
                    datePrevius = new Date(reserve.getDateDay().getTime());
                }
            }
        }

        /*for (Local local : locals) {
            List<Reserve> listTimes = timeDao.getReserveByLocal(local.getId(), dateBegin, dateEnd);

            if (!isReserved) {
                List<Reserve> listTimesFree = new ArrayList<>();

                if (listTimes.size() == 0) {

                    Calendar c = Calendar.getInstance();
                    c.setTime(dateBegin);
                    while (c.getTime().getTime() <= dateEnd.getTime()) {
                        listTimesFree.add(new Reserve(new Date(c.getTime().getTime()), beginTimeValid, endTimeValid,
                                (endTimeValid.getTime() - beginTimeValid.getTime()) / (60000)));
                        c.add(Calendar.DAY_OF_YEAR, 1);
                    }

                } else {

                    datePrevius = new Date(listTimes.get(0).getDateDay().getTime());

                    for (Reserve time : listTimes) {
                        if (datePrevius.getTime() != time.getDateDay().getTime()) {
                            datePrevius = new Date(time.getDateDay().getTime());
                            timeInitial = new Time(beginTimeValid.getTime());
                        }

                        Long duration = (time.getStartTime().getTime() - timeInitial.getTime()) / 60000;
                        if (duration > 0) {
                            listTimesFree.add(new Reserve(time.getDateDay(), timeInitial, time.getStartTime(), duration));
                        }
                        timeInitial = new Time(time.getEndTime().getTime());
                    }

                    Time time = listTimes.get(listTimes.size() - 1).getEndTime();
                    Long duration = (endTimeValid.getTime() - time.getTime()) / 60000;
                    if (duration > 0 && (!endTimeValid.toString().equals(time.toString())) )
                        listTimesFree.add(new Reserve(time, endTimeValid, duration));
                }

                map.put(local, listTimesFree);
            } else {
                if (listTimes.size() > 0)
                    map.put(local, listTimes);
            }
        }*/

        return map;
    }
	
	public void marcarHorario(Reserve Reserve, boolean isRecorrenteMensal) throws ParseException, SQLException {

        DateFormat formatterTime = new SimpleDateFormat("HH:mm:ss");
        DateFormat formatterDate = new SimpleDateFormat("dd/MM/yyyy");

        java.util.Date today = formatterDate.parse(formatterDate.format(new java.util.Date()));
        Time timeActual = new Time(formatterTime.parse(formatterTime.format(new java.util.Date())).getTime());


        if (Reserve.getDateDay().getTime() < today.getTime())
            throw new RuntimeException("A data da reserva deve ser maior ou igual a de hoje.");

        if (Reserve.getDateDay().getTime() == today.getTime() && Reserve.getStartTime().getTime() < timeActual.getTime())
            throw new RuntimeException("A hora da reserva deve ser maior ou igual a hora atual");

        if (Reserve.getStartTime().getTime() < beginTimeValid.getTime() || Reserve.getEndTime().getTime() > endTimeValid.getTime())
            throw new RuntimeException("A reserva deve está entre " + beginTimeValid.toString() + " e " + endTimeValid.toString());

        List<Date> dates = new ArrayList<>();

        dates.add(Reserve.getDateDay());
        if (isRecorrenteMensal) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Reserve.getDateDay());

            for (int i = 0; i < 3; ++i) {
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
                dates.add(new Date(calendar.getTime().getTime()));
            }
        }

        String sql = "SELECT id FROM Reserve\n" +
                "WHERE dateDay = ?\n" +
                "   AND idLocal = ?\n" +
                "   AND ( ( ? >= startTime  AND  ? < endTime) OR\n" +
                "       ( ? >= startTime  AND  ? < endTime) )\n";
        if (Reserve.getId() != -1)
            sql += "   AND id != " + Reserve.getId();

        for (Date date : dates) {
            Reserve.setDateDay(new Date(date.getTime()));
            PreparedStatement preparedStatement = Connection.getInstance().preparedStatement(sql);

            preparedStatement.setDate(1, Reserve.getDateDay());
            preparedStatement.setLong(2, Reserve.getLocal().getId());
            preparedStatement.setTime(3, Reserve.getStartTime());
            preparedStatement.setTime(4, Reserve.getStartTime());
            preparedStatement.setTime(5, Reserve.getEndTime());
            preparedStatement.setTime(6, Reserve.getEndTime());

            if (preparedStatement.executeQuery().next())
                throw new RuntimeException("Esse horário no dia " + new SimpleDateFormat("dd/MM/yyyy").format(date) + " não está disponível.");
        }

        Reserve.setUser(new User(UserBo.getIdUserActive()));
        for (Date date : dates) {
            Reserve.setDateDay(new Date(date.getTime()));
            if (date == dates.get(0) && Reserve.getId() != -1)
                new ReserveDao().updade(Reserve);
            else
                new ReserveDao().insert(Reserve);
        }
    }

    public Reserve getReserva(Long id) throws SQLException {

        String sql = "SELECT Reserve.id, Reserve.dateDay, Reserve.startTime, Reserve.endTime, \n" +
                "Reserve.duration, Local.id AS idLocal, Local.description AS descriptionLocal, User.id AS idUser, \n" +
                "User.name AS nameUser, Client.id AS idClient, Client.name AS nameClient, Client.telephone AS telephoneClient, \n" +
                "   Client.image AS imgeClient \n" +
                "FROM Reserve\n" +
                "INNER JOIN Local\n" +
                "   ON Local.id = Reserve.idLocal\n" +
                "INNER JOIN User\n" +
                "   ON User.id = Reserve.idUser\n" +
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
                    new User(resultSet.getLong("idUser"), resultSet.getString("nameUser")),
                    new Local(resultSet.getLong("idLocal"), resultSet.getString("descriptionLocal")),
                    new Client(resultSet.getLong("idClient"), resultSet.getString("nameClient"),
                            resultSet.getString("telephoneClient"), resultSet.getBytes("imgeClient")));
        }

        return null;
    }
}