package xyz.article.chunk;

import org.cloudburstmc.math.vector.Vector2i;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkManager {
    public static Map<Vector2i, ChunkData> chunkDataMap = new ConcurrentHashMap<>();
}
