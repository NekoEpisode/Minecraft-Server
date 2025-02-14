package xyz.article.api;

import org.cloudburstmc.math.vector.Vector2i;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.mcprotocollib.network.Session;
import xyz.article.RunningData;
import xyz.article.api.player.Player;
import xyz.article.api.world.World;
import xyz.article.api.world.WorldManager;
import xyz.article.api.world.block.BlockPos;
import xyz.article.api.world.chunk.ChunkPos;

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
    public static Player getPlayer(Session session) {
        for (Player player : RunningData.playerList) {
            if (player.getSession().equals(session)) {
                return player;
            }
        }
        return null;
    }

    public static ChunkPos getChunkPos(int x, int z, World world) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        return new ChunkPos(world, Vector2i.from(chunkX, chunkZ));
    }
    public static ChunkPos getChunkPos(BlockPos blockPos) {
        int chunkX = blockPos.pos().getX() >> 4;
        int chunkZ = blockPos.pos().getZ() >> 4;
        return new ChunkPos(blockPos.world(), Vector2i.from(chunkX, chunkZ));
    }

    public static int getChunkSectionIndex(int y) {
        int sectionHeight = 16;
        int worldBottom = -64;
        return (y - worldBottom) / sectionHeight;
    }

    public static Vector3i getInChunkLocation(int x, int y, int z){
        x = x & 15;
        y = y & 15;
        z = z & 15;
        return Vector3i.from(x, y, z);
    }

    public static Vector3i getInChunkSectionLocation(int x, int y, int z, int sectionIndex) {
        int sectionHeight = 16;
        int worldBottom = -64;
        int x1 = x & 15;
        int y1 = y - (sectionIndex * sectionHeight + worldBottom);
        int z1 = z & 15;
        return Vector3i.from(x1, y1, z1);
    }
}
