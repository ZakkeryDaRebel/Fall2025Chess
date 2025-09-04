package chess;

import chess.extracreditcalculators.CastleCalculator;
import chess.extracreditcalculators.EnPassantCalculator;
import chess.movecalculators.AttackKingCalculator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor teamTurn;
    private ChessBoard board;
    private EnPassantCalculator enPassantCal;
    private CastleCalculator castleCal;
    private boolean isGameOver;


    public ChessGame() {
        teamTurn = TeamColor.WHITE;
        board = new ChessBoard();
        board.resetBoard();
        enPassantCal = new EnPassantCalculator();
        castleCal = new CastleCalculator();
        isGameOver = false;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        //check if position is on the board
        if (startPosition.getRow() < 1 || startPosition.getRow() > 8 ||
                startPosition.getColumn() < 1 || startPosition.getColumn() > 8) {
            return null;
        }

        //Check if there is no piece at position
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }

        //Get all the pieceMoves to go through and check
        Collection<ChessMove> possibleMoves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();
        TeamColor color = board.getPiece(startPosition).getTeamColor();

        //Loop through possibleMoves and check to see if the king is in check after executing a move
        for (ChessMove move : possibleMoves) {
            ChessGame testGame = new ChessGame();
            ChessBoard testBoard = board.clone();
            if (testBoard == null) {
                System.out.println("FAIL to clone, returning null");
                return null;
            }
            testGame.setBoard(testBoard);
            testGame.executeMove(move);
            if (!testGame.isInCheck(color)) {
                validMoves.add(move);
            }
        }

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            enPassantCal.checkEnPassant(board, startPosition, validMoves);
        } else if (piece.getPieceType() == ChessPiece.PieceType.KING && !isInCheck(color)) {
            castleCal.checkCastling(board, startPosition, validMoves);
        }

        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPos = move.getStartPosition();
        ChessPosition endPos = move.getEndPosition();

        //No piece at startPosition
        ChessPiece piece = board.getPiece(startPos);
        if (piece == null) {
            throw new InvalidMoveException();
        }

        //No valid moves at startPosition
        if (validMoves(startPos) == null || !validMoves(startPos).contains(move)) {
            throw new InvalidMoveException();
        }

        //Not the piece at startPosition's turn
        TeamColor color = piece.getTeamColor();
        if (color != teamTurn) {
            throw new InvalidMoveException();
        }

        //Check to see if the Pawn did EnPassant to update the other piece
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            if (enPassantCal.isEnPassantMove(board, move)) {
                ChessPosition capturedPiece = new ChessPosition(startPos.getRow(), endPos.getColumn());
                //Remove piece that got en passant ed
                board.addPiece(capturedPiece, null);
            }
        }

        //Check to see if the King did Castle to update the other piece
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            if (castleCal.isCastlingMove(move)) {
                //Move the rook that castled with the King
                //Default to castle Queen side
                int startCol = 1;
                int endCol = 4;
                //Check to see if castled King side
                if (endPos.getColumn() == 7) {
                    startCol = 8;
                    endCol = 6;
                }
                ChessPosition castleStart = new ChessPosition(startPos.getRow(), startCol);
                ChessPosition castleEnd = new ChessPosition(startPos.getRow(), endCol);
                executeMove(new ChessMove(castleStart, castleEnd, null));


            }
            //The king has moved, it can no longer castle
            if (color == TeamColor.WHITE) {
                castleCal.setCastleBool(TeamColor.WHITE, 1, false);
            } else {
                castleCal.setCastleBool(TeamColor.BLACK, 1, false);
            }
        }

        //If it was a rook that moved, possibly need to update castling abilities
        if (piece.getPieceType() == ChessPiece.PieceType.ROOK) {
            //See if the rook was originally in A column or H column, and set that to false
            castleCal.checkRookCastling(startPos);
        }

        //See if anyone captured the Rook's corner
        castleCal.checkRookCastling(endPos);

        executeMove(move);
        enPassantCal.setLastMove(move);

        //Swap team turn
        if (teamTurn == TeamColor.WHITE) {
            teamTurn = TeamColor.BLACK;
        } else {
            teamTurn = TeamColor.WHITE;
        }

        //Check to see if game is over
        if (isInCheckmate(teamTurn) || isInStalemate(teamTurn)) {
            isGameOver = true;
        }
    }

    public void executeMove(ChessMove move) {
        ChessPiece oldPiece = board.getPiece(move.getStartPosition());
        //Remove the piece from the start position
        board.addPiece(move.getStartPosition(), null);
        //If the piece doesn't promote, put the same piece at the end position
        if (move.getPromotionPiece() == null) {
            board.addPiece(move.getEndPosition(), oldPiece);
            //Otherwise, but the new promotion type at the end position
        } else {
            board.addPiece(move.getEndPosition(), new ChessPiece(oldPiece.getTeamColor(), move.getPromotionPiece()));
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKing(teamColor);
        if (kingPosition == null) {
            //If you can't find the king, then the king can't be in check
            return false;
        }
        return AttackKingCalculator.canAttackKing(board, teamColor, kingPosition);
    }



    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && noTeamMoves(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && noTeamMoves(teamColor);
    }

    public boolean noTeamMoves(TeamColor teamColor) {
        for (int row = 1; row < 9; row++) {
            for (int col = 1; col < 9; col++) {
                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(new ChessPosition(row, col));
                    if (moves != null && !moves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;

        enPassantCal.setLastMove(null);
        castleCal.loadBoard(board);
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    public ChessPosition findKing(TeamColor color) {
        for (int row = 1; row < 9; row++) {
            for (int col = 1; col < 9; col++) {
                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                if (piece != null &&
                        piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == color) {
                    return new ChessPosition(row, col);
                }
            }
        }
        return null;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public void setIsGameOver(boolean isGameOver) {
        this.isGameOver = isGameOver;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        boolean over = isGameOver == chessGame.isGameOver;
        boolean turn = teamTurn == chessGame.teamTurn;
        boolean squares = Objects.equals(board, chessGame.board);
        boolean pawn = Objects.equals(enPassantCal, chessGame.enPassantCal);
        boolean castle = Objects.equals(castleCal, chessGame.castleCal);
        return over && turn && squares && pawn && castle;
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, board, enPassantCal, castleCal, isGameOver);
    }
}
