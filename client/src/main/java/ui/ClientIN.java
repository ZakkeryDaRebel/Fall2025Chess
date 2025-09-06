package ui;

import chess.ChessGame;
import connection.ServerFacade;
import exception.ResponseException;
import model.GameData;
import requests.*;
import results.*;
import java.util.ArrayList;
import java.util.Scanner;

public class ClientIN {

    private String authToken;
    private ArrayList<GameData> gameList;
    private ServerFacade serverFacade;
    private GameData currentGame;
    private ChessGame.TeamColor playColor;


    public ClientIN(ServerFacade serverFacade) {
        this.serverFacade = serverFacade;
    }

    public void printPrompt() {
        System.out.print(" [SIGNED IN]>>> ");
    }

    public void updateAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String inEval(Scanner scan, String input) throws ResponseException {
        if (input.equals("2") || input.equalsIgnoreCase("Q") || input.equalsIgnoreCase("Quit")) {
            if (logout().equals("success")) {
                return "quit";
            }
            return "Error: Failed to quit";
        } else if (input.equals("3") || input.equalsIgnoreCase("G") || input.equalsIgnoreCase("Logout")) {
            if (logout().equals("success")) {
                return "logout";
            }
            return "Error: Failed to logout";
        } else if (input.equals("4") || input.equalsIgnoreCase("C") || input.equalsIgnoreCase("Create")) {
            System.out.println(" Please enter the name of the game you would like to create");
            printPrompt();
            String gameName = scan.nextLine();
            CreateGameRequest request = new CreateGameRequest(authToken, gameName);
            CreateGameResult result = serverFacade.createGame(request);
            if (result.gameID() > 0) {
                return "Message:Successfully created the game (" + gameName + ")";
            }
            return "Error: Failed to create the game (" + gameName + ")";
        } else if (input.equals("5") || input.equalsIgnoreCase("L") || input.equalsIgnoreCase("List")) {
            ListGamesRequest request = new ListGamesRequest(authToken);
            ListGamesResult result = serverFacade.listGames(request);
            if (result.games() == null) {
                return "Error: List games command returned no games";
            }
            gameList = (ArrayList<GameData>) result.games();
            if (gameList.isEmpty()) {
                return "Message: No current games";
            }

            return formatList();
        } else if (input.equals("6") || input.equalsIgnoreCase("P") || input.equalsIgnoreCase("Play")) {
            if (isListEmpty()) {
                return "empty list";
            }
            int gameID;
            try {
                gameID = getGameNumber(scan);
            } catch (NumberFormatException ex) {
                return "Error: That is not a number. Please try again";
            } catch (IndexOutOfBoundsException ex) {
                return "Error: That is not a valid game number. Please try again";
            }
            System.out.println(" Please enter \"W\" or \"White\" if you would like to play as White, or"
                      + "\n" + " Please enter \"B\" or \"Black\" if you would like to play as Black");
            printPrompt();
            String color = scan.nextLine();
            ChessGame.TeamColor playerColor;
            if (color.equalsIgnoreCase("W") || color.equalsIgnoreCase("White")) {
                playerColor = ChessGame.TeamColor.WHITE;
            } else if (color.equalsIgnoreCase("B") || color.equalsIgnoreCase("Black")) {
                playerColor = ChessGame.TeamColor.BLACK;
            } else {
                return "Error: Please enter a correct color option. Please try again";
            }
            JoinGameRequest request = new JoinGameRequest(authToken, playerColor, gameID);
            serverFacade.joinGame(request);
            playColor = playerColor;
            return "play";
        } else if (input.equals("7") || input.equalsIgnoreCase("O") || input.equalsIgnoreCase("Observe")) {
            if (isListEmpty()) {
                return "empty list";
            }
            int gameID;
            try {
                gameID = getGameNumber(scan);
            } catch (NumberFormatException ex) {
                return "Error: That is not a number. Please try again";
            } catch (IndexOutOfBoundsException ex) {
                return "Error: That is not a valid game number. Please try again";
            }
            //Send Websocket
            return "observe";
        } else {
            return "invalid input";
        }
    }

    public String logout() throws ResponseException {
        LogoutRequest request = new LogoutRequest(authToken);
        serverFacade.logout(request);
        authToken = null;
        return "success";
    }

    public String formatList() {
        StringBuilder list = new StringBuilder();
        list.append(" Here is a list of games currently in the CGI: \n");
        for (int i = 0; i < gameList.size(); i++) {
            GameData game = gameList.get(i);
            list.append(" " + (i + 1) + ") "+ game.gameName());
            list.append("\n   White User: " + (game.whiteUsername() == null ? "[AVAILABLE]" : game.whiteUsername()));
            list.append("\n   Black User: " + (game.blackUsername() == null ? "[AVAILABLE]" : game.blackUsername()));
            list.append("\n\n");
        }
        return list.toString();
    }

    public int getGameNumber(Scanner scan) throws NumberFormatException, IndexOutOfBoundsException {
        System.out.println(" Please enter the game number of the game you would like to play in");
        printPrompt();
        String gameNumberString = scan.nextLine();

        int gameNumber = Integer.parseInt(gameNumberString);

        GameData gameData = gameList.get(gameNumber - 1);
        currentGame = gameData;
        return gameData.gameID();
    }

    public boolean isListEmpty() {
        if (gameList == null) {
            System.out.println(" Before you can join a game, make sure you enter \"5\", \"L\", or \"List\""
                    + "\n" +   "   so that you can know what games are available to join");
            return true;
        } else if (gameList.isEmpty()) {
            System.out.println(" Sorry, there are no games available to join right now. If you would like, you can"
                    + "\n" +   "   Enter \"4\", \"C\", or \"Create\" to create a game or wait till someone else does");
            return true;
        } else {
            return false;
        }
    }

    public GameData getCurrentGame() {
        return currentGame;
    }

    public ChessGame.TeamColor getColor() {
        return playColor;
    }

    public void resetGame() {
        this.currentGame = null;
        this.playColor = null;
    }
}
