package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import service.model.*;

public class Phase3MasterService {
    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private UserDAO userDAO;

    private UserService userService;
    private GameService gameService;
    private DeleteService deleteService;

    public Phase3MasterService() {
        authDAO = new AuthDAO();
        gameDAO = new GameDAO();
        userDAO = new UserDAO();

        userService = new UserService();
        gameService = new GameService();
        deleteService = new DeleteService();
    }

    public UserDataResult register(RegisterRequest request) {
        
    }

    public UserDataResult login(LoginRequest request) {

    }

    public void logout(LogoutRequest request) {

    }

    public ListGamesResult listGames(ListGamesRequest request) {

    }

    public CreateGameResult createGame(CreateGameRequest request) {

    }

    public void joinGame(JoinGameRequest request) {

    }

    public void clear() {

    }
}
