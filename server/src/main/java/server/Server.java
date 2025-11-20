package server;

import dataaccess.*;
import dataaccess.sql.SQLAuthDAO;
import dataaccess.sql.SQLGameDAO;
import dataaccess.sql.SQLUserDAO;
import exception.ResponseException;
import handler.Handler;
import websocket.ConnectionManager;
import websocket.WebSocketHandler;
import io.javalin.Javalin;
import service.*;

public class Server {

    private final Javalin javalin;

    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private UserDAO userDAO;

    private ClearService clearService;
    private GameService gameService;
    private UserService userService;

    private Handler handler;
    private ConnectionManager connectionManager;
    private WebSocketHandler websocketHandler;

    public Server() {
        try {
            //First try to create SQL Based DAOs
            authDAO = new SQLAuthDAO();
            gameDAO = new SQLGameDAO();
            userDAO = new SQLUserDAO();
            System.out.println("Server is using SQL databases");
        } catch (Exception ex) {
            //Memory Based DAOs if MySQL ones failed
            System.out.println("Failed to create SQL DAO: " + ex.getMessage());
            authDAO = new MemoryAuthDAO();
            gameDAO = new MemoryGameDAO();
            userDAO = new MemoryUserDAO();
            System.out.println("Server is using Memory databases");
        }

        //Create Services
        clearService = new ClearService(authDAO, gameDAO, userDAO);
        gameService = new GameService(authDAO, gameDAO);
        userService = new UserService(authDAO, userDAO);

        handler = new Handler(clearService, gameService, userService);
        connectionManager = new ConnectionManager();
        websocketHandler = new WebSocketHandler(authDAO, gameDAO, connectionManager);

        javalin = Javalin.create(config -> config.staticFiles.add("web"))
                // Register your endpoints and exception handlers here.
                .ws("/ws", ws -> {
                    ws.onConnect(websocketHandler);
                    ws.onClose(websocketHandler);
                    ws.onMessage(websocketHandler);
                })
                .delete("/db", context -> {handler.clearHandler(context);})
                .delete("/session", context -> {handler.logoutHandler(context);})
                .get("/game", context -> {handler.listGamesHandler(context);})
                .post("/user", context -> {handler.registerHandler(context);})
                .post("/session", context -> {handler.loginHandler(context);})
                .post("/game", context -> {handler.createGameHandler(context);})
                .put("/game", context -> {handler.joinGameHandler(context);})
                .exception(ResponseException.class, (e, context) -> {handler.exceptionHandler(e, context);})
        ;
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
