package client;

import dataaccess.*;
import org.junit.jupiter.api.*;
import server.Server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static ui.Variables.SERVER_LOCATION;


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

}
