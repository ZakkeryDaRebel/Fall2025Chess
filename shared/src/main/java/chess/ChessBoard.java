package chess;

import chess.ChessPiece.PieceType;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard implements Cloneable {

    private ChessPiece[][] board;

    public ChessBoard() {
        board = new ChessPiece[8][8];
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        addRowPieces(0, ChessGame.TeamColor.WHITE);
        addRowPieces(7, ChessGame.TeamColor.BLACK);
        addPawns();
    }

    public void addRowPieces(int row, ChessGame.TeamColor color) {
        board[row][0] = new ChessPiece(color, PieceType.ROOK);
        board[row][1] = new ChessPiece(color, PieceType.KNIGHT);
        board[row][2] = new ChessPiece(color, PieceType.BISHOP);
        board[row][3] = new ChessPiece(color, PieceType.QUEEN);
        board[row][4] = new ChessPiece(color, PieceType.KING);
        board[row][5] = new ChessPiece(color, PieceType.BISHOP);
        board[row][6] = new ChessPiece(color, PieceType.KNIGHT);
        board[row][7] = new ChessPiece(color, PieceType.ROOK);
    }

    public void addPawns() {
        for (int col = 0; col < 8; col++) {
            board[1][col] = new ChessPiece(ChessGame.TeamColor.WHITE, PieceType.PAWN);
            board[6][col] = new ChessPiece(ChessGame.TeamColor.BLACK, PieceType.PAWN);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }

    @Override
    protected ChessBoard clone() {
        try {
            ChessBoard clone = new ChessBoard();
            ChessPiece[][] clonedBoard = new ChessPiece[8][8];
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    if (board[row][col] != null) {
                        clonedBoard[row][col] = board[row][col].clone();
                    }
                }
            }
            clone.board = clonedBoard;
            return clone;
        } catch (CloneNotSupportedException cloneEx) {
            System.out.println("Error to clone: " + cloneEx.getMessage());
            return null;
        }
    }
}
