package xyz.article.api.world;

import net.kyori.adventure.key.Key;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorldManager {
    public static Map<Key, World> worldMap = new ConcurrentHashMap<>();
}
