package xyz.article.api;

import xyz.article.api.world.World;
import xyz.article.api.world.WorldManager;

import java.util.ArrayList;
import java.util.List;

public class Slider {
    public static List<World> getWorlds() {
        List<World> worlds = new ArrayList<>();
        WorldManager.worldMap.forEach((key, world) -> worlds.add(world));
        return worlds;
    }
}
