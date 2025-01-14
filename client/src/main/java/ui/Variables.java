package ui;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class Variables {

    public static final String SERVER_LOCATION = "http://localhost:8080";
    public static final Gson gson = new Gson();
    public static String authToken;

    public static Map<Integer, Integer> gameIDMap = new HashMap<>();

}
