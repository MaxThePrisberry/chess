package dataaccess;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.Data;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class DOATests {

    @BeforeAll
    static void createDatabase() throws DataAccessException {
        DatabaseManager.clearDatabase();
    }

    @BeforeEach
    void setUp() {
        UserDAO.setUp();
        GameDAO.setUp();
        AuthDAO.setUp();

        clearDatabase();
    }

    private void clearDatabase() {
        try (Connection conn = DatabaseManager.getConnection()){
            try (PreparedStatement statement = conn.prepareStatement("TRUNCATE TABLE auths;")) {
                statement.executeUpdate();
            } catch (SQLException ignored) {}
            try (PreparedStatement statement = conn.prepareStatement("TRUNCATE TABLE games;")) {
                statement.executeUpdate();
            }
            //Seeing if this helps the autograder to not develop cancer...
            try (PreparedStatement statement = conn.prepareStatement("TRUNCATE TABLE users;")) {
                statement.executeUpdate();
            }
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        } catch (SQLException ignored) {}
    }

    void assertEmpty() {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement statement = conn.prepareStatement("SELECT COUNT(*) FROM users;")) {
                ResultSet res = statement.executeQuery();
                res.next();
                assertEquals(0, res.getInt(1));
            }
            try (PreparedStatement statement = conn.prepareStatement("SELECT COUNT(*) FROM games;")) {
                ResultSet res = statement.executeQuery();
                res.next();
                assertEquals(0, res.getInt(1));
            }
            try (PreparedStatement statement = conn.prepareStatement("SELECT COUNT(*) FROM auths;")) {
                ResultSet res = statement.executeQuery();
                res.next();
                assertEquals(0, res.getInt(1));
            }
        } catch (DataAccessException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Clear Positive Test")
    void clearValid() {
        assertEmpty();

        UserDAO.createUser("Potato", "Farmer", "a@us.f");
        AuthDAO.createAuth("authtoken", "Potato");

        ChessGame game = new ChessGame();
        GameDAO.createGame("Potato", "Orange", "Fruit Bowl", game);

        UserDAO.clear();
        AuthDAO.clear();
        GameDAO.clear();

        assertEmpty();
    }

    @Test
    @DisplayName("Get User Positive Test")
    void getUserValid() throws DataAccessException {
        UserDAO.createUser("Potato", "Farmer", "a@us.f");
        UserData user = UserDAO.getUser("Potato");
        assertEquals("Potato", user.username());
        assertEquals("Farmer", user.password());
        assertEquals("a@us.f", user.email());

    }

    @Test
    @DisplayName("Get User With Nonexistent User")
    void getUserNonexistent() {
        assertThrows(DataAccessException.class, () -> UserDAO.getUser("Carrot"));
        UserDAO.createUser("Potato", "Farmer", "a@us.f");
        assertThrows(DataAccessException.class, () -> UserDAO.getUser("Carrot"));
    }

    @Test
    @DisplayName("Create User Valid Test")
    void createUserValid() {
        UserDAO.createUser("Potato", "Farmer", "a@us.f");
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement statement = conn.prepareStatement("SELECT COUNT(*) FROM users;")) {
                ResultSet res = statement.executeQuery();
                res.next();
                assertEquals(1, res.getInt(1));
            }
            try (PreparedStatement statement = conn.prepareStatement("SELECT * FROM users WHERE username = ?;")) {
                statement.setString(1, "Potato");
                ResultSet res = statement.executeQuery();
                res.next();
                assertEquals("Potato", res.getString("username"));
                assertEquals("Farmer", res.getString("password"));
                assertEquals("a@us.f", res.getString("email"));
            }
        } catch (DataAccessException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Create User Invalid Inputs")
    void createUserInvalidInputs() {
        assertThrows(RuntimeException.class, () -> UserDAO.createUser(null, null, null));
    }

    @Test
    @DisplayName("Get Auth Positive Test")
    void getAuthValid() throws DataAccessException {
        AuthDAO.createAuth("fakeAuthToken", "Potato");
        AuthData auth = AuthDAO.getAuth("fakeAuthToken");
        assertEquals("Potato", auth.username());
        assertEquals("fakeAuthToken", auth.authToken());
    }

    @Test
    @DisplayName("Get Auth With Nonexistent User")
    void getAuthNonexistent() {
        assertThrows(DataAccessException.class, () -> AuthDAO.getAuth("fakeAuthToken"));
        AuthDAO.createAuth("differentAuthToken", "Potato");
        assertThrows(DataAccessException.class, () -> AuthDAO.getAuth("fakeAuthToken"));
    }

    @Test
    @DisplayName("Create Auth Valid Test")
    void createAuthValid() {
        AuthDAO.createAuth("fakeAuthToken", "Potato");
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement statement = conn.prepareStatement("SELECT COUNT(*) FROM auths;")) {
                ResultSet res = statement.executeQuery();
                res.next();
                assertEquals(1, res.getInt(1));
            }
            try (PreparedStatement statement = conn.prepareStatement("SELECT * FROM auths WHERE auth_token = ?;")) {
                statement.setString(1, "fakeAuthToken");
                ResultSet res = statement.executeQuery();
                res.next();
                assertEquals("Potato", res.getString("username"));
                assertEquals("fakeAuthToken", res.getString("auth_token"));
            }
        } catch (DataAccessException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Create Auth Invalid Inputs")
    void createAuthInvalidInputs() {
        assertThrows(RuntimeException.class, () -> AuthDAO.createAuth(null, null));
    }

    @Test
    @DisplayName("Delete Auth Valid Test")
    void deleteAuthValid() {
        AuthDAO.createAuth("fakeAuthToken", "Potato");
        assertDoesNotThrow(() -> AuthDAO.deleteAuth("fakeAuthToken"));
    }

    @Test
    @DisplayName("Delete Nonexistent Auth")
    void deleteNonexistentAuth() {
        assertThrows(DataAccessException.class, () -> AuthDAO.deleteAuth("nonexistent"));
    }

    @Test
    @DisplayName("Create Game Valid Test")
    void createGameValid() {
        Gson gson = new Gson();
        ChessGame game = new ChessGame();
        GameDAO.createGame("Potato", "Carrot", "Fruit Fight", game);
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement statement = conn.prepareStatement("SELECT COUNT(*) FROM games;")) {
                ResultSet res = statement.executeQuery();
                res.next();
                assertEquals(1, res.getInt(1));
            }
            try (PreparedStatement statement = conn.prepareStatement("SELECT * FROM games WHERE game_id = ?;")) {
                statement.setInt(1, 1);
                ResultSet res = statement.executeQuery();
                res.next();
                assertEquals("Potato", res.getString("white_username"));
                assertEquals("Carrot", res.getString("black_username"));
                assertEquals("Fruit Fight", res.getString("game_name"));
                assertEquals(gson.toJson(game), res.getString("chess_game"));
            }
        } catch (DataAccessException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Create Game Invalid Inputs")
    void createGameInvalidInputs() {
        assertThrows(RuntimeException.class, () -> GameDAO.createGame(null, null, null, null));
    }

    @Test
    @DisplayName("Get Game Valid Test")
    void getGameValid() throws DataAccessException {
        ChessGame chessGame = new ChessGame();
        int gameID = GameDAO.createGame("Potato", "Carrot", "Fruit Fight", chessGame);
        GameData game = GameDAO.getGame(gameID);
        assertEquals("Potato", game.whiteUsername());
        assertEquals("Carrot", game.blackUsername());
        assertEquals("Fruit Fight", game.gameName());
        assertEquals(chessGame, game.game());
    }

    @Test
    @DisplayName("Get Nonexistent Game")
    void getNonexistentGame() {
        assertThrows(DataAccessException.class, () -> GameDAO.getGame(1));
    }

    @Test
    @DisplayName("List Games Valid Test")
    void listGamesValid() {
        ChessGame chessGame = new ChessGame();
        int gameID1 = GameDAO.createGame("Potato", "Carrot", "Fruit Fight", chessGame);
        int gameID2 = GameDAO.createGame("Peanut Butter", "Celery", "Banana Bowl", chessGame);

        Set<GameData> expected = new HashSet<>();
        expected.add(new GameData(gameID1, "Potato", "Carrot", "Fruit Fight", chessGame));
        expected.add(new GameData(gameID2, "Peanut Butter", "Celery", "Banana Bowl", chessGame));

        Set<GameData> actual = assertDoesNotThrow(GameDAO::listGames);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Update Game Valid Test")
    void updateGameValid() throws InvalidMoveException, DataAccessException {
        ChessGame chessGame = new ChessGame();
        int gameID1 = GameDAO.createGame("Potato", "Carrot", "Fruit Fight", chessGame);

        chessGame.makeMove(new ChessMove(new ChessPosition(2, 5), new ChessPosition(4, 5), null));
        assertDoesNotThrow(() -> GameDAO.updateGame(gameID1, "Peanut Butter", "Celery", "Banana Bowl", chessGame));

        GameData game = GameDAO.getGame(gameID1);

        assertEquals("Peanut Butter", game.whiteUsername());
        assertEquals("Celery", game.blackUsername());
        assertEquals("Banana Bowl", game.gameName());
        assertEquals(chessGame, game.game());
    }

    @Test
    @DisplayName("Update Game Invalid Inputs")
    void updateGameInvalidInputs() throws InvalidMoveException {
        ChessGame chessGame = new ChessGame();
        int gameID1 = GameDAO.createGame("Potato", "Carrot", "Fruit Fight", chessGame);

        assertThrows(RuntimeException.class, () -> GameDAO.updateGame(gameID1, null, "Celery", null, null));
    }
}
