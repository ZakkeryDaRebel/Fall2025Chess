package service;

import dataaccess.*;
import exception.ResponseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import requests.LoginRequest;
import requests.LogoutRequest;
import requests.RegisterRequest;
import results.LoginResult;
import results.RegisterResult;

public class UserServiceTests {

    AuthDAO authDAO = new MemoryAuthDAO();
    UserDAO userDAO = new MemoryUserDAO();
    UserService userService = new UserService(authDAO, userDAO);

    @Test
    public void goodRegistration() throws ResponseException, DataAccessException {
        RegisterResult result = userService.register(new RegisterRequest("username", "password", "email"));
        Assertions.assertNotNull(userDAO.getUser(result.username()));
        Assertions.assertNotNull(authDAO.getAuth(result.authToken()));
    }

    @Test
    public void noNameRegistration() {
        Assertions.assertThrows(ResponseException.class, () -> {
            userService.register(new RegisterRequest(null, "password", "email"));
        });
    }

    @Test
    public void noPasswordRegistration() {
        Assertions.assertThrows(ResponseException.class, () -> {
            userService.register(new RegisterRequest("username", null, "email"));
        });
    }

    @Test
    public void noEmailRegistration() {
        Assertions.assertThrows(ResponseException.class, () -> {
            userService.register(new RegisterRequest("username", "password", null));
        });
    }

    @Test
    public void nameTakenRegistration() throws ResponseException, DataAccessException {
        goodRegistration();
        Assertions.assertThrows(ResponseException.class, this::goodRegistration);
    }


    @Test
    public void goodLogin() throws ResponseException, DataAccessException {
        goodRegistration();
        LoginResult result = userService.login(new LoginRequest("username", "password"));
        Assertions.assertNotNull(authDAO.getAuth(result.authToken()));
    }

    @Test
    public void noUsernameLogin() {
        Assertions.assertThrows(ResponseException.class, () -> {
            goodRegistration();
            userService.login(new LoginRequest(null, "password"));
        });
    }

    @Test
    public void noPasswordLogin() {
        Assertions.assertThrows(ResponseException.class, () -> {
            goodRegistration();
            userService.login(new LoginRequest("username", null));
        });
    }

    @Test
    public void noRegisterLogin() {
        Assertions.assertThrows(ResponseException.class, () -> {
            userService.login(new LoginRequest("username", "password"));
        });
    }

    @Test
    public void badPasswordLogin() {
        Assertions.assertThrows(ResponseException.class, () -> {
            goodRegistration();
            userService.login(new LoginRequest("username", "invalid_password"));
        });
    }


    @Test
    public void goodLogout() throws ResponseException {
        RegisterResult result = userService.register(new RegisterRequest("username", "password", "email"));
        userService.logout(new LogoutRequest(result.authToken()));
        Assertions.assertThrows(DataAccessException.class, () -> {
            authDAO.getAuth(result.authToken());
        });
    }

    @Test
    public void badAuthTokenLogout() {
        Assertions.assertThrows(ResponseException.class, () -> {
            goodRegistration();
            userService.logout(new LogoutRequest("1234"));
        });
    }
}
