package websocket;

import com.google.gson.Gson;
import exception.ResponseException;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    private ConcurrentHashMap<Integer, ArrayList<Connection>> connectionMap;

    public ConnectionManager() {
        connectionMap = new ConcurrentHashMap<>();
    }

    public void add(int gameID, Session session, String username) throws ResponseException {
        try {
            Connection newConnection = new Connection(session, username);
            ArrayList<Connection> connectionList = connectionMap.get(gameID);
            if (connectionList == null) {
                connectionList = new ArrayList<>();
            }
            connectionList.add(newConnection);
            connectionMap.put(gameID, connectionList);
        } catch (Exception ex) {
            throw new ResponseException(ex.getMessage(), 500);
        }
    }

    public void remove(int gameID, Session session) throws ResponseException {
        try {
            ArrayList<Connection> connectionList = connectionMap.get(gameID);
            connectionList.remove(session);
        } catch (Exception ex) {
            throw new ResponseException(ex.getMessage(), 500);
        }
    }

    public enum MessageType {
        ROOT,
        NOT_ROOT,
        EVERYONE
    }

    public void messageDelivery(MessageType messageType, int gameID, Session rootClient, ServerMessage serverMessage) throws ResponseException {
        ArrayList<Connection> connectionList = connectionMap.get(gameID);
        switch (messageType) {
            case ROOT: {
                sendMessage(rootClient, serverMessage);
                return;
            } case NOT_ROOT: {
                for (Connection connection : connectionList) {
                    if (!rootClient.equals(connection.session())) {
                        sendMessage(connection.session(), serverMessage);
                    }
                }
                return;
            } case EVERYONE: {
                for (Connection connection : connectionList) {
                    sendMessage(connection.session(), serverMessage);
                }
                return;
            }
        }
    }

    public void sendMessage(Session session, ServerMessage serverMessage) throws ResponseException {
        try {
            session.getRemote().sendString(new Gson().toJson(serverMessage));
        } catch (Exception ex) {
            throw new ResponseException(ex.getMessage(), 500);
        }
    }
}
