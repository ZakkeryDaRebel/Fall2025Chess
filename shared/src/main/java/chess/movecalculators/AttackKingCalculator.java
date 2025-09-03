package chess.movecalculators;

import chess.*;

import java.util.Collection;

public class AttackKingCalculator {

    public boolean canAttackKing(ChessBoard board, ChessGame.TeamColor teamColor, ChessPosition kingPos) {
        for (int row = 1; row < 9; row++) {
            for (int col = 1; col < 9; col++) {
                ChessPosition testPos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(testPos);
                if (piece!=null && piece.getTeamColor()!=teamColor && pieceMeetsKing(board, piece, testPos, kingPos)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean pieceMeetsKing(ChessBoard board, ChessPiece piece, ChessPosition testPos, ChessPosition kingPos) {
        Collection<ChessMove> pieceMoves = piece.pieceMoves(board, testPos);
        for (ChessMove attack : pieceMoves) {
            if (attack.getEndPosition().equals(kingPos)) {
                return true;
            }
        }
        return false;
    }
}
