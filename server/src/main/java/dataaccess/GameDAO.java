package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import model.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class GameDAO {

    private Gson gson = new Gson();

    public GameDAO() {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement statement = conn.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS games (
                        game_id INT AUTO_INCREMENT PRIMARY KEY,
                        white_username VARCHAR(255),
                        black_username VARCHAR(255),
                        game_name VARCHAR(255),
                        chess_game LONGTEXT
            );""")) {
                statement.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public int createGame(String whiteUsername, String blackUsername, String gameName, ChessGame game) {
        try (Connection conn = DatabaseManager.getConnection()){
            try (PreparedStatement createStatement = conn.prepareStatement("INSERT INTO games (white_username, " +
                    "black_password, game_name, chess_game) VALUES (?, ?, ?, ?);")) {
                createStatement.setString(1, whiteUsername);
                createStatement.setString(2, blackUsername);
                createStatement.setString(3, gameName);
                createStatement.setString(3, gson.toJson(game));
                createStatement.executeUpdate();

                ResultSet res = createStatement.getGeneratedKeys();
                res.next();
                return res.getInt("game_id");
            }
        } catch (DataAccessException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public GameData getGame(int gameID) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()){
            try (PreparedStatement statement = conn.prepareStatement("SELECT game_id, white_username," +
                    "black_username, game_name, chess_game FROM games WHERE game_id = ?;")) {
                statement.setInt(1, gameID);
                ResultSet res = statement.executeQuery();
                if (res.next()) {
                    ChessGame game = gson.fromJson(res.getString("chess_game"), ChessGame.class);
                    return new GameData(Integer.parseInt(res.getString("game_id")), res.getString("white_username"), res.getString("black_username"), res.getString("game_name"), game);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
