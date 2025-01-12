package dataaccess;

import model.AuthData;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class AuthDAO {

    private Set<AuthData> database = new HashSet<>();

    public AuthDAO() {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement statement = conn.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS users (
                        auth_token VARCHAR(255) NOT NULL,
                        username VARCHAR(50) NOT NULL UNIQUE
            );""")) {
                statement.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void createAuth(String authToken, String username) {
        try (Connection conn = DatabaseManager.getConnection()){
            try (PreparedStatement statement = conn.prepareStatement("SELECT auth_token, username FROM users " +
                    "WHERE username = ?;")) {
                statement.setString(1, username);
                ResultSet res = statement.executeQuery();
                if (res.next()) {
                    try (PreparedStatement updateStatement = conn.prepareStatement("UPDATE users SET auth_token =" +
                            " ? WHERE username = ?;")) {
                        updateStatement.setString(1, authToken);
                        updateStatement.setString(2, username);
                        updateStatement.executeUpdate();
                    }
                } else {
                    try (PreparedStatement createStatement = conn.prepareStatement("INSERT INTO users (auth_token, username) " +
                            "VALUES (?, ?);")) {
                        createStatement.setString(1, authToken);
                        createStatement.setString(2, username);
                        createStatement.executeUpdate();
                    }
                }
            }
        } catch (DataAccessException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()){
            try (PreparedStatement statement = conn.prepareStatement("SELECT auth_token, username FROM users " +
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

    public void deleteAuth(String authToken) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()){
            try (PreparedStatement statement = conn.prepareStatement("DELETE FROM users WHERE auth_token = ?;")) {
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

    public void clear() {
        try (Connection conn = DatabaseManager.getConnection()){
            try (PreparedStatement statement = conn.prepareStatement("TRUNCATE TABLE users;")) {
                statement.executeUpdate();
            }
        } catch (DataAccessException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
