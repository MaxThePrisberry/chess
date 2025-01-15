package server;

import org.eclipse.jetty.websocket.api.annotations.*;
import spark.Session;

import java.util.HashSet;
import java.util.Set;

@WebSocket
public class WSHandler {

    public Set<Session> sessions = new HashSet<>();

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

    }
}
