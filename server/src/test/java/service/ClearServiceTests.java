package service;

import dataaccess.*;
import exception.ResponseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import requests.CreateGameRequest;
import requests.RegisterRequest;
import results.CreateGameResult;
import results.RegisterResult;


public class ClearServiceTests {

    AuthDAO authDAO = new MemoryAuthDAO();
    UserDAO userDAO = new MemoryUserDAO();
    GameDAO gameDAO = new MemoryGameDAO();
    UserService userService = new UserService(authDAO, userDAO);
    GameService gameService = new GameService(authDAO, gameDAO);
    ClearService clearService = new ClearService(authDAO, gameDAO, userDAO);


    @Test
    public void clearUsers() throws ResponseException, DataAccessException {
        String username = "user1";
        userService.register(new RegisterRequest(username, "password", "email"));
        Assertions.assertNotNull(userDAO.getUser(username));
        clearService.clear();
        Assertions.assertThrows(DataAccessException.class, () -> {
            userDAO.getUser(username);
        });
    }

    @Test
    public void clearAuths() throws ResponseException, DataAccessException {
        String username = "user2";
        RegisterResult result = userService.register(new RegisterRequest(username, "password", "email"));
        Assertions.assertNotNull(authDAO.getAuth(result.authToken()));
        clearService.clear();
        Assertions.assertThrows(DataAccessException.class, () -> {
            authDAO.getAuth(result.authToken());
        });
    }

    @Test
    public void clearGames() throws ResponseException, DataAccessException {
        String username = "user3";
        String authToken = userService.register(new RegisterRequest(username, "pw", "email")).authToken();
        CreateGameResult result = gameService.createGame(new CreateGameRequest(authToken, "newGame"));
        Assertions.assertNotNull(gameDAO.getGame(result.gameID()));
        clearService.clear();
        Assertions.assertThrows(DataAccessException.class, () -> {
            gameDAO.getGame(result.gameID());
        });
    }
}
