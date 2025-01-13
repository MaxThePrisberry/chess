package dataaccess;

import model.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public static void setUp() {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement statement = conn.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS users (
                        email VARCHAR(255) NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        username VARCHAR(50) NOT NULL UNIQUE
            );""")) {
                statement.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createUser(String username, String password, String email) {
        try (Connection conn = DatabaseManager.getConnection()){
            try (PreparedStatement createStatement = conn.prepareStatement("INSERT INTO users (username, password, email) " +
                    "VALUES (?, ?, ?);")) {
                createStatement.setString(1, username);
                createStatement.setString(2, password);
                createStatement.setString(3, email);
                createStatement.executeUpdate();
            }
        } catch (DataAccessException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static UserData getUser(String username) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()){
            try (PreparedStatement statement = conn.prepareStatement("SELECT username, password, email FROM users" +
                    " WHERE username = ?;")) {
                statement.setString(1, username);
                ResultSet res = statement.executeQuery();
                if (res.next()) {
                    return new UserData(res.getString("username"), res.getString("password"),
                            res.getString("email"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        throw new DataAccessException("getUser Error: No user with given username");
    }

    public static void clear() {
        try (Connection conn = DatabaseManager.getConnection()){
            try (PreparedStatement statement = conn.prepareStatement("TRUNCATE TABLE users;")) {
                statement.executeUpdate();
            }
        } catch (DataAccessException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
