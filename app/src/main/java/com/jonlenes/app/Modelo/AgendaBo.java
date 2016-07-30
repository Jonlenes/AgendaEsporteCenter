package com.jonlenes.app.Modelo;

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

    public Map<Local, List<ScheduledTime>> getHorarios(Date dateBegin, Date dateEnd, List<Local> locals, Boolean isReserved) throws SQLException {

        ScheduledTimeDao timeDao = new ScheduledTimeDao();
        Map<Local, List<ScheduledTime>> map = new LinkedHashMap<>();
		Time timeInitial = new Time(beginTimeValid.getTime());
        Date datePrevius = null;


        for (Local local : locals) {
            List<ScheduledTime> listTimes = timeDao.getScheduledTimeByLocal(local.getId(), dateBegin, dateEnd);

            if (!isReserved) {
                List<ScheduledTime> listTimesFree = new ArrayList<>();

                if (listTimes.size() == 0) {

                    Calendar c = Calendar.getInstance();
                    c.setTime(dateBegin);
                    while (c.getTime().getTime() <= dateEnd.getTime()) {
                        listTimesFree.add(new ScheduledTime(new Date(c.getTime().getTime()), beginTimeValid, endTimeValid,
                                (endTimeValid.getTime() - beginTimeValid.getTime()) / (60000)));
                        c.add(Calendar.DAY_OF_YEAR, 1);
                    }

                } else {

                    datePrevius = new Date(listTimes.get(0).getDateDay().getTime());

                    for (ScheduledTime time : listTimes) {
                        if (datePrevius.getTime() != time.getDateDay().getTime()) {
                            datePrevius = new Date(time.getDateDay().getTime());
                            timeInitial = new Time(beginTimeValid.getTime());
                        }

                        Long duration = (time.getStartTime().getTime() - timeInitial.getTime()) / 60000;
                        if (duration > 0) {
                            listTimesFree.add(new ScheduledTime(time.getDateDay(), timeInitial, time.getStartTime(), duration));
                        }
                        timeInitial = new Time(time.getEndTime().getTime());
                    }

                    Time time = listTimes.get(listTimes.size() - 1).getEndTime();
                    Long duration = (endTimeValid.getTime() - time.getTime()) / 60000;
                    if (duration > 0 && (!endTimeValid.toString().equals(time.toString())) )
                        listTimesFree.add(new ScheduledTime(time, endTimeValid, duration));
                }

                map.put(local, listTimesFree);
            } else {
                if (listTimes.size() > 0)
                    map.put(local, listTimes);
            }
        }

        return map;
    }
	
	public void marcarHorario(ScheduledTime scheduledTime, boolean isRecorrenteMensal) throws ParseException, SQLException {

        DateFormat formatterTime = new SimpleDateFormat("HH:mm:ss");
        DateFormat formatterDate = new SimpleDateFormat("dd/MM/yyyy");

        java.util.Date today = formatterDate.parse(formatterDate.format(new java.util.Date()));
        Time timeActual = new Time(formatterTime.parse(formatterTime.format(new java.util.Date())).getTime());


        if (scheduledTime.getDateDay().getTime() < today.getTime())
            throw new RuntimeException("A data da reserva deve ser maior ou igual a de hoje.");

        if (scheduledTime.getDateDay().getTime() == today.getTime() && scheduledTime.getStartTime().getTime() < timeActual.getTime())
            throw new RuntimeException("A hora da reserva deve ser maior ou igual a hora atual");

        if (scheduledTime.getStartTime().getTime() < beginTimeValid.getTime() || scheduledTime.getEndTime().getTime() > endTimeValid.getTime())
            throw new RuntimeException("A reserva deve está entre " + beginTimeValid.toString() + " e " + endTimeValid.toString());

        List<Date> dates = new ArrayList<>();

        dates.add(scheduledTime.getDateDay());
        if (isRecorrenteMensal) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(scheduledTime.getDateDay());

            for (int i = 0; i < 3; ++i) {
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
                dates.add(new Date(calendar.getTime().getTime()));
            }
        }

        String sql = "SELECT id FROM ScheduledTime\n" +
                "WHERE dateDay = ?\n" +
                "   AND idLocal = ?\n" +
                "   AND ( ( ? >= startTime  AND  ? < endTime) OR\n" +
                "       ( ? >= startTime  AND  ? < endTime) )\n";
        if (scheduledTime.getId() != -1)
            sql += "   AND id != " + scheduledTime.getId();

        for (Date date : dates) {
            scheduledTime.setDateDay(new Date(date.getTime()));
            PreparedStatement preparedStatement = Connection.getInstance().preparedStatement(sql);

            preparedStatement.setDate(1, scheduledTime.getDateDay());
            preparedStatement.setLong(2, scheduledTime.getLocal().getId());
            preparedStatement.setTime(3, scheduledTime.getStartTime());
            preparedStatement.setTime(4, scheduledTime.getStartTime());
            preparedStatement.setTime(5, scheduledTime.getEndTime());
            preparedStatement.setTime(6, scheduledTime.getEndTime());

            if (preparedStatement.executeQuery().next())
                throw new RuntimeException("Esse horário no dia " + new SimpleDateFormat("dd/MM/yyyy").format(date) + " não está disponível.");
        }

        scheduledTime.setUser(new User(UserBo.getIdUserActive()));
        for (Date date : dates) {
            scheduledTime.setDateDay(new Date(date.getTime()));
            if (date == dates.get(0) && scheduledTime.getId() != -1)
                new ScheduledTimeDao().updade(scheduledTime);
            else
                new ScheduledTimeDao().insert(scheduledTime);
        }
    }

    public ScheduledTime getReserva(Long id) throws SQLException {

        String sql = "SELECT ScheduledTime.id, ScheduledTime.dateDay, ScheduledTime.startTime, ScheduledTime.endTime, \n" +
                "ScheduledTime.duration, Local.id AS idLocal, Local.description AS descriptionLocal, User.id AS idUser, \n" +
                "User.name AS nameUser, Client.id AS idClient, Client.name AS nameClient, Client.telephone AS telephoneClient, \n" +
                "   Client.image AS imgeClient \n" +
                "FROM ScheduledTime\n" +
                "INNER JOIN Local\n" +
                "   ON Local.id = ScheduledTime.idLocal\n" +
                "INNER JOIN User\n" +
                "   ON User.id = ScheduledTime.idUser\n" +
                "INNER JOIN Client\n" +
                "   ON Client.id = ScheduledTime.idClient\n" +
                "WHERE ScheduledTime.id = " + id;

        ResultSet resultSet = Connection.getInstance().getExecute(sql);

        if (resultSet.next()) {
            return new ScheduledTime(resultSet.getLong("id"),
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