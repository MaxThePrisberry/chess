package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.HandlerTargetedException;
import dataaccess.UserDAO;
import service.model.*;

public class Phase3MasterService {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final UserDAO userDAO;

    private final UserService userService;
    private final GameService gameService;
    private final DeleteService deleteService;

    public Phase3MasterService() {
        authDAO = new AuthDAO();
        gameDAO = new GameDAO();
        userDAO = new UserDAO();

        userService = new UserService();
        gameService = new GameService();
        deleteService = new DeleteService();
    }

    public UserDataResult register(RegisterRequest request) throws HandlerTargetedException {
        return userService.register(request, userDAO, authDAO);
    }

    public UserDataResult login(LoginRequest request) throws HandlerTargetedException {
        return userService.login(request, userDAO, authDAO);
    }

    public void logout(LogoutRequest request) throws HandlerTargetedException {
        userService.logout(request, authDAO);
    }

    public ListGamesResult listGames(ListGamesRequest request) throws HandlerTargetedException {
        return gameService.listGames(request, authDAO, gameDAO);
    }

    public CreateGameResult createGame(CreateGameRequest request) throws HandlerTargetedException {
        return gameService.createGame(request, authDAO, gameDAO);
    }

    public void joinGame(JoinGameRequest request) throws HandlerTargetedException {
        gameService.joinGame(request, authDAO, gameDAO);
    }

    public void clear() {
        deleteService.clear(authDAO, gameDAO, userDAO);
    }
}
