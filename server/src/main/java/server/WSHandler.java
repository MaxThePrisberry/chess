package server;

import chess.ChessBoard;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.*;

@WebSocket
public class WSHandler {

    public static Map<String, Session> sessions = new HashMap<>();
    public static Map<Integer, Map<String, Session>> gameRooms = new HashMap<>();
    public static Gson gson = new Gson();

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case LEAVE -> {
                session.close();
            }
            case CONNECT -> {
                AuthData user = null;
                try {
                    user = AuthDAO.getAuth(command.getAuthToken());
                } catch (DataAccessException e) {
                    throw new RuntimeException(e);
                }
                sessions.put(user.username(), session);
                if (gameRooms.containsKey(command.getGameID())) {
                    gameRooms.get(command.getGameID()).put(user.username(), session);
                } else {
                    gameRooms.put(command.getGameID(), Map.of(user.username(), session));
                }
                GameData data = getGame(command.getGameID());
                ServerMessage response = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gson.toJson(data.game().getBoard(), ChessBoard.class));
                try {
                    session.getRemote().sendString(gson.toJson(response));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            case RESIGN -> {
                GameData data = getGame(command.getGameID());
                AuthData user;
                try {
                    user = AuthDAO.getAuth(command.getAuthToken());
                } catch (DataAccessException e) {
                    throw new RuntimeException(e);
                }
                String otherUsername;
                if (user.username().equals(data.whiteUsername())) {
                    otherUsername = data.blackUsername();
                } else if (user.username().equals(data.blackUsername())) {
                    otherUsername = data.whiteUsername();
                } else {
                    otherUsername = null;
                }
                gameRooms.get(command.getGameID()).forEach((key, value) -> {
                    ServerMessage response;
                    if (key.equals(otherUsername)) {
                        response = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                                "Your opponent " + otherUsername + " has resigned. You win!");
                    } else {
                        response = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                                "The player " + otherUsername + " has resigned.");
                    }
                    try {
                        value.getRemote().sendString(gson.toJson(response));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            case MAKE_MOVE -> {

            }
        }
    }

    private GameData getGame(int gameID) {
        try {
            return GameDAO.getGame(gameID);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
