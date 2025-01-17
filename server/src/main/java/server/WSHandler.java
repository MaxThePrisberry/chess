package server;

import chess.*;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
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

    public static Map<Session, String> sessions = new HashMap<>();
    public static Map<Integer, Map<String, Session>> gameRooms = new HashMap<>();
    public static Gson gson = new Gson();

    private static final String SERVER_ERROR_TEXT = "Server error.";
    private static final String AUTH_ERROR_TEXT = "No user registered with your session.";

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("Connection: " + session);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable throwable) {
        System.out.println("Error: " + throwable.getMessage());
    }

    @OnWebSocketClose
    public void onClose(Session session, int status, String message) {
        for (int key : gameRooms.keySet()) {
            if (gameRooms.get(key).containsValue(session)) {
                try {
                    GameData game = GameDAO.getGame(key);
                    if (game.whiteUsername() != null && game.whiteUsername().equals(sessions.get(session))) {
                        GameDAO.updateGame(key, null, game.blackUsername(), game.gameName(), game.game());
                    } else if (game.blackUsername() != null && game.blackUsername().equals(sessions.get(session))) {
                        GameDAO.updateGame(key, game.whiteUsername(), null, game.gameName(), game.game());
                    }
                } catch (DataAccessException e) {
                    sendError(session, "Error removing user from game.");
                }
                gameRooms.get(key).values().removeIf(value -> value.equals(session));
            }
        }
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
        AuthData user;
        try {
            user = AuthDAO.getAuth(command.getAuthToken());
        } catch (DataAccessException e) {
            sendError(session, AUTH_ERROR_TEXT);
            return;
        }
        GameData data = getGame(session, command.getGameID());
        if (data == null) {return;}
        switch (command.getCommandType()) {
            case LEAVE -> {
                handleLeave(session, user, data, command);
            }
            case CONNECT -> {
                handleConnect(session, command, data, user);
            }
            case RESIGN -> {
                handleResign(session, user, data, command);
            }
            case MAKE_MOVE -> {
                handleMove(session, data, user, command);
            }
        }
    }

    private void handleMove(Session session, GameData data, AuthData user, UserGameCommand command) {
        if (data.game().isIsOver()) {
            sendError(session, "Game already over. No moves can be made.");
            return;
        }
        if ((data.game().getTeamTurn().equals(ChessGame.TeamColor.WHITE) && !user.username().equals(data.whiteUsername())) ||
                (data.game().getTeamTurn().equals(ChessGame.TeamColor.BLACK) && !user.username().equals(data.blackUsername()))) {
            sendError(session, "Make moves on your own turn, buddy.");
            return;
        }
        try {
            data.game().makeMove(command.getMove());
            GameDAO.updateGame(data);
        } catch (InvalidMoveException e) {
            sendError(session, "Not a valid move, buddy.");
            return;
        }
        ServerMessage loadNewGamestate = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        loadNewGamestate.setGame(gson.toJson(data.game()));
        try {
            session.getRemote().sendString(gson.toJson(loadNewGamestate));
        } catch (IOException e) {
            sendError(session, SERVER_ERROR_TEXT);
        }
        sendOtherClients(session, command, loadNewGamestate);
        ServerMessage loadMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        loadMessage.setMessage("The move " + translateChessPosition(command.getMove().getStartPosition()) +
                " to " + translateChessPosition(command.getMove().getEndPosition()) + " was made.");
        sendOtherClients(session, command, loadMessage);
    }

    private void handleResign(Session session, AuthData user, GameData data, UserGameCommand command) {
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
                response.setMessage("Your opponent " + user.username() + " has resigned. You win!");
            } else {
                response = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
                response.setMessage("The player " + user.username() + " has resigned.");
            }
            try {
                value.getRemote().sendString(gson.toJson(response));
            } catch (IOException e) {
                sendError(session, SERVER_ERROR_TEXT);
            }
        });
    }

    private void handleConnect(Session session, UserGameCommand command, GameData data, AuthData user) {
        if (command.getColor() != null) {
            if (command.getColor().equals("WHITE") && data.whiteUsername() == null) {
                GameDAO.updateGame(data.gameID(), user.username(), data.blackUsername(), data.gameName(), data.game());
            } else if (command.getColor().equals("BLACK") && data.blackUsername() == null) {
                GameDAO.updateGame(data.gameID(), data.whiteUsername(), user.username(), data.gameName(), data.game());
            } else {
                sendError(session, "Can't join as a color not available.");
            }
        }
        sessions.put(session, user.username());
        ServerMessage response = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        response.setGame(gson.toJson(data.game()));
        try {
            session.getRemote().sendString(gson.toJson(response));
        } catch (IOException e) {
            sendError(session, SERVER_ERROR_TEXT);
        }
        if (gameRooms.containsKey(command.getGameID())) {
            gameRooms.get(command.getGameID()).put(user.username(), session);
        } else {
            gameRooms.put(command.getGameID(), new HashMap<>(Map.of(user.username(), session)));
        }
        ServerMessage tmp = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        if (command.getColor() != null) {
            String color = data.whiteUsername().equals(user.username()) ? "WHITE" : "BLACK";
            tmp.setMessage("User " + user.username() + " has joined the game as color " + color + ".");
        } else {
            tmp.setMessage("User " + user.username() + " is observing this game.");
        }
        sendOtherClients(session, command, tmp);
    }

    private void handleLeave(Session session, AuthData user, GameData data, UserGameCommand command) {
        if (user.username().equals(data.whiteUsername())) {
            GameDAO.updateGame(data.gameID(), null, data.blackUsername(), data.gameName(), data.game());
        } else if (user.username().equals(data.blackUsername())) {
            GameDAO.updateGame(data.gameID(), data.whiteUsername(), null, data.gameName(), data.game());
        }
        gameRooms.get(command.getGameID()).values().removeIf(value -> value.equals(session));
        ServerMessage tmp = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        tmp.setMessage("User " + user.username() + " has left the game.");
        sendOtherClients(session, command, tmp);
        session.close();
    }

    private String translateChessPosition(ChessPosition pos) {
        StringBuilder output = new StringBuilder();
        switch (pos.getColumn()) {
            case 1 -> {
                output.append('a');
            }
            case 2 -> {
                output.append('b');
            }
            case 3 -> {
                output.append('c');
            }
            case 4 -> {
                output.append('d');
            }
            case 5 -> {
                output.append('e');
            }
            case 6 -> {
                output.append('f');
            }
            case 7 -> {
                output.append('g');
            }
            case 8 -> {
                output.append('h');
            }
        }
        output.append(pos.getRow());
        return output.toString();
    }

    private void sendOtherClients(Session session, UserGameCommand command, ServerMessage tmp) {
        String notification = gson.toJson(tmp);
        gameRooms.get(command.getGameID()).forEach((key, value) -> {
            if (!value.equals(session) && value.isOpen()) {
                try {
                    value.getRemote().sendString(notification);
                } catch (IOException e) {
                    sendError(session, SERVER_ERROR_TEXT);
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
            sendError(session, SERVER_ERROR_TEXT);
        }
    }
}
