package dataaccess;

import exception.ResponseException;
import model.GameData;

import java.util.Collection;

public interface GameDAO {
    int createGame(String gameName) throws ResponseException;
    GameData getGame(int gameID) throws DataAccessException, ResponseException;
    Collection<GameData> listGames() throws ResponseException;
    void updateGame(GameData gameData) throws DataAccessException, ResponseException;
    void clearGames() throws ResponseException;
}
