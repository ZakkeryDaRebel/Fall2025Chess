package dataaccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager {
    private static String databaseName;
    private static String dbUsername;
    private static String dbPassword;
    private static String connectionUrl;

    /*
     * Load the database information for the db.properties file.
     */
    static {
        loadPropertiesFromResources();
    }

    /**
     * Creates the database if it does not already exist.
     */
    static public void createDatabase() throws DataAccessException {
        var statement = "CREATE DATABASE IF NOT EXISTS " + databaseName;
        try (var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
             var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to create database", ex);
        }
    }

    /**
     * Create a connection to the database and sets the catalog based upon the
     * properties specified in db.properties. Connections to the database should
     * be short-lived, and you must close the connection when you are done with it.
     * The easiest way to do that is with a try-with-resource block.
     * <br/>
     * <code>
     * try (var conn = DatabaseManager.getConnection()) {
     * // execute SQL statements.
     * }
     * </code>
     */
    static Connection getConnection() throws DataAccessException {
        try {
            //do not wrap the following line with a try-with-resources
            var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
            conn.setCatalog(databaseName);
            return conn;
        } catch (SQLException ex) {
            throw new DataAccessException("failed to get connection", ex);
        }
    }

    private static void loadPropertiesFromResources() {
        try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
            if (propStream == null) {
                throw new Exception("Unable to load db.properties");
            }
            Properties props = new Properties();
            props.load(propStream);
            loadProperties(props);
        } catch (Exception ex) {
            throw new RuntimeException("unable to process db.properties", ex);
        }
    }

    private static void loadProperties(Properties props) {
        databaseName = props.getProperty("db.name");
        dbUsername = props.getProperty("db.user");
        dbPassword = props.getProperty("db.password");

        var host = props.getProperty("db.host");
        var port = Integer.parseInt(props.getProperty("db.port"));
        connectionUrl = String.format("jdbc:mysql://%s:%d", host, port);
    }

    private final static String createAuthStatement = """
        CREATE TABLE IF NOT EXISTS auth (
            authToken VARCHAR(256) NOT NULL,
            username VARCHAR(256) NOT NULL,
            PRIMARY KEY (authToken)
        )
        """;

    private final static String createGameStatement = """
        CREATE TABLE IF NOT EXISTS game (
            gameID INT NOT NULL AUTO_INCREMENT,
            whiteUsername VARCHAR(256) DEFAULT NULL,
            blackUsername VARCHAR(256) DEFAULT NULL,
            gameName VARCHAR(256) NOT NULL,
            game TEXT NOT NULL, 
            PRIMARY KEY (gameID)
        )
        """;

    private final static String createUserStatement = """
        CREATE TABLE IF NOT EXISTS user (
            username VARCHAR(256) NOT NULL,
            password VARCHAR(256) NOT NULL,
            email VARCHAR(256) NOT NULL,
            PRIMARY KEY (username)
        )
        """;

    public static void configureDatabase() throws DataAccessException, SQLException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement userPS = conn.prepareStatement(createUserStatement)) {
                userPS.executeUpdate();
            }
            try (PreparedStatement authPS = conn.prepareStatement(createAuthStatement)) {
                authPS.executeUpdate();
            }
            try (PreparedStatement gamePS = conn.prepareStatement(createGameStatement)) {
                gamePS.executeUpdate();
            }
        }
    }
}
