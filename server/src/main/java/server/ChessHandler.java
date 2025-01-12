package server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dataaccess.HandlerTargetedException;
import service.Phase3MasterService;
import service.model.LoginRequest;
import service.model.RegisterRequest;
import service.model.UserDataResult;
import spark.Request;
import spark.Response;

import java.util.Map;

public class ChessHandler {
    private final Gson gson = new Gson();

    private final Phase3MasterService service = new Phase3MasterService();

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
        String message;
        Map<String, String> request = gson.fromJson(req.body(), new TypeToken<Map<String, String>>(){}.getType());
        if (request.get("username") == null || request.get("username").isBlank() ||
                request.get("password") == null || request.get("password").isBlank() ||
                request.get("email") == null || request.get("email").isBlank()) {
            res.status(400);
            message = "Error: bad request";
            return gson.toJson(message);
        }
        try {
            UserDataResult user = service.register(new RegisterRequest(request.get("username"), request.get("password"), request.get("email")));
            return gson.toJson(user);
        } catch (HandlerTargetedException e) {
            res.status(e.getErrorNumber());
            message = e.getMessage();
            return gson.toJson(message);
        } catch (Exception e) {
            res.status(500);
            message = "Error: " + e.getMessage();
            return gson.toJson(message);
        }
    }

    public String login(Request req, Response res) {
        String message;
        Map<String, String> request = gson.fromJson(req.body(), new TypeToken<Map<String, String>>(){}.getType());
        if (request.get("username") == null || request.get("username").isBlank() ||
                request.get("password") == null || request.get("password").isBlank()) {
            res.status(400);
            message = "Error: bad request";
            return gson.toJson(message);
        }
        try {
            UserDataResult user = service.login(new LoginRequest(request.get("username"), request.get("password")));
            return gson.toJson(user);
        } catch (HandlerTargetedException e) {
            res.status(e.getErrorNumber());
            message = e.getMessage();
            return gson.toJson(message);
        } catch (Exception e) {
            res.status(500);
            message = "Error: " + e.getMessage();
            return gson.toJson(message);
        }
    }

    public String logout(Request req, Response res) {

    }

    public String listGames(Request req, Response res) {

    }

    public String createGame(Request req, Response res) {

    }

    public String joinGame(Request req, Response res) {

    }
}
