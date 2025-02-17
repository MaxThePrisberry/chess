package dataaccess;

import model.AuthData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthDAO {

    public static void setUp() {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement statement = conn.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS auths (
                        auth_token VARCHAR(255) NOT NULL,
                        username VARCHAR(50) NOT NULL
            );""")) {
                statement.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createAuth(String authToken, String username) {
        try (Connection conn = DatabaseManager.getConnection()){
            try (PreparedStatement createStatement = conn.prepareStatement("INSERT INTO auths (auth_token, username) " +
                    "VALUES (?, ?);")) {
                createStatement.setString(1, authToken);
                createStatement.setString(2, username);
                createStatement.executeUpdate();
            }
        } catch (DataAccessException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static AuthData getAuth(String authToken) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()){
            try (PreparedStatement statement = conn.prepareStatement("SELECT auth_token, username FROM auths " +
                    "WHERE auth_token = ?;")) {
                statement.setString(1, authToken);
                ResultSet res = statement.executeQuery();
                if (res.next()) {
                    return new AuthData(res.getString("auth_token"), res.getString("username"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        throw new DataAccessException("getAuth Error: No AuthData with given authToken");
    }

    public static void deleteAuth(String authToken) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()){
            try (PreparedStatement statement = conn.prepareStatement("DELETE FROM auths WHERE auth_token = ?;")) {
                statement.setString(1, authToken);
                int res = statement.executeUpdate();
                if (res == 0) {
                    throw new DataAccessException("deleteAuth Error: No AuthData with given authToken");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void clear() {
        try (Connection conn = DatabaseManager.getConnection()){
            try (PreparedStatement statement = conn.prepareStatement("TRUNCATE TABLE auths;")) {
                statement.executeUpdate();
            }
        } catch (DataAccessException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
