package com.jonlenes.app.Modelo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jonlenes on 19/07/2016.
 */
public class ReserveBo {

    public Map<Local, List<Reserve>> getReserveByUser() throws SQLException {

        List<Reserve> list = new ReserveDao().getReserveByUser(UserBo.getIdUserActive());
        Map<Local, List<Reserve>> map = new LinkedHashMap<>();

        if (list.size() > 0) {

            Local local = list.get(0).getLocal();
            int i = 0;

            while (true) {
                List<Reserve> listLocal = new ArrayList<>();
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
