package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import connection.ServerFacade;
import exception.ResponseException;
import model.GameData;

import java.util.Scanner;

public class ClientPLAY {

    private final ServerFacade serverFacade;
    private GameData gameData;
    private ChessGame.TeamColor playColor;
    private boolean isObserver;
    private final DrawBoard drawBoard;

    public ClientPLAY(ServerFacade serverFacade, DrawBoard drawBoard) {
        this.serverFacade = serverFacade;
        this.drawBoard = drawBoard;
    }

    public void setPlayColor(ChessGame.TeamColor color) {
        playColor = color;
    }

    public void setObserver(boolean isObserver) {
        this.isObserver = isObserver;
    }

    public Boolean isObserver() {
        return isObserver;
    }

    public void setGameInfo(GameData game) {
        gameData = game;
    }

    public void setGame(ChessGame game) {
        gameData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game);
    }

    public String playEval(Scanner scan, String input) throws ResponseException {
        if (input.equals("2") || input.equalsIgnoreCase("L") || input.equalsIgnoreCase("Leave")) {

            //Websocket LEAVE

            return "leave";
        } else if (input.equals("3")) {
            System.out.println(" Would you like your chess pieces to look like the chess piece icons, or use a text letter?");
            System.out.println(" - Enter \"1\", \"P\", or \"Piece\" if you would like your chess pieces to look like '" + EscapeSequences.BLACK_QUEEN + "'");
            System.out.println(" - Enter \"2\", \"T\", or \"Text\" if you would like your chess pieces to look like 'Q'");
            printPrompt();
            String choice = scan.nextLine();
            if (choice.equals("1") || choice.equalsIgnoreCase("P") || choice.equalsIgnoreCase("Piece")) {
                drawBoard.setPieceFont(true);
            } else if (choice.equals("2") || choice.equalsIgnoreCase("T") || choice.equalsIgnoreCase("Text")) {
                drawBoard.setPieceFont(false);
            } else {
                return "invalid input";
            }
            return "";
        } else if (input.equals("4") || input.equalsIgnoreCase("C") || input.equalsIgnoreCase("Color")) {
            System.out.println(" Please select the board color format that you would like to play with");
            drawBoard.printColorOptions();
            printPrompt();
            String choice = scan.nextLine();
            if (choice.equals("1")) {
                drawBoard.setColorFormat(0);
            } else if (choice.equals("2")) {
                drawBoard.setColorFormat(1);
            } else {
                return "invalid input";
            }
            return "Message: Successfully updated the color";
        } else if (input.equals("5") || input.equalsIgnoreCase("I") || input.equalsIgnoreCase("Highlight")) {
            System.out.println(" Please enter the location of the piece you would like to highlight");
            ChessPosition highlightPos;
            try {
                highlightPos = getChessPosition(scan);
            } catch (ResponseException e) {
                return "invalid input";
            }
            drawBoard.drawBoard(gameData.game(), playColor, highlightPos);
            return "";
        } else if (input.equals("6") || input.equalsIgnoreCase("D") || input.equalsIgnoreCase("Draw")) {
            drawBoard.drawBoard(gameData.game(), playColor, null);
            return "";
        } else if (!isObserver && input.equals("7") || input.equalsIgnoreCase("M") || input.equalsIgnoreCase("Move")) {
            System.out.println(" Please enter the location of the piece you would like to move"
                    + "\n" +   "   (For example, to move the pawn from a2 to a4, put 'a' then '2')");
            ChessPosition startPos;
            ChessPosition endPos;
            ChessPiece.PieceType promotion;
            try {
                startPos = getChessPosition(scan);
                System.out.println(" Please enter the location of where you would like to move the piece to"
                        + "\n" +   "   (For example, to move the pawn from a2 to a4, put 'a' then '4')");
                endPos = getChessPosition(scan);
                promotion = getPromotion(scan, startPos, endPos);
            } catch (ResponseException e) {
                return "invalid input";
            }
            ChessMove newMove = new ChessMove(startPos, endPos, promotion);

            //Websocket Make Move
            return "Message: Not implemented yet";

        } else if (!isObserver && input.equals("8") || input.equalsIgnoreCase("R") || input.equalsIgnoreCase("Resign")) {
            System.out.println(" You are about to resign the game. To confirm, please type \"Yes\", or anything else to cancel");
            printPrompt();
            String confirmation = scan.nextLine();
            if (confirmation.equalsIgnoreCase("Yes")) {

                //Websocket Resign
                return "Message: Not implemented yet";

            } else {
                return "Message: You have cancelled the resign";
            }
        } else {
            return "invalid input";
        }
    }

    public void printPrompt() {
        System.out.print(" [PLAYING GAME]>>> ");
    }

    public ChessPosition getChessPosition(Scanner scan) throws ResponseException {
        System.out.println(" ~ Please input the column (a - h)");
        printPrompt();
        String choice = scan.nextLine();
        ChessMove test = new ChessMove(null, null, null);
        int col = getColNum(choice);
        if (col == -1) {
            throw new ResponseException("invalid input", 0);
        }
        System.out.println(" ~ Please input the row (1-8)");
        printPrompt();
        choice = scan.nextLine();
        int row;
        try {
            row = Integer.parseInt(choice);
            if (row < 1 || row > 8) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            throw new ResponseException("invalid input", 0);
        }
        return new ChessPosition(row, col);
    }

    public int getColNum(String choice) {
        if (choice.equalsIgnoreCase("a")) {
            return 1;
        } else if (choice.equalsIgnoreCase("b")) {
            return 2;
        } else if (choice.equalsIgnoreCase("c")) {
            return 3;
        } else if (choice.equalsIgnoreCase("d")) {
            return 4;
        } else if (choice.equalsIgnoreCase("e")) {
            return 5;
        } else if (choice.equalsIgnoreCase("f")) {
            return 6;
        } else if (choice.equalsIgnoreCase("g")) {
            return 7;
        } else if (choice.equalsIgnoreCase("h")) {
            return 8;
        } else {
            return -1;
        }
    }

    public ChessPiece.PieceType getPromotion(Scanner scan, ChessPosition startPos, ChessPosition endPos) throws ResponseException {
        ChessPiece piece = gameData.game().getBoard().getPiece(startPos);
        if (piece.getPieceType() != ChessPiece.PieceType.PAWN) {
            return null;
        }
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE && endPos.getRow() == 8) {
            return requestPromotionType(scan);
        } else if (piece.getTeamColor() == ChessGame.TeamColor.BLACK && endPos.getRow() == 1) {
            return requestPromotionType(scan);
        } else {
            return null;
        }
    }

    public ChessPiece.PieceType requestPromotionType(Scanner scan) throws ResponseException {
        System.out.println(" Looks like this pawn can promote! What piece would you like to promote it to?"
                + "\n" +   " - Enter \"1\", \"Q\", or \"Queen\""
                + "\n" +   " - Enter \"2\", \"R\", or \"Rook\""
                + "\n" +   " - Enter \"3\", \"B\", or \"Bishop\""
                + "\n" +   " - Enter \"4\", \"N\", or \"Knight\"");
        printPrompt();
        String promotion = scan.nextLine();
        if (promotion.equals("1") || promotion.equalsIgnoreCase("Q") || promotion.equalsIgnoreCase("Queen")) {
            return ChessPiece.PieceType.QUEEN;
        } else if (promotion.equals("2") || promotion.equalsIgnoreCase("R") || promotion.equalsIgnoreCase("Rook")) {
            return ChessPiece.PieceType.ROOK;
        } else if (promotion.equals("3") || promotion.equalsIgnoreCase("B") || promotion.equalsIgnoreCase("Bishop")) {
            return ChessPiece.PieceType.BISHOP;
        } else if (promotion.equalsIgnoreCase("4") || promotion.equalsIgnoreCase("N") || promotion.equalsIgnoreCase("Knight")) {
            return ChessPiece.PieceType.KNIGHT;
        } else {
            throw new ResponseException("invalid input", 0);
        }
    }
}
