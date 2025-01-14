package client;

import dataaccess.*;
import org.junit.jupiter.api.*;
import server.Server;
import ui.LoginUI;
import ui.PreLoginUI;
import ui.ServerFacade;
import ui.model.UIData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static ui.Variables.SERVER_LOCATION;
import static ui.Variables.authToken;


public class ServerFacadeTests {

    private static Server server;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        SERVER_LOCATION = "http://localhost:" + port;
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
        assertTrue(authToken.length() > 20);
        assertEquals(ServerFacade.UIType.LOGIN, data.uiType());
    }

    @Test
    public void login() {
        String oldAuth = authToken;
        PreLoginUI.register("Potato", "Farmer", "on@farm");
        LoginUI.logout();
        UIData data = assertDoesNotThrow(() -> PreLoginUI.login("Potato", "Farmer"));
        assertNotEquals(oldAuth, authToken);
        assertTrue(authToken.length() > 20);
        assertEquals(ServerFacade.UIType.LOGIN, data.uiType());
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
}
