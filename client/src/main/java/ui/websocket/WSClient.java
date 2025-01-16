package ui.websocket;

import chess.ChessBoard;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import ui.GameplayUI;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerError;
import websocket.messages.ServerMessage;
import websocket.messages.ServerNotification;

import java.io.IOException;
import java.net.URI;

import static ui.Variables.*;

public class WSClient implements WebSocketListener {

    public org.eclipse.jetty.websocket.api.Session session;
    public String color;
    public int gameID;

    public WSClient(int gameID, String color) throws Exception {
        this.gameID = gameID;
        this.color = color;
        WebSocketClient client = new WebSocketClient();
        client.start();

        session = client.connect(this, new URI("ws://localhost:8080/ws")).get();
        session.getRemote().sendString(gson.toJson(new UserGameCommand(
                UserGameCommand.CommandType.CONNECT, authToken, gameID)));
    }

    public void send(UserGameCommand.CommandType commandType) throws IOException {
        UserGameCommand command = new UserGameCommand(commandType, authToken, gameID);
        session.getRemote().sendString(gson.toJson(command));
    }

    @Override
    public void onWebSocketBinary(byte[] bytes, int i, int i1) {

    }

    @Override
    public void onWebSocketText(String s) {
        ServerMessage message = gson.fromJson(s, ServerMessage.class);
        switch (message.getServerMessageType()) {
            case LOAD_GAME -> {
                GameplayUI.currentBoard = gson.fromJson(message.getContent(), ChessBoard.class);
                GameplayUI.redrawBoardComplete();
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

    @Override
    public void onWebSocketClose(int i, String s) {

    }

    @Override
    public void onWebSocketConnect(org.eclipse.jetty.websocket.api.Session session) {

    }

    @Override
    public void onWebSocketError(Throwable throwable) {

    }
}
