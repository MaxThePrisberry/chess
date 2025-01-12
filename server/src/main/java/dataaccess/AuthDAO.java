package dataaccess;

import model.AuthData;

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
                statement.executeQuery();
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void createAuth(String authToken, String username) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()){
            try (PreparedStatement statement = conn.prepareStatement("INSERT INTO users (auth_token, username) " +
                    "VALUES ('?', '?');")) {
                statement.setString(1, authToken);
                statement.setString(2, username);
                statement.executeQuery();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        for (AuthData auth : database) {
            if (auth.authToken().equals(authToken)) {
                return auth;
            }
        }
        throw new DataAccessException("getAuth Error: No AuthData with given authToken");
    }

    public void deleteAuth(String authToken) throws DataAccessException {
        if (database.stream().noneMatch(auth -> auth.authToken().equals(authToken))) {
            throw new DataAccessException("deleteAuth Error: No AuthData with given authToken");
        }
        database.removeIf(auth -> auth.authToken().equals(authToken));
    }

    public void clear() {
        database.clear();
    }
}
