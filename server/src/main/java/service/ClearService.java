package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import exception.ResponseException;

public class ClearService {

    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private UserDAO userDAO;

    public ClearService(AuthDAO authDAO, GameDAO gameDAO, UserDAO userDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
        this.userDAO = userDAO;
    }

    public void clear() throws ResponseException {
        try {
            authDAO.clearAuths();
            gameDAO.clearGames();
            userDAO.clearUsers();
        } catch (Exception ex) {
            throw new ResponseException("Error: Failed to clear database ("+ex.getMessage()+")", 500);
        }

    }
}
