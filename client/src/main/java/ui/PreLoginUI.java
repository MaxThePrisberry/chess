package ui;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import ui.model.UIData;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.util.Map;

public class PreLoginUI extends UserUI {
    private static final Gson gson = new Gson();

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
        String data = gson.toJson(jsonMap);

        Map<String, String> response = sendServer("/user", "POST", null, data);

        if (!response.get("username").isEmpty() && !response.get("authToken").isEmpty()) {
            return new UIData(UIType.LOGIN, "Registration Success!\nYou are now logged in. ['help']");
        } else {
            throw new UIException(false, "Registration Failed: Response from server blank.");
        }
    }
}
