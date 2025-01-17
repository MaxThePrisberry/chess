package ui.websocket;

import chess.ChessGame;
import chess.ChessMove;
import ui.GameplayUI;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

import static ui.Variables.*;

public class WSClient extends Endpoint {

    public Session session;
    public String color;
    public int gameID;

    public WSClient(int gameID, String color) throws Exception {
        this.gameID = gameID;
        this.color = color;
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, new URI("ws://localhost:8080/ws"));

        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String s) {
                try {
                    ServerMessage message = gson.fromJson(s, ServerMessage.class);
                    switch (message.getServerMessageType()) {
                        case LOAD_GAME -> {
                            GameplayUI.currentGame = gson.fromJson(message.getGame(), ChessGame.class);
                            GameplayUI.redrawBoardComplete();
                        }
                        case ERROR -> {
                            GameplayUI.displayErrorNotification("Error: " + message.getErrorMessage());
                        }
                        case NOTIFICATION -> {
                            GameplayUI.displayNotification(message.getMessage());
                        }
                    }
                } catch (Exception e) {
                    System.out.println("There was a client websocket error: " + e.getMessage());
                }
            }
        });
    }

    public void send(UserGameCommand.CommandType commandType) throws IOException {
        UserGameCommand command = new UserGameCommand(commandType, authToken, gameID);
        session.getBasicRemote().sendText(gson.toJson(command));
    }

    public void send(UserGameCommand.CommandType commandType, ChessMove move) throws IOException {
        UserGameCommand command = new UserGameCommand(commandType, authToken, gameID);
        command.setMove(move);
        session.getBasicRemote().sendText(gson.toJson(command));
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        try {
            send(UserGameCommand.CommandType.CONNECT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
