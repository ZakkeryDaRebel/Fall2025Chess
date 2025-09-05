package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import exception.ResponseException;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import requests.*;
import results.*;

import java.util.UUID;

public class UserService {

    private AuthDAO authDAO;
    private UserDAO userDAO;

    public UserService(AuthDAO authDAO, UserDAO userDAO) {
        this.authDAO = authDAO;
        this.userDAO = userDAO;
    }

    public RegisterResult register(RegisterRequest registerRequest) throws ResponseException {
        if (registerRequest.password()==null || registerRequest.username()==null || registerRequest.email()==null) {
            throw new ResponseException("Error: Bad request", 400);
        }

        try {
            userDAO.getUser(registerRequest.username());
            throw new ResponseException("Error: Already taken", 403);
        } catch (DataAccessException daex) {
            if (daex.getMessage().contains("cannot connect")) {
                throw new ResponseException("Error: " + daex.getMessage(), 500);
            }
            try {
                String hashPassword = hashPassword(registerRequest.password());
                userDAO.createUser(registerRequest.username(), hashPassword, registerRequest.email());
                String authToken = generateAuthToken();
                authDAO.createAuth(registerRequest.username(), authToken);
                return new RegisterResult(authToken, registerRequest.username());
            } catch (Exception ex) {
                throw new ResponseException("Error: " + ex.getMessage(), 500);
            }
        }
    }

    public LoginResult login(LoginRequest loginRequest) throws ResponseException {
        if (loginRequest.password() == null || loginRequest.username() == null) {
            throw new ResponseException("Error: Bad request", 400);
        }

        try {
            UserData userData = userDAO.getUser(loginRequest.username());
            comparePasswords(loginRequest.password(), userData.password());
            String authToken = generateAuthToken();
            authDAO.createAuth(loginRequest.username(), authToken);
            return new LoginResult(authToken, loginRequest.username());
        } catch (DataAccessException ex) {
            if (ex.getMessage().contains("Unauthorized") || ex.getMessage().contains("No such user")) {
                throw new ResponseException("Error: Unauthorized", 401);
            }
            throw new ResponseException("Error: " + ex.getMessage(), 500);
        } catch (Exception ex) {
            throw new ResponseException("Error: " + ex.getMessage(), 500);
        }
    }

    public void logout(LogoutRequest logoutRequest) throws ResponseException {
        if (logoutRequest.authToken() == null) {
            throw new ResponseException("Error: Bad request", 400);
        }

        try {
            authDAO.getAuth(logoutRequest.authToken());
            authDAO.deleteAuth(logoutRequest.authToken());
        } catch (DataAccessException ex) {
            if (ex.getMessage().contains("Unauthorized")) {
                throw new ResponseException("Error: Unauthorized", 401);
            }
            throw new ResponseException("Error: " + ex.getMessage(), 500);

        } catch (Exception ex) {
            throw new ResponseException("Error: " + ex.getMessage(), 500);
        }
    }

    public String generateAuthToken() {
        return UUID.randomUUID().toString();
    }

    public String hashPassword(String clearTextPassword) {
        return BCrypt.hashpw(clearTextPassword, BCrypt.gensalt());
    }

    public void comparePasswords(String userPassword, String dataBasePassword) throws DataAccessException {
        if (!userPassword.equals(dataBasePassword) && !BCrypt.checkpw(userPassword, dataBasePassword)) {
            throw new DataAccessException("Error: Unauthorized");
        }
    }
}
