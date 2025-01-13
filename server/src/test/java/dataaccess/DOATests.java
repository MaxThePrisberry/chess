package dataaccess;

import chess.ChessGame;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
}
