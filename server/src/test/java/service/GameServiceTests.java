package service;

import chess.ChessGame;
import dataaccess.*;
import exception.ResponseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import requests.CreateGameRequest;
import requests.JoinGameRequest;
import requests.ListGamesRequest;
import requests.RegisterRequest;
import results.CreateGameResult;
import results.ListGamesResult;
import results.RegisterResult;

public class GameServiceTests {

    AuthDAO authDAO = new MemoryAuthDAO();
    UserDAO userDAO = new MemoryUserDAO();
    GameDAO gameDAO = new MemoryGameDAO();
    UserService userService = new UserService(authDAO, userDAO);
    GameService gameService = new GameService(authDAO, gameDAO);

    @Test
    public void goodCreateGame() throws ResponseException, DataAccessException {
        String authToken = registerUser("user1");
        int gameID = createGameID(authToken, "game1");
        gameDAO.getGame(gameID);
    }

    @Test
    public void noNameCreateGame() {
        Assertions.assertThrows(ResponseException.class, () -> {
            String authToken = registerUser("user1");
            gameService.createGame(new CreateGameRequest(authToken, null));
        });
    }

    @Test
    public void badAuthTokenCreateGame() {
        Assertions.assertThrows(ResponseException.class, () -> {
            gameService.createGame(new CreateGameRequest(null, "game1"));
        });
    }


    @Test
    public void goodJoinGameWhite() throws ResponseException, DataAccessException {
        String username = "white_user";
        String authToken = registerUser(username);
        int gameID = createGameID(authToken, "game1");
        gameService.joinGame(new JoinGameRequest(authToken, ChessGame.TeamColor.WHITE, gameID));
        Assertions.assertEquals(username, gameDAO.getGame(gameID).whiteUsername());
    }

    @Test
    public void goodJoinGameBlack() throws ResponseException, DataAccessException {
        String username = "black_user";
        String authToken = registerUser(username);
        int gameID = createGameID(authToken, "game2");
        gameService.joinGame(new JoinGameRequest(authToken, ChessGame.TeamColor.BLACK, gameID));
        Assertions.assertEquals(username, gameDAO.getGame(gameID).blackUsername());
    }

    @Test
    public void noColorJoinGame() {
        Assertions.assertThrows(ResponseException.class, () -> {
            String authToken = registerUser("no_color_user");
            int gameID = createGameID(authToken, "game3");
            gameService.joinGame(new JoinGameRequest(authToken, null, gameID));
        });
    }

    @Test
    public void badAuthTokenJoinGame() {
        Assertions.assertThrows(ResponseException.class, () -> {
            String authToken = registerUser("bad_auth_user");
            int gameID = createGameID(authToken, "game4");
            gameService.joinGame(new JoinGameRequest(null, ChessGame.TeamColor.WHITE, gameID));
        });
    }

    @Test
    public void badGameIDJoinGame() {
        Assertions.assertThrows(ResponseException.class, () -> {
            String authToken = registerUser("bad_game_user");
            gameService.joinGame(new JoinGameRequest(authToken, ChessGame.TeamColor.WHITE, -5));
        });
    }

    @Test
    public void whiteTakenJoinGame() {
        Assertions.assertThrows(ResponseException.class, () -> {
            String authToken1 = registerUser("white_user");
            int gameID = createGameID(authToken1, "game5");
            gameService.joinGame(new JoinGameRequest(authToken1, ChessGame.TeamColor.WHITE, gameID));
            String authToken2 = registerUser("second_user");
            gameService.joinGame(new JoinGameRequest(authToken2, ChessGame.TeamColor.WHITE, gameID));
        });
    }

    @Test
    public void blackTakenJoinGame() {
        Assertions.assertThrows(ResponseException.class, () -> {
            String authToken1 = registerUser("black_user");
            int gameID = createGameID(authToken1, "game6");
            gameService.joinGame(new JoinGameRequest(authToken1, ChessGame.TeamColor.BLACK, gameID));
            String authToken2 = registerUser("second_user");
            gameService.joinGame(new JoinGameRequest(authToken2, ChessGame.TeamColor.BLACK, gameID));
        });
    }


    @Test
    public void list0Games() throws ResponseException {
        String authToken = registerUser("list_user");
        ListGamesResult result = gameService.listGames(new ListGamesRequest(authToken));
        Assertions.assertEquals(0, result.games().size());
    }

    //Successful List games with 1 game
    @Test
    public void list1Game() throws ResponseException {
        String authToken = registerUser("list_user");
        createGameID(authToken, "game7");
        ListGamesResult result = gameService.listGames(new ListGamesRequest(authToken));
        Assertions.assertEquals(1, result.games().size());
    }

    //List games with invalid authToken
    @Test
    public void badAuthTokenListGames() {
        Assertions.assertThrows(ResponseException.class, () -> {
            gameService.listGames(new ListGamesRequest(null));
        });
    }

    public String registerUser(String username) throws ResponseException {
        RegisterResult result = userService.register(new RegisterRequest(username, "password", "email"));
        return result.authToken();
    }

    public int createGameID(String authToken, String gameName) throws ResponseException {
        CreateGameResult result = gameService.createGame(new CreateGameRequest(authToken, "game1"));
        return result.gameID();
    }
}
