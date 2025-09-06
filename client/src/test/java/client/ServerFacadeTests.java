package client;

import chess.ChessGame;
import connection.ServerFacade;
import exception.ResponseException;
import org.junit.jupiter.api.*;
import requests.*;
import results.*;
import server.Server;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        serverFacade = new ServerFacade("http://localhost:8888", null);
        System.out.println("Started test HTTP server on " + port);
    }

    @BeforeEach
    public void clearDB() {
        Assertions.assertDoesNotThrow(() -> {
            serverFacade.clear();
        });
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    @Test
    public void goodRegister() {
        Assertions.assertDoesNotThrow(() -> {
            RegisterResult registerResult = serverFacade.register(new RegisterRequest("username", "password", "email"));
            Assertions.assertNotNull(registerResult.authToken());
        });
    }

    @Test
    public void nullRegister() {
        Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.register(new RegisterRequest(null, null, null));
        });
    }

    @Test
    public void takenRegister() {
        goodRegister();
        Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.register(new RegisterRequest("username", "password", "email"));
        });
    }

    @Test
    public void goodLogin() {
        goodRegister();
        Assertions.assertDoesNotThrow(() -> {
            LoginResult loginResult = serverFacade.login(new LoginRequest("username", "password"));
            Assertions.assertNotNull(loginResult.authToken());
        });
    }

    @Test
    public void noUserLogin() {
        Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.login(new LoginRequest("username", "password"));
        });
    }

    @Test
    public void wrongPasswordLogin() {
        goodRegister();
        Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.login(new LoginRequest("username", "p"));
        });
    }

    public String getAuthToken(String username) {
        return Assertions.assertDoesNotThrow(() -> {
            RegisterResult registerResult = serverFacade.register(new RegisterRequest(username, "Auth", "Auth"));
            return registerResult.authToken();
        });
    }

    @Test
    public void goodLogout() {
        String authToken = getAuthToken("goodLogout");
        Assertions.assertDoesNotThrow(() -> {
            serverFacade.logout(new LogoutRequest(authToken));
        });
    }

    @Test
    public void badAuthLogout() {
        Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.logout(new LogoutRequest("1234"));
        });
    }

    @Test
    public void goodCreate() {
        String authToken = getAuthToken("goodCreate");
        Assertions.assertDoesNotThrow(() -> {
            CreateGameResult createResult = serverFacade.createGame(new CreateGameRequest(authToken, "New Game"));
            Assertions.assertNotNull(createResult);
            Assertions.assertTrue(createResult.gameID() > 0);
        });
    }

    @Test
    public void goodCreateMultiple() {
        String authToken = getAuthToken("goodCreateMultiple");
        Assertions.assertDoesNotThrow(() -> {
            for (int i = 0; i < 5; i++) {
                CreateGameResult createResult = serverFacade.createGame(new CreateGameRequest(authToken, "Game"+i));
                Assertions.assertNotNull(createResult);
                Assertions.assertTrue(createResult.gameID() > 0);
            }
        });
    }

    @Test
    public void noAuthCreate() {
        Assertions.assertThrows(ResponseException.class, () -> {
            CreateGameResult createResult = serverFacade.createGame(new CreateGameRequest("1234", "New Game"));
            Assertions.assertNotNull(createResult);
            Assertions.assertTrue(createResult.gameID() > 0);
        });
    }

    @Test
    public void noNameCreate() {
        String authToken = getAuthToken("noNameCreate");
        Assertions.assertThrows(ResponseException.class, () -> {
            CreateGameResult createResult = serverFacade.createGame(new CreateGameRequest(authToken, null));
            Assertions.assertNotNull(createResult);
            Assertions.assertTrue(createResult.gameID() > 0);
        });
    }

    @Test
    public void listNoGames() {
        String authToken = getAuthToken("listNoGames");
        Assertions.assertDoesNotThrow(() -> {
            ListGamesResult listResult = serverFacade.listGames(new ListGamesRequest(authToken));
            Assertions.assertNotNull(listResult);
        });
    }

    @Test
    public void listOneGame() {
        goodCreate();
        String authToken = getAuthToken("listOneGame");
        Assertions.assertDoesNotThrow(() -> {
            ListGamesResult listResult = serverFacade.listGames(new ListGamesRequest(authToken));
            Assertions.assertEquals(1, listResult.games().size());
        });
    }

    @Test
    public void listMultipleGames() {
        goodCreateMultiple();
        String authToken = getAuthToken("listMultipleGames");
        Assertions.assertDoesNotThrow(() -> {
            ListGamesResult listResult = serverFacade.listGames(new ListGamesRequest(authToken));
            Assertions.assertEquals(5, listResult.games().size());
        });
    }

    @Test
    public void noAuthList() {
        Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.listGames(new ListGamesRequest("1234"));
        });
    }

    @Test
    public void goodJoin() {
        String authToken = getAuthToken("GoodJoing");
        goodCreate();
        Assertions.assertDoesNotThrow(() -> {
            serverFacade.joinGame(new JoinGameRequest(authToken, ChessGame.TeamColor.WHITE, 1));
        });
    }

    @Test
    public void noGameJoin() {
        String authToken = getAuthToken("noGameJoin");
        Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.joinGame(new JoinGameRequest(authToken, ChessGame.TeamColor.WHITE, 5));
        });
    }

    @Test
    public void alreadyTakenJoin() {
        String authToken = getAuthToken("alreadyTakenJoin");
        goodJoin();
        Assertions.assertThrows(ResponseException.class, () -> {
            serverFacade.joinGame(new JoinGameRequest(authToken, ChessGame.TeamColor.WHITE, 1));
        });
    }

    @Test
    public void goodClear() {
        goodRegister();
        Assertions.assertDoesNotThrow(() -> {
            serverFacade.clear();
        });
        goodRegister();
    }
}
