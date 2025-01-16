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

    private static final String serverErrorText = "Server error.";
    private static final String authErrorText = "No user registered with your session.";

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case LEAVE -> {
                AuthData user = null;
                try {
                    user = AuthDAO.getAuth(command.getAuthToken());
                } catch (DataAccessException e) {
                    sendError(session, authErrorText);
                    return;
                }
                gameRooms.get(command.getGameID()).values().removeIf(value -> value.equals(session));
                ServerMessage tmp = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
                tmp.setMessage("User " + user.username() + " has left the game.");
                sendOtherClients(session, command, tmp);
                session.close();
            }
            case CONNECT -> {
                AuthData user = null;
                try {
                    user = AuthDAO.getAuth(command.getAuthToken());
                } catch (DataAccessException e) {
                    sendError(session, authErrorText);
                    return;
                }
                sessions.put(user.username(), session);
                GameData data = getGame(session, command.getGameID());
                if (data == null) {return;}
                ServerMessage response = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
                response.setGame(gson.toJson(data.game().getBoard(), ChessBoard.class));
                try {
                    session.getRemote().sendString(gson.toJson(response));
                } catch (IOException e) {
                    sendError(session, serverErrorText);
                }
                if (gameRooms.containsKey(command.getGameID())) {
                    gameRooms.get(command.getGameID()).put(user.username(), session);
                } else {
                    gameRooms.put(command.getGameID(), new HashMap<>(Map.of(user.username(), session)));
                }
                ServerMessage tmp = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
                String color = data.whiteUsername().equals(user.username()) ? "WHITE" : "BLACK";
                tmp.setMessage("User " + user.username() + " has joined the game as color " + color + ".");
                sendOtherClients(session, command, tmp);
            }
            case RESIGN -> {
                GameData data = getGame(session, command.getGameID());
                AuthData user = null;
                try {
                    user = AuthDAO.getAuth(command.getAuthToken());
                } catch (DataAccessException e) {
                    sendError(session, authErrorText);
                }
                String otherUsername;
                if (user.username().equals(data.whiteUsername())) {
                    otherUsername = data.blackUsername();
                } else if (user.username().equals(data.blackUsername())) {
                    otherUsername = data.whiteUsername();
                } else {
                    sendError(session, "Cannot resign as observer.");
                    return;
                }
                if (data.game().isIsOver()) {
                    sendError(session, "Game already over.");
                    return;
                } else {
                    data.game().setIsOver(true);
                    GameDAO.updateGame(data);
                }
                gameRooms.get(command.getGameID()).forEach((key, value) -> {
                    ServerMessage response;
                    if (key.equals(otherUsername)) {
                        response = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
                        response.setMessage("Your opponent " + otherUsername + " has resigned. You win!");
                    } else {
                        response = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
                        response.setMessage("The player " + otherUsername + " has resigned.");
                    }
                    try {
                        value.getRemote().sendString(gson.toJson(response));
                    } catch (IOException e) {
                        sendError(session, serverErrorText);
                    }
                });
                gameRooms.get(command.getGameID()).values().removeIf(value -> value.equals(session));
            }
            case MAKE_MOVE -> {

            }
        }
    }

    private void sendOtherClients(Session session, UserGameCommand command, ServerMessage tmp) {
        String notification = gson.toJson(tmp);
        gameRooms.get(command.getGameID()).forEach((key, value) -> {
            if (!value.equals(session) && value.isOpen()) {
                try {
                    value.getRemote().sendString(notification);
                } catch (IOException e) {
                    sendError(session, serverErrorText);
                }
            }
        });
    }

    private GameData getGame(Session session, int gameID) {
        try {
            return GameDAO.getGame(gameID);
        } catch (DataAccessException e) {
            sendError(session, "No game with given gameID.");
            return null;
        }
    }

    private void sendError(Session session, String errorMessage) {
        ServerMessage message = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
        message.setErrorMessage(errorMessage);
        try {
            session.getRemote().sendString(gson.toJson(message));
        } catch (IOException e) {
            sendError(session, serverErrorText);
        }
    }
}
