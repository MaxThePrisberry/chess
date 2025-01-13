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
    public UserDataResult register(RegisterRequest request) throws HandlerTargetedException {
        try {
            UserData user = UserDAO.getUser(request.username());
        } catch (DataAccessException e) {
            UserDAO.createUser(request.username(), BCrypt.hashpw(request.password(), BCrypt.gensalt()), request.email());
            String authToken = UUID.randomUUID().toString();
            AuthDAO.createAuth(authToken, request.username());
            return new UserDataResult(request.username(), authToken);
        }
        throw new HandlerTargetedException(403, "Error: already taken");
    }

    public UserDataResult login(LoginRequest request) throws HandlerTargetedException {
        UserData user;
        try {
            user = UserDAO.getUser(request.username());
            if (!BCrypt.checkpw(request.password(), user.password())) {
                throw new AuthenticationException("Error: passwords do not match");
            }
            String authToken = UUID.randomUUID().toString();
            AuthDAO.createAuth(authToken, request.username());
            return new UserDataResult(request.username(), authToken);
        } catch (DataAccessException | AuthenticationException e) {
            throw new HandlerTargetedException(401, "Error: unauthorized");
        }
    }

    public void logout(LogoutRequest request) throws HandlerTargetedException {
        try {
            AuthDAO.deleteAuth(request.authToken());
        } catch (DataAccessException e) {
            throw new HandlerTargetedException(401, "Error: unauthorized");
        }
    }
}
