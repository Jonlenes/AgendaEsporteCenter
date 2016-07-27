package com.jonlenes.app.Modelo;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Jonlenes on 19/07/2016.
 */
public class ScheduledTimeBo {

    public List<ScheduledTime> getScheduledTimeByUser() throws SQLException {
        return new ScheduledTimeDao().getScheduledTimeByUser(UserBo.getIdUserActive());
    }

}
