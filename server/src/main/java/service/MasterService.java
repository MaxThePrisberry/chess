package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.HandlerTargetedException;
import dataaccess.UserDAO;
import service.model.*;

public class MasterService {

    private final UserService userService;
    private final GameService gameService;
    private final DeleteService deleteService;

    public MasterService() {
        userService = new UserService();
        gameService = new GameService();
        deleteService = new DeleteService();

        UserDAO.setUp();
        GameDAO.setUp();
        AuthDAO.setUp();
    }

    public UserDataResult register(RegisterRequest request) throws HandlerTargetedException {
        return userService.register(request);
    }

    public UserDataResult login(LoginRequest request) throws HandlerTargetedException {
        return userService.login(request);
    }

    public void logout(LogoutRequest request) throws HandlerTargetedException {
        userService.logout(request);
    }

    public ListGamesResult listGames(ListGamesRequest request) throws HandlerTargetedException {
        return gameService.listGames(request);
    }

    public CreateGameResult createGame(CreateGameRequest request) throws HandlerTargetedException {
        return gameService.createGame(request);
    }

    public void joinGame(JoinGameRequest request) throws HandlerTargetedException {
        gameService.joinGame(request);
    }

    public void clear() {
        deleteService.clear();
    }
}
