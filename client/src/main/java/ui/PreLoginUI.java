package ui;

import ui.model.UIData;

import java.util.Map;

import static ui.Variables.authToken;
import static ui.Variables.GSON;

public class PreLoginUI extends ServerFacade {

    public static UIData help() {
        String output = """
                register <USERNAME> <PASSWORD> <EMAIL> - create a new user to play chess
                login <USERNAME> <PASSWORD> - login to play chess with an existing user
                quit - exit this chess program
                help - see available commands""";
        return new UIData(UIType.PRELOGIN, output);
    }

    public static UIData register(String username, String password, String email) throws UIException {
        if (username.isBlank() || password.isBlank() || email.isBlank()) {
            throw new UIException(true, "User supplied blank input.");
        }

        Map<String, String> jsonMap = Map.of("username", username, "password", password, "email", email);
        String data = GSON.toJson(jsonMap);

        Map<String, String> response = sendServer("/user", "POST", data);

        if (!response.get("username").isEmpty() && !response.get("authToken").isEmpty()) {
            authToken = response.get("authToken");
            return new UIData(UIType.LOGIN, "Registration Success!\nYou are now registered. ['help']");
        } else {
            throw new UIException(false, "Registration Failed: Response from server blank.");
        }
    }

    public static UIData login(String username, String password) throws UIException {
        if (username.isBlank() || password.isBlank()) {
            throw new UIException(true, "User supplied blank input.");
        }

        Map<String, String> jsonMap = Map.of("username", username, "password", password);
        String data = GSON.toJson(jsonMap);

        Map<String, String> response = sendServer("/session", "POST", data);

        if (!response.get("username").isEmpty() && !response.get("authToken").isEmpty()) {
            authToken = response.get("authToken");
            return new UIData(UIType.LOGIN, "Login Success!\n['help']");
        } else {
            throw new UIException(false, "Registration Failed: Response from server blank.");
        }
    }
}
