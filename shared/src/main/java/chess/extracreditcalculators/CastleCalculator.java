package chess.extracreditcalculators;

import chess.*;
import chess.movecalculators.AttackKingCalculator;

import java.util.Collection;

public class CastleCalculator {

    Boolean[] whiteCastling;
    Boolean[] blackCastling;
    AttackKingCalculator attackCal;

    public CastleCalculator() {
        attackCal = new AttackKingCalculator();
        //whiteCastling and blackCastling stores if the pieces for castling haven't moved
        whiteCastling = new Boolean[]{true, true, true};
        blackCastling = new Boolean[]{true, true, true};
        //castling[0] references the A rook, castling[1] references the King, and castling[2] references the H rook
    }

    public void setCastleBool(ChessGame.TeamColor color, int place, Boolean bool) {
        if (color == ChessGame.TeamColor.WHITE) {
            whiteCastling[place] = bool;
        } else {
            blackCastling[place] = bool;
        }
    }

    public void checkCastling(ChessBoard board, ChessPosition startPos, Collection<ChessMove> validMoves) {
        ChessGame.TeamColor color = board.getPiece(startPos).getTeamColor();
        Boolean[] castling;
        if (color == ChessGame.TeamColor.WHITE) {
            castling = whiteCastling;
        } else {
            castling = blackCastling;
        }

        //If king has moved or if the king is in check, then can't castle
        if (!castling[1]) {
            return;
        }

        //If a rook hasn't moved.
        if (castling[0] && checkCastleQueen(board, startPos, color)) {
            validMoves.add(new ChessMove(startPos, new ChessPosition(startPos.getRow(), 3), null));
        }

        //If h rook hasn't moved.
        if (castling[2] && checkCastleKing(board, startPos, color)) {
            validMoves.add(new ChessMove(startPos, new ChessPosition(startPos.getRow(), 7), null));
        }
    }

    public boolean checkCastleQueen(ChessBoard board, ChessPosition startPos, ChessGame.TeamColor color) {
        ChessPosition bSpot = new ChessPosition(startPos.getRow(), 2);
        ChessPiece bPiece = board.getPiece(bSpot);
        ChessPosition cSpot = new ChessPosition(startPos.getRow(), 3);
        ChessPiece cPiece = board.getPiece(cSpot);
        ChessPosition dSpot = new ChessPosition(startPos.getRow(), 4);
        ChessPiece dPiece = board.getPiece(dSpot);

        //No pieces between rook and king.
        if (bPiece != null || cPiece != null || dPiece != null) {
            return false;
        }

        //Make sure the king won't be attacked on b, c, or d column.
        return !attackCal.canAttackKing(board, color, cSpot) && !attackCal.canAttackKing(board, color, dSpot);
    }

    public boolean checkCastleKing(ChessBoard board, ChessPosition startPos, ChessGame.TeamColor color) {
        ChessPosition fSpot = new ChessPosition(startPos.getRow(), 6);
        ChessPiece fPiece = board.getPiece(fSpot);
        ChessPosition gSpot = new ChessPosition(startPos.getRow(), 7);
        ChessPiece gPiece = board.getPiece(gSpot);

        //No pieces between rook and king.
        if (fPiece != null || gPiece != null) {
            return false;
        }

        //Make sure the king won't be attacked on f or g column.
        return !attackCal.canAttackKing(board, color, fSpot) && !attackCal.canAttackKing(board, color, gSpot);
    }

    public void loadBoard(ChessBoard newBoard) {
        //Check to see if pieces are in places for castling
        ChessPiece whiteRook = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
        ChessPiece blackRook = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);
        ChessPiece whiteKing = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING);
        ChessPiece blackKing = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING);

        ChessPiece pieceAOne = newBoard.getPiece(new ChessPosition(1, 1));
        ChessPiece pieceEOne = newBoard.getPiece(new ChessPosition(1, 5));
        ChessPiece pieceHOne = newBoard.getPiece(new ChessPosition(1, 8));
        ChessPiece pieceAEight = newBoard.getPiece(new ChessPosition(8, 1));
        ChessPiece pieceEEight = newBoard.getPiece(new ChessPosition(8, 5));
        ChessPiece pieceHEight = newBoard.getPiece(new ChessPosition(8, 8));

        whiteCastling[0] = (pieceAOne != null && pieceAOne.equals(whiteRook));
        whiteCastling[1] = (pieceEOne != null && pieceEOne.equals(whiteKing));
        whiteCastling[2] = (pieceHOne != null && pieceHOne.equals(whiteRook));
        blackCastling[0] = (pieceAEight != null && pieceAEight.equals(blackRook));
        blackCastling[1] = (pieceEEight != null && pieceEEight.equals(blackKing));
        blackCastling[2] = (pieceHEight != null && pieceHEight.equals(blackRook));
    }

    public void checkRookCastling(ChessPosition movePos) {
        ChessPosition rookAOne = new ChessPosition(1,1);
        ChessPosition rookAEight = new ChessPosition(8,1);
        ChessPosition rookHOne = new ChessPosition(1, 8);
        ChessPosition rookHEight = new ChessPosition(8, 8);

        if (movePos.equals(rookAOne)) {
            whiteCastling[0] = false;
        } else if (movePos.equals(rookHOne)) {
            whiteCastling[2] = false;
        } else if (movePos.equals(rookAEight)) {
            blackCastling[0] = false;
        } else if (movePos.equals(rookHEight)) {
            blackCastling[2] = false;
        }
    }

    public boolean isCastlingMove(ChessMove move) {
        //If the king stays on the same row
        if(move.getStartPosition().getRow() == move.getEndPosition().getRow()) {
            //If the king tries to move 2 spaces to the right or left, it's a castling move
            return move.getStartPosition().getColumn() == move.getEndPosition().getColumn() - 2 ||
                    move.getStartPosition().getColumn() == move.getEndPosition().getColumn() + 2;
        }
        return false;
    }
}
