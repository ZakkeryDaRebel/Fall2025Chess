package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import exception.ResponseException;
import model.GameData;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

public class SQLGameDAO implements GameDAO {

    public SQLGameDAO() throws DataAccessException, SQLException {
        configureDatabase();
    }

    public int createGame(String gameName) throws ResponseException {
        String statement = "INSERT INTO game (gameName, game) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, gameName);
                ChessGame newGame = new ChessGame();
                var gson = new Gson();
                String game = gson.toJson(newGame);
                ps.setString(2, game);
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
                throw new ResponseException("Couldn't return the gameID", 500);
            }
        } catch (DataAccessException ex) {
            throw new ResponseException("Cannot connect to the Database", 500);
        } catch (SQLException ex) {
            throw new ResponseException("SQL Exception (" + ex.getMessage() + ")", 500);
        } catch (Exception ex) {
            throw new ResponseException(ex.getMessage(), 500);
        }
    }

    public GameData getGame(int gameID) throws DataAccessException, ResponseException {
        String statement = "SELECT whiteUsername, blackUsername, gameName, game FROM game WHERE gameID=?";
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String whiteUsername = rs.getString("whiteUsername");
                    String blackUsername = rs.getString("blackUsername");
                    String gameName = rs.getString("gameName");
                    ChessGame game = new Gson().fromJson(rs.getString("game"), ChessGame.class);
                    return new GameData(gameID, whiteUsername, blackUsername, gameName, game);
                }
                throw new DataAccessException("Error: Invalid gameID");
            }
        } catch (SQLException ex) {
            throw new ResponseException("SQL Exception (" + ex.getMessage() + ")", 500);
        }
    }

    public Collection<GameData> listGames() throws ResponseException {
        ArrayList<GameData> gameList = new ArrayList<>();
        String statement = "SELECT gameID, whiteUsername, blackUsername, gameName FROM game";
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                try (ResultSet allGames = ps.executeQuery()) {
                    while (allGames.next()) {
                        int gameID = allGames.getInt("gameID");
                        String whiteUsername = allGames.getString("whiteUsername");
                        String blackUsername = allGames.getString("blackUsername");
                        String gameName = allGames.getString("gameName");
                        gameList.add(new GameData(gameID, whiteUsername, blackUsername, gameName, null));
                    }
                    return gameList;
                }
            }
        } catch (DataAccessException ex) {
            throw new ResponseException("Cannot connect to the Database", 500);
        } catch (SQLException ex) {
            throw new ResponseException("SQL Exception (" + ex.getMessage() + ")", 500);
        }
    }

    public void updateGame(GameData gameData) throws DataAccessException, ResponseException {
        String statement = "UPDATE game SET whiteUsername=?, blackUsername=?, game=? WHERE gameID=?";
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, gameData.whiteUsername());
                ps.setString(2, gameData.blackUsername());
                ps.setString(3, new Gson().toJson(gameData.game()));
                ps.setInt(4, gameData.gameID());
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new ResponseException("SQL Exception (" + ex.getMessage() + ")", 500);
        }
    }

    public void clearGames() throws ResponseException{
        String statement = "TRUNCATE game";
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new ResponseException("SQL Exception (" + ex.getMessage() + ")", 500);
        } catch (DataAccessException ex) {
            throw new ResponseException(" Cannot connect to the Database", 500);
        }
    }

    private String createStatement = """
        CREATE TABLE IF NOT EXISTS game (
            gameID INT NOT NULL AUTO_INCREMENT,
            whiteUsername VARCHAR(256) DEFAULT NULL,
            blackUsername VARCHAR(256) DEFAULT NULL,
            gameName VARCHAR(256) NOT NULL,
            game TEXT NOT NULL, 
            PRIMARY KEY (gameID)
        )
        """;

    public void configureDatabase() throws DataAccessException, SQLException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(createStatement)) {
                ps.executeUpdate();
            }
        }
    }
}
