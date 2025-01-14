package ui;

import model.GameData;
import ui.model.UIData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ui.Variables.authToken;
import static ui.Variables.gson;
import static ui.EscapeSequences.*;

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

    private static boolean isJustANumber(String number) {
        for (char c : number.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    public static UIData join(String gameID, String playerColor) {
        if (gameID == null || gameID.isBlank() || !isJustANumber(gameID) || playerColor == null || playerColor.isBlank() ||
                (!playerColor.equals("WHITE") && !playerColor.equals("BLACK"))) {
            throw new UIException(true, "User inputs are in the wrong format or empty.");
        }

        Map<String, String> jsonMap = Map.of("gameID", gameID, "playerColor", playerColor);
        String data = gson.toJson(jsonMap);

        sendServer("/game", "PUT", data);

        return new UIData(UIType.LOGIN, "You have joined game " + gameID + " as player " + playerColor + ".\n" +
                printChessBoard(playerColor));
    }

    public static UIData list() {
        if (authToken == null || authToken.isBlank()) {
            throw new UIException(false, "authToken is blank when it should have been something.");
        }

        Map<String, List<Map<String, Object>>> response = sendServer("/game", "GET", null);
        List<Map<String, Object>> games = response.get("games");

        if (games == null) {
            throw new UIException(false, "List games failed: Response from server blank.");
        }

        games.sort((one, two) -> {
            Double gameID1 = (Double) one.get("gameID");
            Double gameID2 = (Double) two.get("gameID");
            return gameID1.compareTo(gameID2);
        });

        StringBuilder output = new StringBuilder();
        for (Map<String, Object> game : games) {
            output.append(((Double)game.get("gameID")).intValue()).append(" | ").append(game.get("gameName")).append('\n');
        }
        return new UIData(UIType.LOGIN, "Games:\n" + output.toString());
    }
}
