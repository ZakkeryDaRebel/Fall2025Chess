package service;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import requests.*;
import results.*;

import java.util.Collection;

public class GameService {

    private AuthDAO authDAO;
    private GameDAO gameDAO;

    public GameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public CreateGameResult createGame(CreateGameRequest createGameRequest) throws ResponseException {
        if (createGameRequest.gameName() == null) {
            throw new ResponseException("Error: Bad Request", 400);
        }

        validateAuth(createGameRequest.authToken());

        try {
            int gameID = gameDAO.createGame(createGameRequest.gameName());
            return new CreateGameResult(gameID);
        } catch (Exception ex) {
            throw new ResponseException("Error: " + ex.getMessage(), 500);
        }
    }

    public void joinGame(JoinGameRequest joinGameRequest) throws ResponseException {
        String username = validateAuth(joinGameRequest.authToken());

        try {
            GameData gameData = gameDAO.getGame(joinGameRequest.gameID());
            if (gameData == null) {
                throw new DataAccessException("Invalid gameID");
            }
            String whiteName = gameData.whiteUsername();
            String blackName = gameData.blackUsername();
            if (joinGameRequest.playerColor() == ChessGame.TeamColor.WHITE) {
                if (whiteName != null && !whiteName.equals(username)) {
                    throw new ResponseException("Error: Already taken", 403);
                }
                whiteName = username;
            } else if (joinGameRequest.playerColor() == ChessGame.TeamColor.BLACK){
                if (blackName != null && !blackName.equals(username)) {
                    throw new ResponseException("Error: Already taken", 403);
                }
                blackName = username;
            }
            else {
                throw new ResponseException("Error: Bad request", 400);
            }
            gameDAO.updateGame(new GameData(gameData.gameID(),whiteName,blackName,gameData.gameName(),gameData.game()));
        } catch (DataAccessException ex) {
            if (ex.getMessage().contains("cannot connect")) {
                throw new ResponseException("Error: " + ex.getMessage(), 500);
            }
            throw new ResponseException("Error: Bad request", 400);
        } catch (ResponseException rex) {
            throw rex;
        } catch (Exception ex) {
            throw new ResponseException("Error: " + ex.getMessage(), 500);
        }
    }

    public ListGamesResult listGames(ListGamesRequest listGamesRequest) throws ResponseException {
        validateAuth(listGamesRequest.authToken());

        try {
            Collection<GameData> gameList = gameDAO.listGames();
            return new ListGamesResult(gameList);
        } catch (Exception ex) {
            throw new ResponseException("Error: " + ex.getMessage(), 500);
        }
    }

    public String validateAuth(String authToken) throws ResponseException {
        try {
            AuthData auth = authDAO.getAuth(authToken);
            return auth.username();
        } catch (DataAccessException ex) {
            if (ex.getMessage().contains("Unauthorized")) {
                throw new ResponseException("Error: Unauthorized", 401);
            }
            throw new ResponseException("Error: " + ex.getMessage(), 500);
        }
    }
}
