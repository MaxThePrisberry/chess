package server;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.UserGameCommand;

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
    public void onClose(Session session) {
        sessions.remove(session);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case LEAVE -> {
                session.close();
            }
        }
    }
}
