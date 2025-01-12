package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import service.model.*;

public class GameService {
    public ListGamesResult listGames(ListGamesRequest request, AuthDAO authDAO, GameDAO gameDAO) throws HandlerTargetedException {
        try {
            AuthData auth = authDAO.getAuth(request.authToken());
            return new ListGamesResult(gameDAO.listGames());
        } catch (DataAccessException e) {
            throw new HandlerTargetedException(401, "Error: unauthorized");
        }
    }

    public CreateGameResult createGame(CreateGameRequest request, AuthDAO authDAO, GameDAO gameDAO) throws HandlerTargetedException {
        try {
            AuthData auth = authDAO.getAuth(request.authToken());
            ChessGame newGame = new ChessGame();
            int gameID = gameDAO.createGame(null, null, request.gameName(), newGame);
            return new CreateGameResult(gameID);
        } catch (DataAccessException e) {
            throw new HandlerTargetedException(401, "Error: unauthorized");
        }
    }

    public void joinGame(JoinGameRequest request, AuthDAO authDAO, GameDAO gameDAO) throws HandlerTargetedException {
        try {
            AuthData auth = authDAO.getAuth(request.authToken());
            GameData game = gameDAO.getGame(request.gameID());
            if (game.blackUsername() != null && game.whiteUsername() != null) {
                throw new IllegalStateException("Error: no space for third player in chess game");
            } else if ((request.playerColor().equals("WHITE") ? game.whiteUsername() : game.blackUsername()) != null) {
                throw new IllegalStateException("Error: player color '" + request.playerColor() + "' is already taken");
            }
            if (request.playerColor().equals("WHITE")) {
                gameDAO.updateGame(request.gameID(), auth.username(), game.blackUsername(), game.gameName(), game.game());
            }
        } catch (DataAccessException e) {
            switch (e.getMessage()) {
                case "getAuth Error: No AuthData with given authToken" -> {
                    throw new HandlerTargetedException(401, "Error: unauthorized");
                }
                case "getGame Error: No game with given gameID" -> {
                    throw new HandlerTargetedException(400, "Error: no game with the ID given");
                }
            }
        } catch (IllegalStateException e) {
            throw new HandlerTargetedException(409, e.getMessage());
        }
    }
}
