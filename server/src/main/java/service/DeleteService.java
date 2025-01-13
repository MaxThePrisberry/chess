package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;

public class DeleteService {
    public void clear() {
        AuthDAO.clear();
        GameDAO.clear();
        UserDAO.clear();
    }
}
