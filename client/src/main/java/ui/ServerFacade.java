package ui;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

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
        LOGIN(LoginUI.class);

        private final Class<? extends ServerFacade> uiClass;

        UIType(Class<? extends ServerFacade> uiClass) {  // Constructor
            this.uiClass = uiClass;
        }

        public Class<? extends ServerFacade> getUIClass() {  // Method
            return uiClass;
        }
    }

    protected static void quit() {
        System.exit(0);
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
                throw new UIException(false, "Server responded with error code " + status);
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

    protected static String printChessBoard(String color) {
        return "ChessBoard representation of " + color;
    }
}
