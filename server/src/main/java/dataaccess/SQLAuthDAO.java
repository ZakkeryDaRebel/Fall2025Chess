package dataaccess;

import exception.ResponseException;
import model.AuthData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLAuthDAO implements AuthDAO {

    public SQLAuthDAO() throws DataAccessException, SQLException {
        DatabaseManager.configureDatabase();
    }

    public void createAuth(String username, String authToken) throws ResponseException {
        String statement = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                ps.setString(2, username);
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new ResponseException("Failed to create Auth", 500);
        } catch (DataAccessException ex) {
            throw new ResponseException("Database failed to connect", 500);
        }
    }

    public AuthData getAuth(String authToken) throws DataAccessException, ResponseException {
        String statement = "SELECT username FROM auth WHERE authToken=?";
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String username = rs.getString("username");
                    return new AuthData(authToken, username);
                }
                throw new DataAccessException("Error: Unauthorized");
            }
        } catch (SQLException ex) {
            throw new ResponseException("SQL Exception (" + ex.getMessage() + ")", 500);
        }
    }

    public void deleteAuth(String authToken) throws ResponseException {
        String statement = "DELETE FROM auth WHERE authToken=?";
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new ResponseException("SQL Exception (" + ex.getMessage() + ")", 500);
        } catch (DataAccessException ex) {
            throw new ResponseException("Cannot connect to Database", 500);
        }
    }

    public void clearAuths() throws ResponseException {
        String statement = "TRUNCATE TABLE auth";
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new ResponseException("SQL Exception (" + ex.getMessage() + ")", 500);
        } catch (DataAccessException ex) {
            throw new ResponseException("Failed to connect to the Database", 500);
        }
    }
}
