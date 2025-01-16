package server;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import spark.*;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException e) {
            throw new RuntimeException("Error connecting to database on Server startup: " + e.getMessage());
        }

        ChessHandler handler = new ChessHandler();

        //Handle websocket interaction
        Spark.webSocket("/ws", WSHandler.class);

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", handler::clear);
        Spark.post("/user", handler::register);
        Spark.post("/session", handler::login);
        Spark.delete("/session", handler::logout);
        Spark.get("/game", handler::listGames);
        Spark.post("/game", handler::createGame);
        Spark.put("/game", handler::joinGame);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
