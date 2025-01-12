package dataaccess;

import model.AuthData;

import java.util.HashSet;
import java.util.Set;

public class AuthDAO {

    private Set<AuthData> database = new HashSet<>();

    public void createAuth(String authToken, String username) {
        database.add(new AuthData(authToken, username));
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        for (AuthData auth : database) {
            if (auth.authToken().equals(authToken)) {
                return auth;
            }
        }
        throw new DataAccessException("getAuth Error: No AuthData with given authToken");
    }

    public void deleteAuth(String authToken) {
        database.removeIf(auth -> auth.authToken().equals(authToken));
    }

    public void clear() {
        database.clear();
    }
}
