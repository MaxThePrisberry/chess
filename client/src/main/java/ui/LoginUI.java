package ui;

import ui.model.UIData;

import java.util.Map;

import static ui.Variables.authToken;
import static ui.Variables.gson;

public class LoginUI extends UserUI {
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
}
