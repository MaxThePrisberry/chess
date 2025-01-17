package client;

import dataaccess.*;
import org.junit.jupiter.api.*;
import server.Server;
import ui.LoginUI;
import ui.PreLoginUI;
import ui.ServerFacade;
import ui.UIException;
import ui.model.UIData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static ui.Variables.serverLocation;
import static ui.Variables.authToken;


public class ServerFacadeTests {

    private static Server server;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        serverLocation = "http://localhost:" + port;
    }

    private void clearDatabase() {
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

    @BeforeEach
    void setUp() {
        authToken = null;
        UserDAO.setUp();
        GameDAO.setUp();
        AuthDAO.setUp();

        clearDatabase();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    @Test
    public void register() {
        String oldAuth = authToken;
        UIData data = assertDoesNotThrow(() -> PreLoginUI.register("Potato", "Farmer", "on@farm"));
        assertNotEquals(oldAuth, authToken);
        assertNotEquals(null, authToken);
        assertTrue(authToken.length() > 20);
        assertEquals(ServerFacade.UIType.LOGIN, data.uiType());
    }

    @Test
    public void registerNegative() {
        assertThrows(UIException.class, () -> PreLoginUI.register("Potato", "", "on@farm"));
        assertNull(authToken);
    }

    @Test
    public void login() {
        String oldAuth = authToken;
        PreLoginUI.register("Potato", "Farmer", "on@farm");
        LoginUI.logout();
        UIData data = assertDoesNotThrow(() -> PreLoginUI.login("Potato", "Farmer"));
        assertNotEquals(oldAuth, authToken);
        assertNotEquals(null, authToken);
        assertTrue(authToken.length() > 20);
        assertEquals(ServerFacade.UIType.LOGIN, data.uiType());
    }

    @Test
    public void loginNegative() {
        String oldAuth = authToken;
        PreLoginUI.register("Potato", "Farmer", "on@farm");
        LoginUI.logout();
        assertThrows(UIException.class, () -> PreLoginUI.login("Potato", "Eater"));
        assertEquals(oldAuth, authToken);
        assertNull(authToken);
    }

    @Test
    public void preloginHelp() {
        UIData data = assertDoesNotThrow(PreLoginUI::help);
        assertEquals(ServerFacade.UIType.PRELOGIN, data.uiType());
    }

    @Test
    public void loginHelp() {
        UIData data = assertDoesNotThrow(LoginUI::help);
        assertEquals(ServerFacade.UIType.LOGIN, data.uiType());
    }

    @Test
    public void logout() {
        PreLoginUI.register("Potato", "Farmer", "on@farm");

        LoginUI.logout();
        assertNull(authToken);
    }

    @Test
    public void logoutNegative() {
        assertThrows(UIException.class, LoginUI::logout);
        assertNull(authToken);
    }

    @Test
    public void create() {
        PreLoginUI.register("Potato", "Farmer", "on@farm");
        UIData data = assertDoesNotThrow(() -> LoginUI.create("testGameName"));
        assertEquals(ServerFacade.UIType.LOGIN, data.uiType());
        assertTrue(data.output().matches("^New game created with ID \\d+"));
    }

    @Test
    public void createNegative() {
        PreLoginUI.register("Potato", "Farmer", "on@farm");
        assertThrows(UIException.class, () -> LoginUI.create(""));
    }

    @Test
    public void join() {
        PreLoginUI.register("Potato", "Farmer", "on@farm");
        LoginUI.create("testGameName");
        UIData data = assertDoesNotThrow(() -> LoginUI.join("1", "WHITE"));
        assertEquals(ServerFacade.UIType.GAMEPLAY, data.uiType());
        assertTrue(data.output().matches("^You have joined game \\d+ as player (?:WHITE|BLACK)\\.[\\s\\S]*"));
    }

    @Test
    public void joinNegative() {
        PreLoginUI.register("Potato", "Farmer", "on@farm");
        assertThrows(UIException.class, () -> LoginUI.join("1", "WHITE"));
    }

    @Test
    public void list() {
        PreLoginUI.register("Potato", "Farmer", "on@farm");
        LoginUI.create("testGameName1");
        LoginUI.create("testGameName2");
        LoginUI.create("testGameName3");
        try (Connection conn = DatabaseManager.getConnection()){
            try (PreparedStatement statement = conn.prepareStatement("DELETE FROM games WHERE game_id = 1;")) {
                statement.executeUpdate();
            } catch (SQLException ignored) {}
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        } catch (SQLException ignored) {}
        LoginUI.create("testGameName4");

        UIData data = LoginUI.list();
        assertEquals(ServerFacade.UIType.LOGIN, data.uiType());
        assertEquals("""
                Games:
                1 | testGameName2
                2 | testGameName3
                3 | testGameName4
                """, data.output());
    }

    @Test
    public void listNegative() {
        assertThrows(UIException.class, LoginUI::list);
    }

    @Test
    public void observe() {
        PreLoginUI.register("Potato", "Farmer", "on@farm");
        LoginUI.create("testGameName1");
        UIData data = assertDoesNotThrow(() -> LoginUI.observe("1"));
        assertEquals(ServerFacade.UIType.GAMEPLAY, data.uiType());
    }

    @Test
    public void observeNegative() {
        PreLoginUI.register("Potato", "Farmer", "on@farm");
        assertThrows(UIException.class, () -> LoginUI.observe("1"));
    }
}
