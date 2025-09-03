package chess.extracreditcalculators;

import chess.*;

import java.util.Collection;

public class EnPassantCalculator {

    ChessMove lastMove;

    public EnPassantCalculator() {
        lastMove = null;
    }

    public void setLastMove(ChessMove lastMove) {
        this.lastMove = lastMove;
    }

    public void checkEnPassant(ChessBoard board, ChessPosition startPosition, Collection<ChessMove> validMoves) {
        if (lastMove == null) {
            return;
        }

        ChessPosition lastStart = lastMove.getStartPosition();
        ChessPosition lastEnd = lastMove.getEndPosition();

        //check if lastMove was a pawn that moved from 2->4 or 7->5
        ChessPiece lastMovePiece = board.getPiece(lastEnd);
        if (lastMovePiece.getPieceType() != ChessPiece.PieceType.PAWN) {
            return;
        }

        if (lastMovePiece.getTeamColor() == ChessGame.TeamColor.WHITE)  {
            if (lastStart.getRow() == 2 && lastEnd.getRow() == 4) {
                if (startPosition.getRow() == 4) {
                    if (enPassantOneColumnOver(startPosition, lastEnd)) {
                        ChessPosition enPassant = new ChessPosition(3, lastEnd.getColumn());
                        validMoves.add(new ChessMove(startPosition, enPassant, null));
                    }
                }
            }
        } else {
            if (lastStart.getRow() == 7 && lastEnd.getRow() == 5) {
                if (startPosition.getRow() == 5) {
                    if (enPassantOneColumnOver(startPosition, lastEnd)) {
                        ChessPosition enPassant = new ChessPosition(6, lastEnd.getColumn());
                        validMoves.add(new ChessMove(startPosition, enPassant, null));
                    }
                }
            }
        }
    }

    public boolean enPassantOneColumnOver(ChessPosition startPosition, ChessPosition lastMoveEnd) {
        int lastMoveCol = lastMoveEnd.getColumn();
        return startPosition.getColumn() == lastMoveCol + 1 || startPosition.getColumn() == lastMoveCol - 1;
    }

    public boolean isEnPassantMove(ChessBoard board, ChessMove move) {
        //check to see if moving diagonally
        if (move.getStartPosition().getColumn() != move.getEndPosition().getColumn()) {
            //If there is a piece there, it is a normal move, otherwise it is enPassant
            return board.getPiece(move.getEndPosition()) == null;
        }
        return false;
    }
}
