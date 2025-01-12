package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import service.model.LoginRequest;
import service.model.LogoutRequest;
import service.model.RegisterRequest;
import service.model.UserDataResult;

public class UserService {
    public UserDataResult register(RegisterRequest request, UserDAO userDAO, AuthDAO authDAO) {

    }

    public UserDataResult login(LoginRequest request, UserDAO userDAO, AuthDAO authDAO) {

    }

    public void logout(LogoutRequest request, AuthDAO authDAO) {

    }
}
