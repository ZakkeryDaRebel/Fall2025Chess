package ui;

import chess.ChessGame;
import connection.ServerFacade;
import connection.ServerMessageObserver;
import exception.ResponseException;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;
import java.util.Scanner;

public class ClientREPL implements ServerMessageObserver {

    private UserState state = UserState.OUT;
    private ServerFacade serverFacade;
    private ClientOUT clientOUT;
    private ClientIN clientIN;
    private ClientPLAY clientPLAY;
    private DrawBoard drawBoard;

    public ClientREPL(String serverURL) {
        serverFacade = new ServerFacade(serverURL, this);
        clientOUT = new ClientOUT(serverFacade);
        clientIN = new ClientIN(serverFacade);
        drawBoard = new DrawBoard();
        clientPLAY = new ClientPLAY(serverFacade, drawBoard);
    }

    public enum UserState {
        OUT,
        IN,
        PLAY
    }

    public String stateToString() {
        switch(state) {
            case OUT: return "[LOGGED OUT]";
            case IN: return "[SIGNED IN]";
            case PLAY: return "[PLAYING GAME]";
            default: return "[null state]";
        }
    }

    public void run() {
        System.out.println("\n Welcome to the Chess Game Interface (or CGI for short). Register or Login to start playing!");
        System.out.println(help());

        Scanner scan = new Scanner(System.in);
        String input = "";
        while (!(input.equals("2") && state == UserState.OUT) && !input.equalsIgnoreCase("Q") && !input.equalsIgnoreCase("Quit")) {
            printPrompt();
            input = scan.nextLine();
            //System.out.println("User input: " + input);   //Testing purposes
            System.out.println();
            try {
                evalInput(scan, input);
            } catch (ResponseException ex) {
                printError(ex.getMessage());
            }
        }
        System.out.println(" Thanks for playing at the CGI! Have a great rest of your day, and hope to see you soon!");
    }

    public void evalInput(Scanner scan, String input) throws ResponseException {
        if (input.equals("1") || input.equalsIgnoreCase("H") || input.equalsIgnoreCase("Help")) {
            System.out.println(help());
            return;
        }
        if (input.equals("0~Clear")) {
            serverFacade.clear();
            state = UserState.OUT;
            System.out.println(" The CGI has been completely reset");
            return;
        }
        switch (state) {
            case OUT: {
                evalResult(clientOUT.outEval(scan, input));
                break;
            }
            case IN: {
                evalResult(clientIN.inEval(scan, input));
                break;
            }
            case PLAY: {
                evalResult(clientPLAY.playEval(scan, input));
                break;
            }
            default: error();
        }
    }

    public void evalResult(String result) throws ResponseException {
        if (result.startsWith("authToken:")) {
            state = UserState.IN;
            clientIN.updateAuthToken(result.substring(10));
            System.out.println("\n You have successfully signed into the CGI");
            System.out.println(help());
        } else if (result.equals("invalid input")) {
            error();
        } else if (result.equals("logout")) {
            state = UserState.OUT;
            System.out.println("\n You have successfully logged out of the CGI");
            System.out.println(help());
        } else if (result.startsWith("Message:")) {
            printMessage(result.substring(8));
        } else if (result.startsWith("Error:")) {
            printError(result);
        } else if (result.startsWith(" Here is a list of games currently in the CGI: \n")) {
            System.out.println(result);
        } else if (result.startsWith("play")) {
            state = UserState.PLAY;
            clientPLAY.setObserver(false);
            clientPLAY.setPlayColor(clientIN.getColor());
            clientPLAY.setGameInfo(clientIN.getCurrentGame());
            System.out.println("\n You have successfully joined the game as " +
                    (clientIN.getColor() == ChessGame.TeamColor.WHITE ? "White" : "Black"));
            //Websocket
            ChessGame game = new ChessGame();
            clientPLAY.setGame(game);
            drawBoard.drawBoard(game, clientIN.getColor(), null);
            System.out.println(help());
        } else if (result.startsWith("observe")) {
            state = UserState.PLAY;
            clientPLAY.setObserver(true);
            clientPLAY.setPlayColor(ChessGame.TeamColor.WHITE);
            clientPLAY.setGameInfo(clientIN.getCurrentGame());
            System.out.println("\n You have succesfully joined the game as an observer");
            //Websocket
            ChessGame game = new ChessGame();
            clientPLAY.setGame(game);
            drawBoard.drawBoard(game, clientIN.getColor(), null);
            System.out.println(help());
        } else if (result.startsWith("leave")) {
            state = UserState.IN;
            clientIN.resetGame();
            System.out.println("\n You have successfully left the game");
            System.out.println(help());
        } else if (result.startsWith("quit")) {
            state = UserState.OUT;
        }
    }

    public void notify(ServerMessage message) {
        if (message.getServerMessageType() == ServerMessage.ServerMessageType.ERROR) {
            printError(((ErrorMessage) message).getErrorMessage());
        } else if (message.getServerMessageType() == ServerMessage.ServerMessageType.NOTIFICATION) {
            printMessage(((NotificationMessage) message).getMessage());
        } else {
            LoadGameMessage lg = (LoadGameMessage) message;
            clientPLAY.setGameInfo(lg.getGame());
            try {
                drawBoard.drawBoard(lg.getGame().game(), clientIN.getColor(), null);
            } catch (ResponseException ex) {
                printError(ex.getMessage());
            }
        }
    }

    public void printMessage(String message) {
        System.out.println(message);
    }

    public void printError(String error) {
        System.out.println("\n Sorry, we have received this error message from the CGI server\n   " +
                setRedText() + error + resetText() + "\n");
    }

    public void printPrompt() {
        System.out.print(" " + stateToString() + ">>> ");
    }

    public void error() {
        System.out.println("\n Sorry, that is an invalid input. Please don't add any spaces, and follow the help instructions");
        System.out.println(help());
    }

    public String help() {
        switch (state) {
            case OUT: return " - Enter " + setYellowText() + "\"1\", \"H\", or \"Help\" " + resetText() +
                                "to show this list of actions you can take again"
                    + "\n" + " - Enter " + setYellowText() + "\"2\", \"Q\", or \"Quit\" " + resetText() +
                                "to exit the Chess Game Interface (or CGI)"
                    + "\n" + " - Enter " + setYellowText() + "\"3\", \"R\", or \"Register\" " + resetText() +
                                "to create a new user"
                    + "\n" + "         (You will need to supply a username, password, and email)"
                    + "\n" + " - Enter " + setYellowText() + "\"4\", \"L\", or \"Login\" " + resetText() +
                                "to login as an existing user"
                    + "\n" + "         (You will need to supply a username and password)";

            case IN: return  " - Enter \"1\", \"H\", or \"Help\" to show this list of actions you can take again"
                    + "\n" + " - Enter \"2\", \"Q\", or \"Quit\" to logout and exit the Chess Game Interface (or CGI)"
                    + "\n" + " - Enter \"3\", \"G\", or \"Logout\" to logout and return to pre signed in state"
                    + "\n" + " - Enter \"4\", \"C\", or \"Create\" to create a new game"
                    + "\n" + "         (You will need to supply a game name)"
                    + "\n" + " - Enter \"5\", \"L\", or \"List\" to list all the games"
                    + "\n" + " - Enter \"6\", \"P\", or \"Play\" to join a game as a player"
                    + "\n" + "         (You will need to supply a game number and player color)"
                    + "\n" + " - Enter \"7\", \"O\", or \"Observe\" to join a game as an observer"
                    + "\n" + "         (You will need to supply a game number)";

            case PLAY: {
                String output = " - Enter \"1\", \"H\", or \"Help\" to show this list of actions you can take again"
                        + "\n" + " - Enter \"2\", \"L\", or \"Leave\" to leave the game and return to signed in state"
                        + "\n" + " - Enter \"3\", \"P\" or \"Piece\" to change the chess pieces to either text or icons"
                        + "\n" + " - Enter \"4\", \"C\" or \"Color\" to change the color format of the chess board"
                        + "\n" + " - Enter \"5\", \"I\", or \"Highlight\" to highlight the legal moves for a chess piece"
                        + "\n" + "         (You will need to supply the row and column of the piece you want to check)"
                        + "\n" + " - Enter \"6\", \"D\", or \"Draw\" to redraw the chess board";
                if (!clientPLAY.isObserver()) {
                    output += "\n" + " - Enter \"7\", \"M\", or \"Move\" to move a chess piece"
                            + "\n" + "         (You will need to supply the row and column of the piece you want to move,"
                            + "\n" + "           and the row and column of where you want to move the piece to)"
                            + "\n" + " - Enter \"8\", \"R\", or \"Resign\" to resign the game";
                }
                return output;
            }
            default: return  " We apologize, the CGI has last track of where you are in the system,"
                    + "\n" + " Please enter \"2\", \"Q\", or \"Quit\" to quit the CGI.";
        }
    }

    public String setYellowText() {
        return EscapeSequences.SET_TEXT_COLOR_YELLOW;
    }

    public String resetText() {
        return EscapeSequences.RESET_TEXT_COLOR;
    }

    public String setRedText() {
        return EscapeSequences.SET_TEXT_COLOR_RED;
    }
}
