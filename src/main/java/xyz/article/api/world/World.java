package xyz.article.api.world;

import net.kyori.adventure.key.Key;
import org.cloudburstmc.math.vector.Vector2i;
import xyz.article.api.world.chunk.ChunkData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class World {
    private Map<Vector2i, ChunkData> chunkDataMap = new ConcurrentHashMap<>();

    public World(Key key) {
        WorldManager.worldMap.put(key, this);
    }

    public void setChunkMap(Map<Vector2i, ChunkData> chunkDataMap) {
        this.chunkDataMap = chunkDataMap;
    }

    public Map<Vector2i, ChunkData> getChunkDataMap() {
        return chunkDataMap;
    }
}
