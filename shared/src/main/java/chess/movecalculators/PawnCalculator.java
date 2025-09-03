package chess.movecalculators;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

public class PawnCalculator implements MoveCalculator {

    //PromotionRow is either 1 or 8, which is the promotion row for black or white pawns
    private int promotionRow;

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition pos) {
        Collection<ChessMove> moves = new ArrayList<>();

        ChessGame.TeamColor color;  //White or Black
        int direction;              //Does the pawn move up or down (based on color)
        int startRow;               //What is the starting row for a double move (based on color)

        if (board.getPiece(pos) == null) {
            return null;
        } else if (board.getPiece(pos).getTeamColor() == ChessGame.TeamColor.WHITE) {
            color = ChessGame.TeamColor.WHITE;
            direction = 1;
            startRow = 2;
            promotionRow = 8;
        } else {
            color = ChessGame.TeamColor.BLACK;
            direction = -1;
            startRow = 7;
            promotionRow = 1;
        }

        ChessPosition moveOne = new ChessPosition(pos.getRow() + direction, pos.getColumn());
        //Test If pawn can move one, stay on the board, and there is no piece already there
        if (onBoard(moveOne) && isEmpty(board, moveOne)) {
            addPromotions(moves, pos, moveOne);

            //Since pawn can move one, check if pawn can move 2
            if (pos.getRow() == startRow) {
                ChessPosition moveTwo = new ChessPosition(moveOne.getRow() + direction, pos.getColumn());
                if (onBoard(moveTwo) && isEmpty(board, moveTwo)) {
                    moves.add(new ChessMove(pos, moveTwo, null));
                }
            }
        }

        //Test if pawn can capture diagonally right and stay on the board
        ChessPosition rightCapture = new ChessPosition(pos.getRow() + direction, pos.getColumn() + 1);
        if (onBoard(rightCapture) && !isEmpty(board, rightCapture) && isEnemy(board.getPiece(rightCapture), color)) {
            addPromotions(moves, pos, rightCapture);
        }

        //Test if pawn can capture diagonally left and stay on the board
        ChessPosition leftCapture = new ChessPosition(pos.getRow() + direction, pos.getColumn() - 1);
        if (onBoard(leftCapture) && !isEmpty(board, leftCapture) && isEnemy(board.getPiece(leftCapture), color)) {
            addPromotions(moves, pos, leftCapture);
        }
        return moves;
    }

    //Adds all the valid promotions to moves
    private void addPromotions(Collection<ChessMove> moves, ChessPosition startPos, ChessPosition endPos) {
        if (endPos.getRow() == promotionRow) {
            moves.add(new ChessMove(startPos, endPos, ChessPiece.PieceType.QUEEN));
            moves.add(new ChessMove(startPos, endPos, ChessPiece.PieceType.ROOK));
            moves.add(new ChessMove(startPos, endPos, ChessPiece.PieceType.BISHOP));
            moves.add(new ChessMove(startPos, endPos, ChessPiece.PieceType.KNIGHT));
        } else {
            moves.add(new ChessMove(startPos, endPos, null));
        }
    }
}
