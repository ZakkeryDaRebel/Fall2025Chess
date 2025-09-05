package dataaccess;

import exception.ResponseException;
import model.AuthData;

public interface AuthDAO {
    void createAuth(String username, String authToken) throws ResponseException;
    AuthData getAuth(String authToken) throws DataAccessException, ResponseException;
    void deleteAuth(String authToken) throws ResponseException;
    void clearAuths() throws ResponseException;
}
