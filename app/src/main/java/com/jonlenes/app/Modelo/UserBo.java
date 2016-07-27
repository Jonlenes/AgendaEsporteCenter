package com.jonlenes.app.Modelo;

import com.jonlenes.app.DataBase.Connection;
import com.mysql.jdbc.ResultSet;

import java.sql.SQLException;

/**
 * Created by asus on 23/07/2016.
 */
public class UserBo {
    private static Long idUserActive = -1l;

    public void login(String name, String password) throws SQLException {

        User user = new UserDao().getUserByName(name.toUpperCase());

        if (user != null) {
            if (!user.getPassword().equals(password))
                throw new RuntimeException("Senha incorreta.");
            idUserActive = user.getId();

        } else
            throw new RuntimeException("Usuário não cadastrado.");

    }

    public void insert(User user) throws SQLException {
        UserDao userDao = new UserDao();

        if (userDao.getUserByName(user.getName().toUpperCase()) != null) {
            throw new RuntimeException("Nome de usuário já existe");
        }
        idUserActive = userDao.insert(user);
    }

    public void update(User user) throws SQLException {
        UserDao userDao = new UserDao();

        if (userDao.getUserByName(user.getName().toUpperCase(), user.getId()) != null) {
            throw new RuntimeException("Nome de usuário já existe");
        }

        userDao.update(user);
    }

    public User getUserActive() throws SQLException {
        return new UserDao().getUser(idUserActive);
    }





    public static void setIdUserActive(Long idUserActive) {
        UserBo.idUserActive = idUserActive;
    }

    public static Long getIdUserActive() {
        return UserBo.idUserActive;
    }
}
