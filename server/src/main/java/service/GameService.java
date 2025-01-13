package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import service.model.*;

public class GameService {
    public ListGamesResult listGames(ListGamesRequest request) throws HandlerTargetedException {
        try {
            AuthData auth = AuthDAO.getAuth(request.authToken());
            return new ListGamesResult(GameDAO.listGames());
        } catch (DataAccessException e) {
            throw new HandlerTargetedException(401, "Error: unauthorized");
        }
    }

    public CreateGameResult createGame(CreateGameRequest request) throws HandlerTargetedException {
        try {
            AuthData auth = AuthDAO.getAuth(request.authToken());
            ChessGame newGame = new ChessGame();
            int gameID = GameDAO.createGame(null, null, request.gameName(), newGame);
            return new CreateGameResult(gameID);
        } catch (DataAccessException e) {
            throw new HandlerTargetedException(401, "Error: unauthorized");
        }
    }

    public void joinGame(JoinGameRequest request) throws HandlerTargetedException {
        try {
            AuthData auth = AuthDAO.getAuth(request.authToken());
            GameData game = GameDAO.getGame(request.gameID());
            if ((request.playerColor().equals("WHITE") ? game.whiteUsername() : game.blackUsername()) != null) {
                throw new IllegalStateException("Error: already taken");
            }
            if (request.playerColor().equals("WHITE")) {
                GameDAO.updateGame(request.gameID(), auth.username(), game.blackUsername(), game.gameName(), game.game());
            } else {
                GameDAO.updateGame(request.gameID(), game.whiteUsername(), auth.username(), game.gameName(), game.game());
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
            throw new HandlerTargetedException(403, e.getMessage());
        }
    }
}
