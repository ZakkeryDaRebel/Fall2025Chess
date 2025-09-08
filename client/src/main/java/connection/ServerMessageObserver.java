package connection;

import websocket.messages.ServerMessage;

public interface ServerMessageObserver {
    public void notify(ServerMessage.ServerMessageType type, String message);
}
