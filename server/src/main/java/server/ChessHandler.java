package server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dataaccess.HandlerTargetedException;
import service.MasterService;
import service.model.*;
import spark.Request;
import spark.Response;

import java.util.Map;

public class ChessHandler {
    private final Gson gson = new Gson();

    private final MasterService service = new MasterService();

    public String clear(Request req, Response res) {
        try {
            service.clear();
        } catch (Exception e) {
            res.status(500);
            String message = "Error: " + e.getMessage();
            return gson.toJson(message);
        }
        return "{}";
    }

    public String register(Request req, Response res) {
        Map<String, String> request = gson.fromJson(req.body(), new TypeToken<Map<String, String>>(){}.getType());
        if (request.get("username") == null || request.get("username").isBlank() ||
                request.get("password") == null || request.get("password").isBlank() ||
                request.get("email") == null || request.get("email").isBlank()) {
            res.status(400);
            return gson.toJson(Map.of("message", "Error: bad request"));
        }
        try {
            UserDataResult user = service.register(new RegisterRequest(request.get("username"), request.get("password"), request.get("email")));
            return gson.toJson(user);
        } catch (HandlerTargetedException e) {
            res.status(e.getErrorNumber());
            return gson.toJson(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    public String login(Request req, Response res) {
        Map<String, String> request = gson.fromJson(req.body(), new TypeToken<Map<String, String>>(){}.getType());
        if (request.get("username") == null || request.get("username").isBlank() ||
                request.get("password") == null || request.get("password").isBlank()) {
            res.status(400);
            return gson.toJson(Map.of("message", "Error: bad request"));
        }
        try {
            UserDataResult user = service.login(new LoginRequest(request.get("username"), request.get("password")));
            return gson.toJson(user);
        } catch (HandlerTargetedException e) {
            res.status(e.getErrorNumber());
            return gson.toJson(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    public String logout(Request req, Response res) {
        String authToken = req.headers("Authorization");
        if (authToken == null || authToken.isBlank()) {
            res.status(400);
            return gson.toJson(Map.of("message", "Error: bad request"));
        }
        try {
            service.logout(new LogoutRequest(authToken));
            return "{}";
        } catch (HandlerTargetedException e) {
            res.status(e.getErrorNumber());
            return gson.toJson(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    public String listGames(Request req, Response res) {
        String authToken = req.headers("authorization");
        if (authToken == null || authToken.isBlank()) {
            res.status(400);
            return gson.toJson(Map.of("message", "Error: bad request"));
        }
        try {
            ListGamesResult games = service.listGames(new ListGamesRequest(authToken));
            return gson.toJson(games);
        } catch (HandlerTargetedException e) {
            res.status(e.getErrorNumber());
            return gson.toJson(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    public String createGame(Request req, Response res) {
        String authToken = req.headers("authorization");
        Map<String, String> request = gson.fromJson(req.body(), new TypeToken<Map<String, String>>(){}.getType());
        if (authToken == null || authToken.isBlank() ||
                request.get("gameName") == null || request.get("gameName").isBlank()) {
            res.status(400);
            return gson.toJson(Map.of("message", "Error: bad request"));
        }
        try {
            CreateGameResult serviceResult = service.createGame(new CreateGameRequest(authToken, request.get("gameName")));
            return gson.toJson(serviceResult);
        } catch (HandlerTargetedException e) {
            res.status(e.getErrorNumber());
            return gson.toJson(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    public String joinGame(Request req, Response res) {
        String authToken = req.headers("authorization");
        Map<String, String> request = gson.fromJson(req.body(), new TypeToken<Map<String, String>>(){}.getType());
        if ((authToken == null || authToken.isBlank() ||
                request.get("playerColor") == null || request.get("playerColor").isBlank() ||
                request.get("gameID") == null || request.get("gameID").isBlank()) ||
                !(request.get("playerColor").equals("WHITE") || request.get("playerColor").equals("BLACK"))) {
            res.status(400);
            return gson.toJson(Map.of("message", "Error: bad request"));
        }
        try {
            service.joinGame(new JoinGameRequest(authToken, request.get("playerColor"), Integer.parseInt(request.get("gameID"))));
            return "{}";
        } catch (HandlerTargetedException e) {
            res.status(e.getErrorNumber());
            return gson.toJson(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}
