package dataaccess;

import model.AuthData;

import java.util.HashMap;

public class MemoryAuthDAO implements AuthDAO {

    HashMap<String, AuthData> auths;

    public MemoryAuthDAO() {
        auths = new HashMap<>();
    }

    public void createAuth(String username, String authToken) {
        AuthData newAuth = new AuthData(authToken, username);
        auths.put(authToken, newAuth);
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        AuthData auth = auths.get(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: Unauthorized");
        }
        return auth;
    }

    public void deleteAuth(String authToken) {
        auths.remove(authToken);
    }

    public void clearAuths() {
        auths.clear();
    }
}
