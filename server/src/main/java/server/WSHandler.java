package server;

import chess.ChessBoard;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@WebSocket
public class WSHandler {

    public static Set<Session> sessions = new HashSet<>();
    public static Gson gson = new Gson();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        sessions.add(session);
    }

    @OnWebSocketClose
    public void onClose(Session session, int status, String message) {
        sessions.remove(session);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case LEAVE -> {
                session.close();
            }
            case CONNECT -> {
                GameData data;
                try {
                    data = GameDAO.getGame(command.getGameID());
                } catch (DataAccessException e) {
                    throw new RuntimeException(e);
                }
                ServerMessage response = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gson.toJson(data.game().getBoard(), ChessBoard.class));
                try {
                    session.getRemote().sendString(gson.toJson(response));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
