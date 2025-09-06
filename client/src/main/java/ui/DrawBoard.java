package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import exception.ResponseException;

import java.util.ArrayList;

public class DrawBoard {

    private boolean chessPieceFont = true;
    private int colorFormat = 0;
    private final int COLOR_OPTIONS = 2;
    private final String[] headers = new String[]{" ", "a", "b", "c", "d", "e", "f", "g", "h", " "};

    public void drawBoard(ChessGame game, ChessGame.TeamColor color, ChessPosition highlightPos) throws ResponseException {
        if (color == ChessGame.TeamColor.BLACK) {
            drawBlack(game, highlightPos);
        } else {
            drawWhite(game, highlightPos);
        }
    }

    public void setPieceFont(boolean wantChessPieceFont) {
        chessPieceFont = wantChessPieceFont;
    }

    public void setColorFormat(int format) {
        colorFormat = format;
    }

    public void printColorOptions() {
        ChessPiece[][] testBoard = new ChessPiece[2][2];
        testBoard[0][0] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
        testBoard[0][1] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        testBoard[1][0] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
        testBoard[1][1] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);

        String[] testHeaders = new String[] {" ", "a", "b", " "};

        int originalFormat = colorFormat;
        for (int i = 0; i < COLOR_OPTIONS; i++) {
            colorFormat = i;
            System.out.println(" Option " + (i+1) + ")");

            System.out.print(getBorderColor() + getBorderText());
            for (int col = 0; col < testHeaders.length; col++) {
                System.out.print(emptySpace() + testHeaders[col] + " ");
            }
            System.out.println(resetAll());

            for (int row = 0; row < testBoard.length; row++) {
                System.out.print(getBorderColor() + getBorderText());
                System.out.print(emptySpace() + (row + 1) + " ");
                System.out.print(resetAll());

                for (int col = 0; col < testBoard.length; col++) {
                    System.out.print(printPiece(testBoard[row][col], (row + col) % 2 == 1, false));
                }

                System.out.print(getBorderColor() + getBorderText());
                System.out.print(emptySpace() + (row + 1) + " ");
                System.out.println(resetAll());
            }

            System.out.print(getBorderColor() + getBorderText());
            for (int col = 0; col < testHeaders.length; col++) {
                System.out.print(emptySpace() + testHeaders[col] + " ");
            }
            System.out.println(resetAll());
        }
        colorFormat = originalFormat;
    }

    private void drawWhite(ChessGame game, ChessPosition highlightPos) throws ResponseException {
        ArrayList<ChessMove> highlightMoves = getHighlightMoves(game, highlightPos);
        System.out.println(printCols(ChessGame.TeamColor.WHITE));
        for (int row = 8; row >= 1; row--) {
            System.out.print(printRow(row));
            for (int col = 1; col <= 8; col++) {
                ChessPosition checkPos = new ChessPosition(row, col);
                ChessPiece piece = game.getBoard().getPiece(checkPos);
                printSquare(checkPos, highlightMoves, piece);
            }
            System.out.println(printRow(row));
        }
        System.out.println(printCols(ChessGame.TeamColor.WHITE));
    }

    private void drawBlack(ChessGame game, ChessPosition highlightPos) throws ResponseException {
        ArrayList<ChessMove> highlightMoves = getHighlightMoves(game, highlightPos);
        System.out.println(printCols(ChessGame.TeamColor.BLACK));
        for (int row = 1; row <= 8; row++) {
            System.out.print(printRow(row));
            for (int col = 8; col >= 1; col--) {
                ChessPosition checkPos = new ChessPosition(row, col);
                ChessPiece piece = game.getBoard().getPiece(checkPos);
                printSquare(checkPos, highlightMoves, piece);
            }
            System.out.println(printRow(row));
        }
        System.out.println(printCols(ChessGame.TeamColor.BLACK));
    }

    private ArrayList<ChessMove> getHighlightMoves(ChessGame game, ChessPosition highlightPos) throws ResponseException {
        if (highlightPos == null) {
            return new ArrayList<>();
        } else {
            if (game.getBoard().getPiece(highlightPos) == null) {
                throw new ResponseException("Error: No piece at that spot", 0);
            }
            ArrayList<ChessMove> validMoves = (ArrayList<ChessMove>) game.validMoves(highlightPos);
            if (validMoves == null) {
                validMoves = new ArrayList<>();
            }
            validMoves.add(new ChessMove(highlightPos, highlightPos, null));
            return validMoves;
        }
    }

    private void printSquare(ChessPosition checkPos, ArrayList<ChessMove> highlightMoves, ChessPiece piece) {
        boolean shouldHighlight = false;
        for (int spot = 0; spot < highlightMoves.size(); spot++) {
            if (highlightMoves.get(spot).getEndPosition().equals(checkPos)) {
                highlightMoves.remove(spot);
                shouldHighlight = true;
                break;
            }
        }
        boolean isLightSquare = (checkPos.getRow() + checkPos.getColumn()) % 2 == 1;
        System.out.print(printPiece(piece, isLightSquare, shouldHighlight));
    }

    private String printCols(ChessGame.TeamColor color) {
        String cols = "";
        cols += getBorderColor() + getBorderText();
        if (color == ChessGame.TeamColor.WHITE) {
            for (int i = 0; i < headers.length; i++) {
                cols += emptySpace() + headers[i] + " ";
            }
        } else {
            for (int i = headers.length - 1; i >= 0; i--) {
                cols += emptySpace() + headers[i] + " ";
            }
        }
        return cols += resetAll();
    }

    private String printRow(int row) {
        String rows = "";
        rows += getBorderColor() + getBorderText();
        rows += emptySpace() + row + " ";
        return rows += resetAll();
    }

    private String emptySpace() {
        return (chessPieceFont ? EscapeSequences.EMPTY : " ");
    }

    private String getBorderColor() {
        switch(colorFormat) {
            case (1): {
                return EscapeSequences.SET_BG_COLOR_CYAN;
            }
            default: {
                return EscapeSequences.SET_BG_COLOR_DARK_GREY;
            }
        }
    }

    private String getBorderText() {
        switch(colorFormat) {
            case (1): {
                return EscapeSequences.SET_TEXT_COLOR_MAGENTA;
            }
            default: {
                return EscapeSequences.SET_TEXT_COLOR_WHITE;
            }
        }
    }

    private String getBGColor(boolean isLightSquare) {
        switch(colorFormat) {
            case (1): {
                return (isLightSquare ? EscapeSequences.SET_BG_COLOR_LIGHT_PINK : EscapeSequences.SET_BG_COLOR_BLUE);
            }
            default: {
                return (isLightSquare ? EscapeSequences.SET_BG_COLOR_CREAM : EscapeSequences.SET_BG_COLOR_DARK_GREEN);
            }
        }
    }

    private String getHighlightBG(boolean isLightSquare) {
        switch(colorFormat) {
            default: {
                return (isLightSquare ? EscapeSequences.SET_BG_COLOR_YELLOW : EscapeSequences.SET_BG_COLOR_ORANGE);
            }
        }
    }

    private String getTextColor(boolean isWhite) {
        switch(colorFormat) {
            case (1): {
                return (isWhite ? EscapeSequences.SET_TEXT_COLOR_YELLOW : EscapeSequences.SET_TEXT_COLOR_RED);
            }
            default: {
                return (isWhite ? EscapeSequences.SET_TEXT_COLOR_WHITE : EscapeSequences.SET_TEXT_COLOR_BLACK);
            }
        }
    }

    private String printPiece(ChessPiece piece, boolean isLightSquare, boolean shouldHighlight) {
        String printOut = "";
        if (!shouldHighlight) {
            printOut += getBGColor(isLightSquare);
        } else {
            printOut += getHighlightBG(isLightSquare);
        }
        if (piece == null) {
            printOut += emptySpace() + "  ";
        } else {
            printOut += getTextColor(piece.getTeamColor() == ChessGame.TeamColor.WHITE);
            printOut += emptySpace() + getPiece(piece.getPieceType()) + " ";
        }
        return printOut += resetAll();
    }

    private String getPiece(ChessPiece.PieceType piece) {
        switch(piece) {
            case PAWN: return (chessPieceFont ? EscapeSequences.BLACK_PAWN : "P");
            case ROOK: return (chessPieceFont ? EscapeSequences.BLACK_ROOK : "R");
            case KNIGHT: return (chessPieceFont ? EscapeSequences.BLACK_KNIGHT : "N");
            case BISHOP: return (chessPieceFont ? EscapeSequences.BLACK_BISHOP : "B");
            case QUEEN: return (chessPieceFont ? EscapeSequences.BLACK_QUEEN : "Q");
            case KING: return (chessPieceFont ? EscapeSequences.BLACK_KING : "K");
            case null, default: return emptySpace() + "  ";
        }
    }

    private String resetAll() {
        return EscapeSequences.RESET_TEXT_COLOR + EscapeSequences.RESET_BG_COLOR;
    }
}
