import chess.*;
import ui.ClientREPL;
import ui.EscapeSequences;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client:" + EscapeSequences.BLACK_PAWN);

        String serverURL = "http://localhost:8888";
        try {
            ClientREPL repl = new ClientREPL(serverURL);
            repl.run();
        } catch (Exception ex) {
            System.out.println("CGI Crashed: " + ex.getMessage());
        }
    }
}
