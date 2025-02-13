package xyz.article.api.world;

import net.kyori.adventure.key.Key;
import org.cloudburstmc.math.vector.Vector2i;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockChangeEntry;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSectionBlocksUpdatePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.MinecraftServer;
import xyz.article.api.world.block.BlockPos;
import xyz.article.api.world.chunk.ChunkData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class World {
    private static final Logger log = LoggerFactory.getLogger(World.class);
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

    public void setBlock(int x, int y, int z, int blockID) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        ChunkData chunk = chunkDataMap.get(Vector2i.from(chunkX, chunkZ));
        if (chunk != null) {
            int sectionHeight = 16;
            int worldBottom = -64;
            int sectionIndex = (y - worldBottom) / sectionHeight;
            ChunkSection[] chunkSections = chunk.getChunkSections();
            int localX = x & 15;
            int localY = y & 15;
            int localZ = z & 15;
            chunkSections[sectionIndex].setBlock(localX, localY, localZ, blockID);
            for (Session session1 : MinecraftServer.playerSessions) {
                session1.send(new ClientboundSectionBlocksUpdatePacket(chunkX, y >> 4, chunkZ, new BlockChangeEntry(Vector3i.from(localX, localY, localZ), blockID)));
            }
        }else {
            log.error("Chunk {}, {} is null!", chunkX, chunkZ);
        }
    }
}
