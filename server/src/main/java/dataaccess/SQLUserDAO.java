package dataaccess;

import exception.ResponseException;
import model.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLUserDAO implements UserDAO {

    public SQLUserDAO() throws DataAccessException, SQLException {
        configureDatabase();
    }

    public void createUser(String username, String password, String email) throws ResponseException {
        String statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                ps.setString(2, password);
                ps.setString(3, email);
                ps.executeUpdate();
            }
        } catch(DataAccessException ex) {
            throw new ResponseException("Cannot connect to database (" + ex.getMessage() + ")", 500);
        } catch(SQLException ex) {
            throw new ResponseException("SQL Exception (" + ex.getMessage() + ")", 500);
        }
    }

    public UserData getUser(String username) throws DataAccessException, ResponseException {
        String statement = "SELECT password, email FROM user WHERE username=?";
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1,username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String password = rs.getString("password");
                        String email = rs.getString("email");
                        return new UserData(username, password, email);
                    } else {
                        throw new DataAccessException("Error: No such user");
                    }
                }
            }
        } catch (SQLException ex) {
            throw new ResponseException("SQL Exception (" + ex.getMessage() + ")", 500);
        }
    }

    public void clearUsers() throws ResponseException {
        String statement = "TRUNCATE TABLE user";
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new ResponseException("SQL Exception (" + ex.getMessage() + ")", 500);
        } catch (DataAccessException ex) {
            throw new ResponseException("Cannot connect to Database", 500);
        }
    }

    private String createStatement = """
        CREATE TABLE IF NOT EXISTS user (
            username VARCHAR(256) NOT NULL,
            password VARCHAR(256) NOT NULL,
            email VARCHAR(256) NOT NULL,
            PRIMARY KEY (username)
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
