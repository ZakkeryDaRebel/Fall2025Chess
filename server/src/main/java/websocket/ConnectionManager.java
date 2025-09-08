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

    public void remove(int gameID, Connection connection) throws ResponseException {
        try {
            ArrayList<Connection> connectionList = connectionMap.get(gameID);
            connectionList.remove(connection);
            connectionMap.put(gameID, connectionList);
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
        ArrayList<Connection> removeList = new ArrayList<>();
        switch (messageType) {
            case ROOT: {
                sendMessage(rootClient, serverMessage);
                return;
            } case NOT_ROOT: {
                for (Connection connection : connectionList) {
                    if (connection.session().isOpen()) {
                        if (!rootClient.equals(connection.session())) {
                            sendMessage(connection.session(), serverMessage);
                        }
                    } else {
                        removeList.add(connection);
                    }
                }
                removeFromList(gameID, removeList);
                return;
            } case EVERYONE: {
                for (Connection connection : connectionList) {
                    if (connection.session().isOpen()) {
                        sendMessage(connection.session(), serverMessage);
                    } else {
                        removeList.add(connection);
                    }
                }
                removeFromList(gameID, removeList);
                return;
            }
        }
    }

    private void removeFromList(int gameID, ArrayList<Connection> removeList) {
        try {
            ArrayList<Connection> connectionList = connectionMap.get(gameID);
            for (Connection connection : removeList) {
                connectionList.remove(connection);
            }
            connectionMap.put(gameID, connectionList);
        } catch (Exception ex) {
            System.out.println("Failed to remove connection from list");
        }
    }

    private void sendMessage(Session session, ServerMessage serverMessage) throws ResponseException {
        try {
            session.getRemote().sendString(new Gson().toJson(serverMessage));
        } catch (Exception ex) {
            throw new ResponseException(ex.getMessage(), 500);
        }
    }
}
