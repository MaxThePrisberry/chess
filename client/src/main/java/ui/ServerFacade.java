package ui;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import ui.model.UIData;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.util.Map;

import static ui.Variables.*;

public abstract class ServerFacade {

    public enum UIType {
        PRELOGIN(PreLoginUI.class),
        LOGIN(LoginUI.class),
        GAMEPLAY(GameplayUI.class);

        private final Class<? extends ServerFacade> uiClass;

        UIType(Class<? extends ServerFacade> uiClass) {  // Constructor
            this.uiClass = uiClass;
        }

        public Class<? extends ServerFacade> getUIClass() {  // Method
            return uiClass;
        }
    }

    public static UIData quit() {
        if (inGame) {
            return GameplayUI.leave();
        } else {
            System.exit(0);
            return null;
        }
    }

    private static void throwStatusError(String endpoint, String method, int status) throws UIException {
        switch (status) {
            case 500 -> {
                throw new UIException(false, "The server is encountering unexpected problems. Try again in a bit.");
            }
            case 400 -> {
                if (endpoint.equals("/user")) {
                    throw new UIException(true, "The user data provided makes for an invalid request. Try again.");
                } else if (endpoint.equals("/game") && method.equals("POST")) {
                    throw new UIException(true, "The create game request was invalid. Try again.");
                } else if (endpoint.equals("/game") && method.equals("PUT")) {
                    throw new UIException(true, "Your request to join a game created an invalid request. Try again.");
                }
            }
            case 401 -> {
                if (endpoint.equals("/session") && method.equals("POST")) {
                    throw new UIException(true, "A username or password given was incorrect. Try again.");
                } else {
                    throw new UIException(true, "You don't seem to be logged in correctly. Log out->in and retry.");
                }
            }
            case 403 -> {
                if (endpoint.equals("/user")) {
                    throw new UIException(true, "A user with that name already exists. Try a different username.");
                } else if (endpoint.equals("/game")) {
                    throw new UIException(true, "You can't join a game as that position because someone's already" +
                            " playing there. Try a different position.");
                }
            }
            default -> {
                throw new UIException(false, "An unaccounted for status code was thrown.");
            }
        }
    }

    protected static Map sendServer(String endpoint, String method, String body) throws UIException {
        URI uri;
        HttpURLConnection http;
        try {
            uri = new URI(SERVER_LOCATION + endpoint);
            http = (HttpURLConnection) uri.toURL().openConnection();
            http.setRequestMethod(method);
            if (authToken != null && !authToken.isBlank()) {
                http.addRequestProperty("Authorization", authToken);
            }
            if (body != null && !body.isEmpty()) {
                http.setDoOutput(true);
                try (OutputStream writeStream = http.getOutputStream()) {
                    writeStream.write(body.getBytes());
                }
            }
            http.connect();
            int status = http.getResponseCode();
            if (status < 200 || status > 299) {
                throwStatusError(endpoint, method, status);
            }
            Map response;
            try (InputStream readStream = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(readStream);
                response = gson.fromJson(reader, Map.class);
            }
            return response;
        } catch (ProtocolException e) {
            throw new UIException(false, "A protocol exception was thrown");
        } catch (MalformedURLException e) {
            throw new UIException(false, "The URL given to connect was malformed");
        } catch (URISyntaxException e) {
            throw new UIException(false, "URI syntax doesn't like the server location variable");
        } catch (IOException e) {
            throw new UIException(false, "There was an error interacting with the server");
        } catch (JsonIOException | JsonSyntaxException e) {
            throw new UIException(false, "We got a problem with the JSON");
        }
    }
}
