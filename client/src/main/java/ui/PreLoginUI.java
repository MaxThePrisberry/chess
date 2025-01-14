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
import java.util.HashMap;
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
            throw new UIException(true, "User supplied blank inputs.");
        }

        Map<String, String> jsonMap = Map.of("username", username, "password", password, "email", email);
        String data = gson.toJson(jsonMap);
        URI uri;
        HttpURLConnection http;
        try {
            uri = new URI(SERVER_LOCATION);
            http = (HttpURLConnection) uri.toURL().openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            try (OutputStream writeStream = http.getOutputStream()) {
                writeStream.write(data.getBytes());
            }
            http.connect();
            int status = http.getResponseCode();
            if (status >= 200 && status < 300) {

            }
            Map<String, String> response;
            try (InputStream readStream = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(readStream);
                response = gson.fromJson(reader, Map.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (JsonIOException | JsonSyntaxException e) {
                throw new RuntimeException("We got a problem with the JSON");
            }

            if (!response.get("username").isEmpty() && !response.get("authToken").isEmpty()) {
                return new UIData(UIType.LOGIN, "Registration Success!");
            } else {
                return new UIData(UIType.PRELOGIN, "Registration Failed: Server returned malformed response.");
            }
        } catch (ProtocolException e) {
            throw new UIException(false, "A protocol exception was thrown");
        } catch (MalformedURLException e) {
            throw new UIException(false, "The URL given to connect was malformed");
        } catch (URISyntaxException e) {
            throw new UIException(false, "URI syntax doesn't like the server location variable");
        } catch (IOException e) {
            throw new UIException(false, "There was an error interacting with the server");
        }
    }
}
