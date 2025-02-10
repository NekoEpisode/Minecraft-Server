package xyz.article;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class RunningData {
    public static boolean ONLINE_MODE;
    public static int MAX_PLAYERS;

    public static void init(File propertiesFile) throws IOException {
        MAX_PLAYERS = (Integer) readAndCheck("max-players", 20, propertiesFile);
        ONLINE_MODE = (boolean) readAndCheck("online-mode", true, propertiesFile);
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
