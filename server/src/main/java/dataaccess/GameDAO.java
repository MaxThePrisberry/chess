package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.HashSet;
import java.util.Set;

public class GameDAO {

    private Set<GameData> database = new HashSet<>();
    private int gameIDCounter = 1;

    public int createGame(String whiteUsername, String blackUsername, String gameName, ChessGame game) {
        database.add(new GameData(gameIDCounter, whiteUsername, blackUsername, gameName, game));
        gameIDCounter++;
        return gameIDCounter - 1;
    }

    public GameData getGame(int gameID) throws DataAccessException {
        for (GameData game : database) {
            if (game.gameID() == gameID) {
                return game;
            }
        }
        throw new DataAccessException("getGame Error: No game with given gameID");
    }

    public Set<GameData> listGames() {
        return database;
    }

    public void deleteGame(int gameID) {
        database.removeIf(game -> game.gameID() == gameID);
    }

    public void updateGame(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
        deleteGame(gameID);
        database.add(new GameData(gameID, whiteUsername, blackUsername, gameName, game));
    }

    public void clear() {
        database.clear();
    }
}
