package dataaccess;

import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserDAOTests {

    private UserDAO userDAO;
    private boolean isSQL;

    public UserDAOTests() {
        try {
            userDAO = new SQLUserDAO();
            isSQL = true;
        } catch (Exception ex) {
            isSQL = false;
        }
    }

    @BeforeEach
    public void clear() throws Exception {
        userDAO.clearUsers();
        Assertions.assertTrue(isSQL);
    }

    @Test
    public void createUser() {
        Assertions.assertDoesNotThrow(() -> {
            userDAO.createUser("username", "password", "email");
        });
    }

    @Test
    public void createAlreadyTakenUser() {
        createUser();
        Assertions.assertThrows(Exception.class, () -> {
            userDAO.createUser("username", "password", "email");
        });
    }

    @Test
    public void createNullUser() {
        Assertions.assertThrows(Exception.class, () -> {
            userDAO.createUser(null, null, null);
        });
    }

    @Test
    public void getUser() {
        createUser();
        Assertions.assertDoesNotThrow(() -> {
            UserData userData = userDAO.getUser("username");
            Assertions.assertEquals(new UserData("username", "password", "email"), userData);
        });
    }

    @Test
    public void getNonexistingUser() {
        Assertions.assertThrows(Exception.class, () -> {
            userDAO.getUser("username");
        });
    }

    @Test
    public void clearCheck() {
        createUser();
        Assertions.assertDoesNotThrow(() -> {
            userDAO.clearUsers();
        });
        Assertions.assertThrows(Exception.class, () -> {
            userDAO.getUser("username");
        });
    }
}
