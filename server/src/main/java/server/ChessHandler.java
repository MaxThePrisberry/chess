package server;

import com.google.gson.Gson;
import service.Phase3MasterService;
import spark.Request;
import spark.Response;

public class ChessHandler {
    private final Gson gson = new Gson();

    private Phase3MasterService service = new Phase3MasterService();

    public String clear(Request req, Response res) {
        try {
            service.clear();
        } catch (Exception e) {
            String message = "Error: " + e.getMessage();
            return gson.toJson(message);
        }
        return "{}";
    }

    public String register(Request req, Response res) {

    }

    public String login(Request req, Response res) {

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
