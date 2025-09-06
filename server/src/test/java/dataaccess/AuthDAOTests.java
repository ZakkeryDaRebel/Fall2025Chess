package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AuthDAOTests {

    private AuthDAO authDAO;
    private boolean isSQL;

    public AuthDAOTests() {
        try {
            authDAO = new SQLAuthDAO();
            isSQL = true;
        } catch(Exception ex) {
            isSQL = false;
        }
    }

    @BeforeEach
    public void clear() throws Exception {
        authDAO.clearAuths();
        Assertions.assertTrue(isSQL);
    }

    @Test
    public void createAuth() {
        Assertions.assertDoesNotThrow(() -> {
            authDAO.createAuth("username", "1234");
        });
    }

    @Test
    public void createWithNullAuth() {
        Assertions.assertThrows(Exception.class, () -> {
            authDAO.createAuth(null, null);
        });
    }

    @Test
    public void getAuth() {
        createAuth();
        Assertions.assertDoesNotThrow(() -> {
            AuthData authData = authDAO.getAuth("1234");
            Assertions.assertEquals(new AuthData("1234", "username"), authData);
        });
    }

    @Test
    public void getNonexistingAuth() {
        Assertions.assertThrows(Exception.class, () -> {
            authDAO.getAuth("1234");
        });
    }

    @Test
    public void getBadAuth() {
        Assertions.assertThrows(Exception.class, () -> {
            authDAO.getAuth(null);
        });
    }

    @Test
    public void deleteAuth() {
        createAuth();
        Assertions.assertDoesNotThrow(() -> {
            authDAO.deleteAuth("1234");
        });
    }

    @Test
    public void deleteNonexistingAuth() {
        createAuth();
        //Deleting something that doesn't exist in SQL doesn't throw an exception
        Assertions.assertDoesNotThrow(() -> {
            authDAO.deleteAuth("9876");
            Assertions.assertEquals(new AuthData("1234", "username"), authDAO.getAuth("1234"));
        });
    }

    @Test
    public void deleteBadAuth() {
        createAuth();
        //Deleting something that doesn't exist in SQL doesn't throw an exception
        Assertions.assertDoesNotThrow(() -> {
            authDAO.deleteAuth(null);
            Assertions.assertEquals(new AuthData("1234", "username"), authDAO.getAuth("1234"));
        });
    }

    @Test
    public void clearAuths() {
        createAuth();
        Assertions.assertDoesNotThrow(() -> {
            authDAO.clearAuths();
        });
        Assertions.assertThrows(Exception.class, () -> {
            authDAO.getAuth("1234");
        });
    }
}
