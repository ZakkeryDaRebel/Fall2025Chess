package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

public class GameDAOTests {

    private GameDAO gameDAO;
    private boolean isSQL;

    public GameDAOTests() {
        try {
            gameDAO = new SQLGameDAO();
            isSQL = true;
        } catch (Exception ex) {
            isSQL = false;
        }
    }

    @BeforeEach
    public void clearAll() throws Exception {
        gameDAO.clearGames();
        Assertions.assertTrue(isSQL);
    }

    @Test
    public void createGame() {
        Assertions.assertDoesNotThrow(() -> {
            gameDAO.createGame("Test Game");
        });
    }

    @Test
    public void createNullGame() {
        Assertions.assertThrows(Exception.class, () -> {
            gameDAO.createGame(null);
        });
    }

    @Test
    public void getGame() {
        createGame();
        Assertions.assertDoesNotThrow(() -> {
            GameData gameData = gameDAO.getGame(1);
            Assertions.assertTrue(compareGames(new GameData(1, null, null, "Test Game", new ChessGame()), gameData));
        });
    }

    @Test
    public void getNonExistingGame() {
        Assertions.assertThrows(Exception.class, () -> {
            gameDAO.getGame(5);
        });
    }

    @Test
    public void listGames() {
        createGame();
        Assertions.assertDoesNotThrow(() -> {
            Collection<GameData> gameList = gameDAO.listGames();
            Assertions.assertEquals(1, gameList.size());
            Assertions.assertTrue(compareGames(new GameData(1, null, null, "Test Game", new ChessGame()), gameList.stream().findFirst().get()));
        });
    }

    @Test
    public void listNoGames() {
        Assertions.assertDoesNotThrow(() -> {
            Collection<GameData> gameList = gameDAO.listGames();
            Assertions.assertEquals(0, gameList.size());
        });
    }

    @Test
    public void updateGame() {
        createGame();
        Assertions.assertDoesNotThrow(() -> {
            GameData expected = new GameData(1, "WHITE PLAYER", "BLACK PLAYER", "Test Game", new ChessGame());
            gameDAO.updateGame(expected);
            Assertions.assertTrue(compareGames(expected, gameDAO.getGame(1)));
        });
    }

    @Test
    public void updateInvalidGame() {
        createGame();
        Assertions.assertDoesNotThrow(() -> {
            gameDAO.updateGame(new GameData(-1, "WHITE NULL", "BLACK NULL", null, null));
            GameData gameData = gameDAO.getGame(1);
            Assertions.assertTrue(compareGames(new GameData(1, null, null, "Test Game", new ChessGame()), gameData));
        });
    }

    @Test
    public void clearGames() {
        createGame();
        Assertions.assertDoesNotThrow(() -> {
            gameDAO.clearGames();
        });
        Assertions.assertThrows(Exception.class, () -> {
            gameDAO.getGame(1);
        });
    }

    public boolean compareGames(GameData expected, GameData returned) {
        return (expected.gameID() == returned.gameID()) && (expected.gameName().equals(returned.gameName()))
                && compareString(expected.whiteUsername(), returned.whiteUsername())
                && compareString(expected.blackUsername(), returned.blackUsername());
    }

    public boolean compareString(String expected, String returned) {
        return (expected == null && returned == null) || (expected != null && expected.equals(returned));
    }
}
