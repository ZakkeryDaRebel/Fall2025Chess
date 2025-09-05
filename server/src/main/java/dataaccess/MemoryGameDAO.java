package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class MemoryGameDAO implements GameDAO {

    HashMap<Integer, GameData> games;
    int nextGameID;

    public MemoryGameDAO() {
        games = new HashMap<>();
        nextGameID = 101;
    }

    public int createGame(String gameName) {
        GameData newGame = new GameData(nextGameID, null, null, gameName, new ChessGame());
        games.put(nextGameID, newGame);
        return nextGameID++;
    }

    public GameData getGame(int gameID) throws DataAccessException {
        GameData game = games.get(gameID);
        if (game == null) {
            throw new DataAccessException("Error: Invalid gameID");
        }
        return game;
    }

    public Collection<GameData> listGames() {
        Collection<GameData> gameList = new ArrayList<>();
        for (GameData gameData : games.values()) {
            gameList.add(new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), null));
        }
        return gameList;
    }

    public void updateGame(GameData gameData) throws DataAccessException {
        GameData oldGame = games.get(gameData.gameID());
        if(oldGame == null) {
            throw new DataAccessException("Error: Game does not exist");
        }
        games.put(gameData.gameID(), gameData);
    }

    public void clearGames() {
        games.clear();
    }
}
