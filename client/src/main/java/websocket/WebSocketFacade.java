package websocket;

import com.google.gson.Gson;
import facade.ServerMessageObserver;
import exception.ResponseException;
import jakarta.websocket.*;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

import java.net.URI;

public class WebSocketFacade extends Endpoint {
    private Session session;
    private ServerMessageObserver messageObserver;

    public WebSocketFacade(String url, ServerMessageObserver serverMessageObserver) {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            messageObserver = serverMessageObserver;

            WebSocketContainer wsContainer = ContainerProvider.getWebSocketContainer();
            this.session = wsContainer.connectToServer(this, socketURI);

            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);
                    messageObserver.notify(serverMessage.getServerMessageType(), message);
                }
            });
        } catch (Exception ex) {

        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {}

    public void sendToServer(UserGameCommand command) throws ResponseException {
        try {
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (Exception ex) {
            throw new ResponseException("Failed to execute the In Game Command", 0);
        }
    }
}
