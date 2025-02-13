package xyz.article.api;

import xyz.article.RunningData;
import xyz.article.api.player.Player;
import xyz.article.api.world.World;
import xyz.article.api.world.WorldManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Slider {
    public static List<World> getWorlds() {
        List<World> worlds = new ArrayList<>();
        WorldManager.worldMap.forEach((key, world) -> worlds.add(world));
        return worlds;
    }

    public static Player getPlayer(String playerName) {
        for (Player player : RunningData.playerList) {
            if (player.getProfile().getName().equalsIgnoreCase(playerName)) {
                return player;
            }
        }
        return null;
    }
    public static Player getPlayer(UUID playerUUID) {
        for (Player player : RunningData.playerList) {
            if (player.getProfile().getId().equals(playerUUID)) {
                return player;
            }
        }
        return null;
    }
}
