package dataaccess;

import exception.ResponseException;
import model.UserData;

public interface UserDAO {
    void createUser(String username, String password, String email) throws ResponseException;
    UserData getUser(String username) throws DataAccessException, ResponseException;
    void clearUsers() throws ResponseException;
}
