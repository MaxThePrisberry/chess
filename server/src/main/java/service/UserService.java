package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.HandlerTargetedException;
import dataaccess.UserDAO;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import service.model.LoginRequest;
import service.model.LogoutRequest;
import service.model.RegisterRequest;
import service.model.UserDataResult;

import javax.naming.AuthenticationException;
import java.util.UUID;

public class UserService {
    public UserDataResult register(RegisterRequest request, UserDAO userDAO, AuthDAO authDAO) throws HandlerTargetedException {
        try {
            UserData user = userDAO.getUser(request.username());
        } catch (DataAccessException e) {
            userDAO.createUser(request.username(), BCrypt.hashpw(request.password(), BCrypt.gensalt()), request.email());
            String authToken = UUID.randomUUID().toString();
            authDAO.createAuth(authToken, request.username());
            return new UserDataResult(request.username(), authToken);
        }
        throw new HandlerTargetedException(403, "Error: already taken");
    }

    public UserDataResult login(LoginRequest request, UserDAO userDAO, AuthDAO authDAO) throws HandlerTargetedException {
        UserData user;
        try {
            user = userDAO.getUser(request.username());
            if (!BCrypt.checkpw(request.password(), user.password())) {
                throw new AuthenticationException("Error: passwords do not match");
            }
            String authToken = UUID.randomUUID().toString();
            authDAO.createAuth(authToken, request.username());
            return new UserDataResult(request.username(), authToken);
        } catch (DataAccessException | AuthenticationException e) {
            throw new HandlerTargetedException(401, "Error: unauthorized");
        }
    }

    public void logout(LogoutRequest request, AuthDAO authDAO) throws HandlerTargetedException {
        try {
            authDAO.deleteAuth(request.authToken());
        } catch (DataAccessException e) {
            throw new HandlerTargetedException(401, "Error: unauthorized");
        }
    }
}
