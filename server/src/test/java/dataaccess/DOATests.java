package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
        try (Connection conn = DatabaseManager.getConnection()){
            try (PreparedStatement statement = conn.prepareStatement("DROP DATABASE chess;")) {
                statement.executeUpdate();
            }
        } catch (DataAccessException | SQLException e) {
            throw new RuntimeException(e);
        }
        DatabaseManager.createDatabase();
    }

    @BeforeEach
    void setUp() {
        UserDAO.setUp();
        GameDAO.setUp();
        AuthDAO.setUp();

        try (Connection conn = DatabaseManager.getConnection()){
            try (PreparedStatement statement = conn.prepareStatement("TRUNCATE TABLE auths;")) {
                statement.executeUpdate();
            } catch (SQLException ignored) {}
            try (PreparedStatement statement = conn.prepareStatement("TRUNCATE TABLE games;")) {
                statement.executeUpdate();
            }
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
        ChessGame chess_game = new ChessGame();
        int gameID = GameDAO.createGame("Potato", "Carrot", "Fruit Fight", chess_game);
        GameData game = GameDAO.getGame(gameID);
        assertEquals("Potato", game.whiteUsername());
        assertEquals("Carrot", game.blackUsername());
        assertEquals("Fruit Fight", game.gameName());
        assertEquals(chess_game, game.game());
    }

    @Test
    @DisplayName("Get Nonexistent Game")
    void getNonexistentGame() {
        assertThrows(DataAccessException.class, () -> GameDAO.getGame(1));
    }

    @Test
    @DisplayName("List Games Valid Test")
    void listGamesValid() {
        ChessGame chess_game = new ChessGame();
        int gameID1 = GameDAO.createGame("Potato", "Carrot", "Fruit Fight", chess_game);
        int gameID2 = GameDAO.createGame("Peanut Butter", "Celery", "Banana Bowl", chess_game);

        Set<GameData> expected = new HashSet<>();
        expected.add(new GameData(gameID1, "Potato", "Carrot", "Fruit Fight", chess_game));
        expected.add(new GameData(gameID2, "Peanut Butter", "Celery", "Banana Bowl", chess_game));

        Set<GameData> actual = assertDoesNotThrow(GameDAO::listGames);

        assertEquals(expected, actual);
    }
}
