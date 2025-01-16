package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class GameDAO {

    private static final Gson gson = new Gson();

    public static void setUp() {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement statement = conn.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS games (
                        game_id INT AUTO_INCREMENT PRIMARY KEY,
                        white_username VARCHAR(255),
                        black_username VARCHAR(255),
                        game_name VARCHAR(255) NOT NULL,
                        chess_game LONGTEXT NOT NULL
            );""")) {
                statement.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static int createGame(String whiteUsername, String blackUsername, String gameName, ChessGame game) {
        try (Connection conn = DatabaseManager.getConnection()){
            try (PreparedStatement createStatement = conn.prepareStatement("INSERT INTO games (white_username, " +
                    "black_username, game_name, chess_game) VALUES (?, ?, ?, ?);", PreparedStatement.RETURN_GENERATED_KEYS)) {
                handleNullUsernames(whiteUsername, blackUsername, gameName, game, createStatement);
                createStatement.executeUpdate();

                ResultSet res = createStatement.getGeneratedKeys();
                res.next();
                return res.getInt(1);
            }
        } catch (DataAccessException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void handleNullUsernames(String whiteUsername, String blackUsername, String gameName, ChessGame game, PreparedStatement createStatement) throws SQLException {
        if (whiteUsername == null) {
            createStatement.setNull(1, Types.VARCHAR);
        } else {
            createStatement.setString(1, whiteUsername);
        }
        if (blackUsername == null) {
            createStatement.setNull(2, Types.VARCHAR);
        } else {
            createStatement.setString(2, blackUsername);
        }
        createStatement.setString(3, gameName);
        createStatement.setString(4, gson.toJson(game));
    }

    public static GameData getGame(int gameID) throws DataAccessException {
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
        } catch (DataAccessException | SQLException e) {
            throw new RuntimeException(e);
        }
        throw new DataAccessException("getGame Error: No game with given gameID");
    }

    public static Set<GameData> listGames() {
        try (Connection conn = DatabaseManager.getConnection()){
            try (PreparedStatement statement = conn.prepareStatement("SELECT * FROM games;")) {
                ResultSet res = statement.executeQuery();
                Set<GameData> games = new HashSet<>();
                while (res.next()) {
                    ChessGame game = gson.fromJson(res.getString("chess_game"), ChessGame.class);
                    games.add(new GameData(res.getInt("game_id"), res.getString("white_username"), res.getString("black_username"), res.getString("game_name"), game));
                }
                return games;
            }
        } catch (DataAccessException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateGame(GameData data) {
        updateGame(data.gameID(), data.whiteUsername(), data.blackUsername(), data.gameName(), data.game());
    }

    public static void updateGame(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
        try (Connection conn = DatabaseManager.getConnection()){
            try (PreparedStatement statement = conn.prepareStatement("SELECT game_id FROM games " +
                    "WHERE game_id = ?;")) {
                statement.setInt(1, gameID);
                ResultSet res = statement.executeQuery();
                if (res.next()) {
                    try (PreparedStatement updateStatement = conn.prepareStatement("UPDATE games SET white_username = ?, black_username = ?, game_name = ?, chess_game = ? WHERE game_id = ?;")) {
                        handleNullUsernames(whiteUsername, blackUsername, gameName, game, updateStatement);
                        updateStatement.setInt(5, gameID);
                        updateStatement.executeUpdate();
                    }
                }
            }
        } catch (DataAccessException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void clear() {
        try (Connection conn = DatabaseManager.getConnection()){
            try (PreparedStatement statement = conn.prepareStatement("TRUNCATE TABLE games;")) {
                statement.executeUpdate();
            }
        } catch (DataAccessException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
