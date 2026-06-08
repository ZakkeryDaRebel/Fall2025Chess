package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import exception.ResponseException;
import io.javalin.websocket.*;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.jetbrains.annotations.NotNull;
import websocket.commands.*;
import websocket.messages.*;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private ConnectionManager connectionManager;

    public WebSocketHandler(AuthDAO authDAO, GameDAO gameDAO, ConnectionManager connectionManager) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
        this.connectionManager = connectionManager;
    }

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext ctx) {
        try {
            UserGameCommand userGameCommand = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            if (userGameCommand.getAuthToken() == null || authDAO.getAuth(userGameCommand.getAuthToken()) == null) {
                throw new Exception("Unauthorized");
            }
            if (userGameCommand.getGameID() == null || gameDAO.getGame(userGameCommand.getGameID()) == null) {
                throw new Exception("Invalid Game");
            }
            AuthData auth = authDAO.getAuth(userGameCommand.getAuthToken());
            GameData game = gameDAO.getGame(userGameCommand.getGameID());
            switch (userGameCommand.getCommandType()) {
                case CONNECT: {
                    handleConnectCommand(auth, game, ctx.session, userGameCommand);
                    return;
                } case LEAVE: {
                    handleLeaveCommand(auth, game, ctx.session, userGameCommand);
                    return;
                } case MAKE_MOVE: {
                    handleMakeMoveCommand(auth, game, ctx.session, new Gson().fromJson(ctx.message(), MakeMoveCommand.class));
                    return;
                } case RESIGN: {
                    handleResignCommand(auth, game, ctx.session, userGameCommand);
                    return;
                }
            }
        } catch (Exception ex) {
            System.out.println("Handle Message Error: " + ex.getMessage());
            ErrorMessage newError = new ErrorMessage(ex.getMessage());
            try {
                connectionManager.messageDelivery(ConnectionManager.MessageType.ROOT, 1, ctx.session, newError);
            } catch (Exception mex) {
                System.out.println("Failed to send Error message to client");
            }
        }
    }

    public void handleConnectCommand(AuthData auth, GameData game, Session session, UserGameCommand connectCommand) throws ResponseException {
        connectionManager.add(connectCommand.getGameID(), session, auth.username());
        String message = auth.username() + " has joined the game as ";
        if (auth.username().equals(game.whiteUsername())) {
            message += "the White player";
        } else if (auth.username().equals(game.blackUsername())) {
            message += "the Black player";
        } else {
            message += "an Observer";
        }

        LoadGameMessage loadMessage = new LoadGameMessage(game);
        NotificationMessage notification = new NotificationMessage(message);
        connectionManager.messageDelivery(ConnectionManager.MessageType.ROOT, 1, session, loadMessage);
        connectionManager.messageDelivery(ConnectionManager.MessageType.NOT_ROOT, connectCommand.getGameID(), session, notification);
    }

    public void handleLeaveCommand(AuthData auth, GameData game, Session session, UserGameCommand leaveCommand) throws ResponseException {
        connectionManager.remove(leaveCommand.getGameID(), new Connection(session, auth.username()));
        try {
            if (auth.username().equals(game.whiteUsername())) {
                gameDAO.updateGame(new GameData(game.gameID(), null, game.blackUsername(), game.gameName(), game.game()));
            } else if (auth.username().equals(game.blackUsername())) {
                gameDAO.updateGame(new GameData(game.gameID(), game.whiteUsername(), null, game.gameName(), game.game()));
            }
        } catch (DataAccessException ex) {
            throw new ResponseException(ex.getMessage(), 0);
        }
        NotificationMessage notification = new NotificationMessage(auth.username() + " has left the game");
        connectionManager.messageDelivery(ConnectionManager.MessageType.NOT_ROOT, game.gameID(), session, notification);
    }

    public void handleMakeMoveCommand(AuthData auth, GameData game, Session session, MakeMoveCommand makeMoveCommand) throws ResponseException {
        ChessGame chessGame = game.game();
        ChessMove userMove = makeMoveCommand.getMove();
        if (chessGame.isGameOver()) {
            throw new ResponseException("The game is over, you can't make any more moves", 0);
        }
        if (chessGame.getBoard().getPiece(userMove.getStartPosition()) == null) {
            throw new ResponseException("There is no piece at that position", 0);
        }
        if (!chessGame.validMoves(userMove.getStartPosition()).contains(userMove)) {
            throw new ResponseException("Invalid Move", 0);
        }
        ChessGame.TeamColor playerColor = null;
        ChessGame.TeamColor opponentColor = null;
        String opponentName = "";
        if (auth.username().equals(game.whiteUsername())) {
            playerColor = ChessGame.TeamColor.WHITE;
            opponentColor = ChessGame.TeamColor.BLACK;
            if (game.blackUsername() != null) {
                opponentName = game.blackUsername();
            } else {
                opponentName = "the black player";
            }
        } else if (auth.username().equals(game.blackUsername())) {
            playerColor = ChessGame.TeamColor.BLACK;
            opponentColor = ChessGame.TeamColor.WHITE;
            if (game.whiteUsername() != null) {
                opponentName = game.whiteUsername();
            } else {
                opponentName = "the white player";
            }
        } else {
            throw new ResponseException("Observers can't make a move", 0);
        }
        if (playerColor != chessGame.getTeamTurn()) {
            throw new ResponseException("Not your turn", 0);
        }
        GameData updatedGame;
        try {
            chessGame.makeMove(userMove);
            updatedGame = new GameData(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName(), chessGame);
            gameDAO.updateGame(updatedGame);
        } catch (InvalidMoveException ex) {
            throw new ResponseException("Invalid Move", 0);
        } catch (DataAccessException ex) {
            throw new ResponseException("Failed to update the Database with the new move", 0);
        }

        LoadGameMessage loadGame = new LoadGameMessage(updatedGame);
        NotificationMessage moveMessage = new NotificationMessage(compileMoveMessage(auth.username(), userMove));
        NotificationMessage status = checkGameStatus(chessGame, opponentColor, auth.username(), opponentName);

        connectionManager.messageDelivery(ConnectionManager.MessageType.EVERYONE, updatedGame.gameID(), session, loadGame);
        connectionManager.messageDelivery(ConnectionManager.MessageType.NOT_ROOT, updatedGame.gameID(), session, moveMessage);
        if (status != null) {
            connectionManager.messageDelivery(ConnectionManager.MessageType.EVERYONE, updatedGame.gameID(), session, status);
        }
    }

    public String compileMoveMessage(String playerName, ChessMove userMove) {
        String moveString = playerName + " has made the move ";
        moveString += getColString(userMove.getStartPosition().getColumn());
        moveString += userMove.getStartPosition().getRow();
        moveString += " to ";
        moveString += getColString(userMove.getEndPosition().getColumn());
        moveString += userMove.getEndPosition().getRow();
        return moveString;
    }

    public NotificationMessage checkGameStatus(ChessGame chessGame, ChessGame.TeamColor opponentColor,
                                               String playerName, String opponentName) {
        if (chessGame.isInCheckmate(opponentColor)) {
            return new NotificationMessage(playerName + "'s move delivers checkmate to " + opponentName
                    + ", winning them the game!");
        } else if (chessGame.isInStalemate(opponentColor)) {
            return new NotificationMessage(playerName + "'s move puts " + opponentName
                    + " into stalemate, making the game result in a tie.");
        } else if (chessGame.isInCheck(opponentColor)) {
            return new NotificationMessage(playerName + "'s move puts " + opponentName
                    + " into check. What's their next move going to be?");
        } else {
            return null;
        }
    }

    public String getColString(int col) {
        return switch (col) {
            case 1 -> "a";
            case 2 -> "b";
            case 3 -> "c";
            case 4 -> "d";
            case 5 -> "e";
            case 6 -> "f";
            case 7 -> "g";
            case 8 -> "h";
            default -> "?";
        };
    }

    public void handleResignCommand(AuthData auth, GameData game, Session session, UserGameCommand resignCommand) {
        ChessGame chessGame = game.game();
        if (chessGame.isGameOver()) {
            sendError("Error: The game is already over", resignCommand.getGameID(), session);
            return;
        }
        if (!auth.username().equals(game.whiteUsername()) && !auth.username().equals(game.blackUsername())) {
            sendError("Error: Observers can't resign", resignCommand.getGameID(), session);
            return;
        }
        chessGame.setIsGameOver(true);
        try {
            gameDAO.updateGame(new GameData(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName(), chessGame));
            NotificationMessage resignMessage = new NotificationMessage(auth.username() + " has resigned the game");
            connectionManager.messageDelivery(ConnectionManager.MessageType.EVERYONE, resignCommand.getGameID(), session, resignMessage);
        } catch (Exception ex) {
            System.out.println("HandleResignCommand Error: " + ex.getMessage());
        }
    }

    public void sendError(String message, int gameID, Session session) {
        ErrorMessage errorMessage = new ErrorMessage(message);
        try {
            connectionManager.messageDelivery(ConnectionManager.MessageType.ROOT, gameID, session, errorMessage);
        } catch (Exception ex) {
            System.out.println("Failed to send message (Error: " + message + ")");
        }
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }
}
