package com.jonlenes.app.Modelo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jonlenes on 19/07/2016.
 */
public class ScheduledTimeBo {

    public Map<Local, List<ScheduledTime>> getScheduledTimeByUser() throws SQLException {

        List<ScheduledTime> list = new ScheduledTimeDao().getScheduledTimeByUser(UserBo.getIdUserActive());
        Map<Local, List<ScheduledTime>> map = new LinkedHashMap<>();

        if (list.size() > 0) {

            Local local = list.get(0).getLocal();
            int i = 0;

            while (true) {
                List<ScheduledTime> listLocal = new ArrayList<>();
                while (i < list.size() && list.get(i).getLocal().getId().equals(local.getId())) {
                    listLocal.add(list.get(i));
                    ++i;
                }
                map.put(local, listLocal);
                if (i == list.size())
                    break;

                local = list.get(i).getLocal();
            }
        }

        return map;
    }

}
