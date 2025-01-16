package ui.websocket;

import chess.ChessBoard;
import ui.GameplayUI;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerError;
import websocket.messages.ServerMessage;
import websocket.messages.ServerNotification;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static ui.Variables.*;

public class WSClient extends Endpoint {

    public Session session;
    public int gameID;

    public WSClient(int gameID) throws URISyntaxException, DeploymentException, IOException {
        this.gameID = gameID;
        URI uri = new URI(SERVER_LOCATION);
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        session = container.connectToServer(this, uri);
        session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String s) {
                ServerMessage message = gson.fromJson(s, ServerMessage.class);
                switch (message.getServerMessageType()) {
                    case LOAD_GAME -> {
                        GameplayUI.currentBoard = gson.fromJson(message.getContent(), ChessBoard.class);
                        GameplayUI.redrawBoard();
                    }
                    case ERROR -> {
                        GameplayUI.displayErrorNotification("Error: " + gson.fromJson(message.getContent(),
                                ServerError.class).errorMessage());
                    }
                    case NOTIFICATION -> {
                        GameplayUI.displayNotification(gson.fromJson(message.getContent(),
                                ServerNotification.class).notification());
                    }
                }
            }
        });
        session.getBasicRemote().sendText(gson.toJson(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID)));
    }

    public void send(UserGameCommand.CommandType commandType) throws IOException {
        UserGameCommand command = new UserGameCommand(commandType, authToken, gameID);
        session.getBasicRemote().sendText(gson.toJson(command));
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {}
}
