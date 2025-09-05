package dataaccess;

import model.UserData;

import java.util.HashMap;

public class MemoryUserDAO implements UserDAO {
    HashMap<String, UserData> users;

    public MemoryUserDAO() {
        users = new HashMap<>();
    }

    public void createUser(String username, String password, String email) {
        UserData newUser = new UserData(username, password, email);
        users.put(username, newUser);
    }

    public UserData getUser(String username) throws DataAccessException {
        UserData user = users.get(username);
        if (user == null) {
            throw new DataAccessException("Error: No such user");
        }
        return user;
    }

    public void clearUsers() {
        users.clear();
    }
}
