package service;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.HandlerTargetedException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceTests {

    private final MasterService service = new MasterService();

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
        try (Connection conn = DatabaseManager.getConnection()){
            try (PreparedStatement statement = conn.prepareStatement("TRUNCATE TABLE auths;")) {
                statement.executeUpdate();
            } catch (SQLException e) {}
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

    @Test
    @DisplayName("Register Positive Test")
    void registerValidUser() throws HandlerTargetedException {
        UserDataResult user = assertDoesNotThrow(() -> service.register(new RegisterRequest("Maxwell", "Pr1sbrey", "spam@gmail.com")));
        assertFalse(user.authToken().isBlank());
        assertEquals("Maxwell", user.username());
    }

    @Test
    @DisplayName("Register Repeat User")
    void registerRepeatUser() throws HandlerTargetedException {
        //Register first user
        service.register(new RegisterRequest("Maxwell", "Pr1sbrey", "spam@gmail.com"));

        //Ensure that attempting to re-register under identical username produces exception
        assertThrows(HandlerTargetedException.class, () -> service.register(new RegisterRequest("Maxwell", "Potato", "spam@ail.com")));
    }

    @Test
    @DisplayName("Login Positive Test")
    void loginValid() throws HandlerTargetedException {
        service.register(new RegisterRequest("Maxwell", "Pr1sbrey", "spam@gmail.com"));
        assertDoesNotThrow(() -> service.login(new LoginRequest("Maxwell", "Pr1sbrey")));
    }

    @Test
    @DisplayName("Login Incorrect Password")
    void loginBadPassword() throws HandlerTargetedException {
        service.register(new RegisterRequest("Maxwell", "Pr1sbrey", "spam@gmail.com"));
        assertThrows(HandlerTargetedException.class, () -> service.login(new LoginRequest("Maxwell", "Incorrect")));
    }

    @Test
    @DisplayName("Login Nonexistent User")
    void loginUnregisteredUser() throws HandlerTargetedException {
        service.register(new RegisterRequest("Maxwell", "Pr1sbrey", "spam@gmail.com"));
        assertThrows(HandlerTargetedException.class, () -> service.login(new LoginRequest("Nathan", "Incorrect")));
    }

    @Test
    @DisplayName("Logout Positive Test")
    void logoutValid() throws HandlerTargetedException {
        UserDataResult user = service.register(new RegisterRequest("Maxwell", "Pr1sbrey", "spam@gmail.com"));
        assertDoesNotThrow(() -> service.logout(new LogoutRequest(user.authToken())));
    }

    @Test
    @DisplayName("Logout Nonexistent User Filled Database")
    void logoutBadFilled() throws HandlerTargetedException {
        service.register(new RegisterRequest("Maxwell", "Pr1sbrey", "spam@gmail.com"));
        assertThrows(HandlerTargetedException.class, () -> service.logout(new LogoutRequest("Faked authToken")));
    }

    @Test
    @DisplayName("Logout Nonexistent User Empty Database")
    void logoutBadEmpty() throws HandlerTargetedException {
        assertThrows(HandlerTargetedException.class, () -> service.logout(new LogoutRequest("This is my madeup authToken")));
    }

    @Test
    @DisplayName("List Games Positive Test")
    void listGamesValid() throws HandlerTargetedException {
        UserDataResult user = service.register(new RegisterRequest("Maxwell", "Pr1sbrey", "spam@gmail.com"));
        ListGamesResult result = assertDoesNotThrow(() -> service.listGames(new ListGamesRequest(user.authToken())));
        assertTrue(result.games().isEmpty());
    }

    @Test
    @DisplayName("List Games Invalid Auth")
    void listGamesInvalidAuth() {
        assertThrows(HandlerTargetedException.class, () -> service.listGames(new ListGamesRequest("HahaPotato")));
    }

    @Test
    @DisplayName("Create Game Positive Test")
    void createGameValid() throws HandlerTargetedException {
        UserDataResult user = service.register(new RegisterRequest("Maxwell", "Pr1sbrey", "spam@gmail.com"));
        CreateGameResult result = assertDoesNotThrow(() -> service.createGame(new CreateGameRequest(user.authToken(), "fakeGame")));
        assertEquals(1, result.gameID());

        result = assertDoesNotThrow(() -> service.createGame(new CreateGameRequest(user.authToken(), "fakeGame")));
        assertEquals(2, result.gameID());
    }

    @Test
    @DisplayName("Create Game Invalid Auth")
    void createGameInvalidAuth() {
        assertThrows(HandlerTargetedException.class, () -> service.createGame(new CreateGameRequest("FakeAuth", "fakeGame")));
    }

    @Test
    @DisplayName("Join Game Positive Test")
    void joinGameValid() throws HandlerTargetedException {
        UserDataResult user = service.register(new RegisterRequest("Maxwell", "Pr1sbrey", "spam@gmail.com"));
        service.createGame(new CreateGameRequest(user.authToken(), "Game 1"));
        assertDoesNotThrow(() -> service.joinGame(new JoinGameRequest(user.authToken(), "WHITE", 1)));
    }

    @Test
    @DisplayName("Join Nonexistent Game")
    void joinGhostGame() throws HandlerTargetedException {
        UserDataResult user = service.register(new RegisterRequest("Maxwell", "Pr1sbrey", "spam@gmail.com"));
        assertThrows(HandlerTargetedException.class, () -> service.joinGame(new JoinGameRequest(user.authToken(), "WHITE", 1)));
    }

    @Test
    @DisplayName("Join Game Side Taken")
    void joinGameOccupied() throws HandlerTargetedException {
        UserDataResult user = service.register(new RegisterRequest("Maxwell", "Pr1sbrey", "spam@gmail.com"));
        service.createGame(new CreateGameRequest(user.authToken(), "Game 1"));
        assertDoesNotThrow(() -> service.joinGame(new JoinGameRequest(user.authToken(), "WHITE", 1)));
        assertThrows(HandlerTargetedException.class, () -> service.joinGame(new JoinGameRequest(user.authToken(), "WHITE", 1)));
    }

    @Test
    @DisplayName("Join Full Game")
    void joinGameFull() throws HandlerTargetedException {
        UserDataResult user = service.register(new RegisterRequest("Maxwell", "Pr1sbrey", "spam@gmail.com"));
        CreateGameResult result = service.createGame(new CreateGameRequest(user.authToken(), "Game 1"));
        assertDoesNotThrow(() -> service.joinGame(new JoinGameRequest(user.authToken(), "WHITE", result.gameID())));
        assertDoesNotThrow(() -> service.joinGame(new JoinGameRequest(user.authToken(), "BLACK", result.gameID())));
        assertThrows(HandlerTargetedException.class, () -> service.joinGame(new JoinGameRequest(user.authToken(), "WHITE", 1)));
    }

    @Test
    @DisplayName("Clear Positive Test")
    void clearTest() throws HandlerTargetedException {
        UserDataResult user = service.register(new RegisterRequest("Maxwell", "Pr1sbrey", "spam@gmail.com"));
        service.register(new RegisterRequest("Tasha", "KN11", "spam@gmail.nope"));
        service.register(new RegisterRequest("OIJSDF", "Greenish", "m@g"));

        service.createGame(new CreateGameRequest(user.authToken(), "Game 1"));
        service.createGame(new CreateGameRequest(user.authToken(), "Game 2"));

        assertDoesNotThrow(service::clear);

        user = service.register(new RegisterRequest("Maxwell", "Pr1sbrey", "spam@gmail.com"));

        assertThrows(HandlerTargetedException.class, () -> service.login(new LoginRequest("Tasha", "KN11")));

        ListGamesResult result = service.listGames(new ListGamesRequest(user.authToken()));

        assertTrue(result.games().isEmpty());
    }
}