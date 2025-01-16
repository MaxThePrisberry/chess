package ui;

import ui.model.UIData;
import ui.websocket.WSClient;

import javax.websocket.DeploymentException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ui.Variables.*;

public class LoginUI extends ServerFacade {

    public static Map<Integer, Integer> gameIDMap = new HashMap<>();

    public static UIData help() {
        String output = """
                logout - end session and return to the login interface
                create - make a new chess game to play
                list - list all live chess games
                join - join a live chess game
                observe - observe a live chess game
                quit - exit this chess program
                help - see available commands""";
        return new UIData(UIType.LOGIN, output);
    }

    public static UIData logout() throws UIException {
        if (authToken == null || authToken.isBlank()) {
            throw new UIException(false, "authToken is blank when it should have been something.");
        }

        sendServer("/session", "DELETE", null);
        authToken = null;

        return new UIData(UIType.PRELOGIN, "You have logged out.");
    }

    public static UIData create(String gameName) throws UIException {
        if (gameName == null || gameName.isBlank()) {
            throw new UIException(false, "gameName is blank when it should have been something.");
        }

        Map<String, String> jsonMap = Map.of("gameName", gameName);
        String data = gson.toJson(jsonMap);

        Map<String, Double> response = sendServer("/game", "POST", data);

        if (response.get("gameID") != null) {
            return new UIData(UIType.LOGIN, "New game created with ID " + response.get("gameID").intValue());
        } else {
            throw new UIException(false, "Create Game Failed: Response from server blank.");
        }
    }

    private static boolean isNotJustANumber(String number) {
        for (char c : number.toCharArray()) {
            if (!Character.isDigit(c)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isValidGameID(String gameID) {
        getGames();
        return gameIDMap.containsKey(Integer.parseInt(gameID));
    }

    public static UIData join(String gameID, String playerColor) {
        if (gameID == null || gameID.isBlank() || isNotJustANumber(gameID) || playerColor == null || playerColor.isBlank() ||
                (!playerColor.equals("WHITE") && !playerColor.equals("BLACK"))) {
            throw new UIException(true, "User inputs are in the wrong format or empty.");
        }
        if (!isValidGameID(gameID)) {
            throw new UIException(true, "Given gameID does not exist.");
        }

        Map<String, String> jsonMap = Map.of("gameID", gameIDMap.get(Integer.parseInt(gameID)).toString(), "playerColor", playerColor);
        String data = gson.toJson(jsonMap);

        sendServer("/game", "PUT", data);

        try {
            transitionToGameplayUI(Integer.parseInt(gameID));
        } catch (DeploymentException | URISyntaxException | IOException e) {
            throw new UIException(false, "WebSocket failed to setup correctly.");
        }
        return new UIData(UIType.GAMEPLAY, "You have joined game " + gameID + " as player " + playerColor + ".\n");
    }

    private static List<Map<String, Object>> getGames() {
        Map<String, List<Map<String, Object>>> response = sendServer("/game", "GET", null);
        List<Map<String, Object>> games = response.get("games");

        if (games == null) {
            throw new UIException(false, "List games failed: Response from server null.");
        }

        games.sort((one, two) -> {
            Double gameID1 = (Double) one.get("gameID");
            Double gameID2 = (Double) two.get("gameID");
            return gameID1.compareTo(gameID2);
        });

        gameIDMap.clear();
        for (int i = 1; i <= games.size(); i++) {
            gameIDMap.put(i, ((Double) games.get(i-1).get("gameID")).intValue());
        }

        return games;
    }

    public static UIData list() {
        if (authToken == null || authToken.isBlank()) {
            throw new UIException(false, "authToken is blank when it should have been something.");
        }

        List<Map<String, Object>> games = getGames();

        StringBuilder output = new StringBuilder();
        for (int i = 1; i <= games.size(); i++) {
            output.append(i).append(" | ").append(games.get(i-1).get("gameName")).append('\n');
        }
        return new UIData(UIType.LOGIN, "Games:\n" + output.toString());
    }

    public static UIData observe(String gameID) {
        if (gameID == null || gameID.isBlank() || isNotJustANumber(gameID)) {
            throw new UIException(true, "User inputs are in the wrong format or empty.");
        }
        if (isValidGameID(gameID)) {
            try {
                transitionToGameplayUI(Integer.parseInt(gameID));
            } catch (DeploymentException | URISyntaxException | IOException e) {
                throw new UIException(false, "WebSocket failed to setup correctly.");
            }
            return new UIData(UIType.GAMEPLAY, "You are now observing game " + gameID);
        }
        throw new UIException(true, "No game associated with given gameID.");
    }

    private static void transitionToGameplayUI(int gameID) throws DeploymentException, URISyntaxException, IOException {
        GameplayUI.wsClient = new WSClient(gameID);
    }
}
