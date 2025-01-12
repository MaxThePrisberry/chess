package dataaccess;

import model.UserData;

import java.util.HashSet;
import java.util.Set;

public class UserDAO {

    private Set<UserData> database = new HashSet<>();

    public void createUser(String username, String password, String email) {
        database.add(new UserData(username, password, email));
    }

    public UserData getUser(String username) throws DataAccessException {
        for (UserData user : database) {
            if (user.username().equals(username)) {
                return user;
            }
        }
        throw new DataAccessException("getUser Error: No user with given username");
    }

    public void deleteUser(String username) {
        database.removeIf(user -> user.username().equals(username));
    }

    public void clear() {
        database.clear();
    }
}
