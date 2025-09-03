package chess;

import java.util.Objects;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {

    private ChessPosition startPos;
    private ChessPosition endPos;
    private ChessPiece.PieceType promotion;

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     ChessPiece.PieceType promotionPiece) {
        startPos = startPosition;
        endPos = endPosition;
        promotion = promotionPiece;
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return startPos;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return endPos;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return promotion;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessMove chessMove = (ChessMove) o;
        return Objects.equals(startPos, chessMove.startPos) && Objects.equals(endPos, chessMove.endPos) && promotion == chessMove.promotion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startPos, endPos, promotion);
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

    public String getColString(int col) {
        switch (col) {
            case 1: return "a";
            case 2: return "b";
            case 3: return "c";
            case 4: return "d";
            case 5: return "e";
            case 6: return "f";
            case 7: return "g";
            case 8: return "h";
            default: return "";
        }
    }
}
