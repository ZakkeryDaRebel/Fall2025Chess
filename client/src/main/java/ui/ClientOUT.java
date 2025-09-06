package ui;

import connection.ServerFacade;
import exception.ResponseException;
import requests.LoginRequest;
import requests.RegisterRequest;
import results.LoginResult;
import results.RegisterResult;
import java.util.Scanner;

public class ClientOUT {

    ServerFacade serverFacade;

    public ClientOUT(ServerFacade serverFacade) {
        this.serverFacade = serverFacade;
    }

    public void printPrompt() {
        System.out.print(" [LOGGED OUT]>>> ");
    }

    public String outEval(Scanner scan, String input) throws ResponseException {
        if (input.equals("2") || input.equalsIgnoreCase("Q") || input.equalsIgnoreCase("Quit")) {
            //Quit doesn't need to do anything when they haven't signed in yet
            return "quit";
        } else if (input.equals("3") || input.equalsIgnoreCase("R") || input.equalsIgnoreCase("Register")) {
            LoginRequest loginReq = getLoginInfo(scan);
            System.out.println("\n Please enter your email");
            printPrompt();
            String email = scan.nextLine();
            RegisterRequest registerReq = new RegisterRequest(loginReq.username(), loginReq.password(), email);

            RegisterResult result = serverFacade.register(registerReq);
            return "authToken:" + result.authToken();
        } else if (input.equals("4") || input.equalsIgnoreCase("L") || input.equalsIgnoreCase("Login")) {
            LoginRequest loginReq = getLoginInfo(scan);

            LoginResult result = serverFacade.login(loginReq);
            return "authToken:" + result.authToken();
        } else {
            return "invalid input";
        }
    }

    public LoginRequest getLoginInfo(Scanner scan) {
        System.out.println(" Please enter your username");
        printPrompt();
        String username = scan.nextLine();
        System.out.println("\n Please enter your password");
        printPrompt();
        String password = scan.nextLine();
        return new LoginRequest(username, password);
    }
}
