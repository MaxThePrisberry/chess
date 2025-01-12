package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;

public class DeleteService {
    public void clear(AuthDAO authDAO, GameDAO gameDAO, UserDAO userDAO) {
        authDAO.clear();
        gameDAO.clear();
        userDAO.clear();
    }
}
