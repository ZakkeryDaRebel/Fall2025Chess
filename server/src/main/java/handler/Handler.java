package handler;

import com.google.gson.Gson;
import exception.ResponseException;
import io.javalin.http.Context;
import requests.*;
import results.CreateGameResult;
import results.ListGamesResult;
import results.LoginResult;
import results.RegisterResult;
import service.*;

import java.util.Map;

public class Handler {

    private ClearService clearService;
    private GameService gameService;
    private UserService userService;

    public Handler(ClearService clearService, GameService gameService, UserService userService) {
        this.clearService = clearService;
        this.gameService = gameService;
        this.userService = userService;
    }

    public void exceptionHandler(ResponseException ex, Context ctx) {
        ctx.status(ex.getStatus());
        ctx.json(new Gson().toJson(Map.of("message", ex.getMessage())));
    }

    public void clearHandler(Context ctx) throws ResponseException {
        clearService.clear();
        successHandler(ctx, "");
    }

    public void createGameHandler(Context ctx) throws ResponseException {
        String authToken = ctx.header("Authorization");
        CreateGameRequest createGameRequest = new Gson().fromJson(ctx.body(), CreateGameRequest.class);
        createGameRequest = new CreateGameRequest(authToken, createGameRequest.gameName());

        CreateGameResult createGameResult = gameService.createGame(createGameRequest);
        successHandler(ctx, new Gson().toJson(createGameResult));
    }

    public void joinGameHandler(Context ctx) throws ResponseException {
        String authToken = ctx.header("Authorization");
        JoinGameRequest joinGameRequest = new Gson().fromJson(ctx.body(), JoinGameRequest.class);
        joinGameRequest = new JoinGameRequest(authToken, joinGameRequest.playerColor(), joinGameRequest.gameID());

        gameService.joinGame(joinGameRequest);
        successHandler(ctx, "");
    }

    public void listGamesHandler(Context ctx) throws ResponseException {
        String authToken = ctx.header("Authorization");
        ListGamesRequest listGamesRequest = new ListGamesRequest(authToken);

        ListGamesResult listGamesResult = gameService.listGames(listGamesRequest);
        successHandler(ctx, new Gson().toJson(listGamesResult));
    }

    public void loginHandler(Context ctx) throws ResponseException {
        LoginRequest loginRequest = new Gson().fromJson(ctx.body(), LoginRequest.class);

        LoginResult loginResult = userService.login(loginRequest);
        successHandler(ctx, new Gson().toJson(loginResult));
    }

    public void logoutHandler(Context ctx) throws ResponseException {
        String authToken = ctx.header("Authorization");
        LogoutRequest logoutRequest = new LogoutRequest(authToken);

        userService.logout(logoutRequest);
        successHandler(ctx, "");
    }

    public void registerHandler(Context ctx) throws ResponseException {
        RegisterRequest registerRequest = new Gson().fromJson(ctx.body(), RegisterRequest.class);

        RegisterResult registerResult = userService.register(registerRequest);
        successHandler(ctx, new Gson().toJson(registerResult));
    }

    public void successHandler(Context ctx, String json) {
        ctx.status(200);
        ctx.json(json);
    }
}
