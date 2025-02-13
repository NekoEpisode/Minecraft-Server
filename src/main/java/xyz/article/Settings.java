package xyz.article;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Settings {
    public static boolean ONLINE_MODE;
    public static int MAX_PLAYERS;
    public static int SERVER_PORT;
    public static String BIND_ADDRESS;
    public static int VIEW_DISTANCE;

    public static void init(File propertiesFile) throws IOException {
        MAX_PLAYERS = (int) readAndCheck("max-players", 20, propertiesFile);
        ONLINE_MODE = (boolean) readAndCheck("online-mode", true, propertiesFile);
        SERVER_PORT = (int) readAndCheck("server-port", 25565, propertiesFile);
        BIND_ADDRESS = (String) readAndCheck("bind-address", "127.0.0.1", propertiesFile);
        BIND_ADDRESS = (String) readAndCheck("view-distance", 10, propertiesFile);
    }

    public static Object readAndCheck(String key, Object defaultValue, File propertiesFile) throws IOException {
        Yaml yamlLoader = new Yaml();
        Map<String, Object> map = yamlLoader.load(new FileInputStream(propertiesFile));
        if (map == null) map = new HashMap<>();
        map.putIfAbsent(key, defaultValue);
        yamlLoader.dump(map, new FileWriter(propertiesFile));
        return map.get(key);
    }
}
