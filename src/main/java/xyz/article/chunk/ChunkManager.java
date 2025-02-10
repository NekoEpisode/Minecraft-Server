package xyz.article.chunk;

import org.cloudburstmc.math.vector.Vector2i;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.nbt.NbtMap;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.data.game.level.LightUpdateData;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityInfo;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundForgetLevelChunkPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundLevelChunkWithLightPacket;
import xyz.article.MinecraftServer;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChunkManager {
    private static final int RENDER_DISTANCE = 8;
    private final Map<Vector2i, ChunkData> loadedChunks = new HashMap<>();

    public void updateChunksForPlayer(Session session, Vector3i playerPosition) {
        Vector2i playerChunkPos = Vector2i.from(playerPosition.getX() >> 4, playerPosition.getZ() >> 4);

        Map<Vector2i, Boolean> newLoadedChunks = new HashMap<>();
        for (int x = -RENDER_DISTANCE; x <= RENDER_DISTANCE; x++) {
            for (int z = -RENDER_DISTANCE; z <= RENDER_DISTANCE; z++) {
                if (x * x + z * z <= RENDER_DISTANCE * RENDER_DISTANCE) {
                    Vector2i chunkPos = playerChunkPos.add(x, z);
                    newLoadedChunks.put(chunkPos, true);
                    saveChunkData(chunkPos, session);
                }
            }
        }

        for (Map.Entry<Vector2i, ChunkData> entry : loadedChunks.entrySet()) {
            if (!newLoadedChunks.containsKey(entry.getKey())) {
                session.send(new ClientboundForgetLevelChunkPacket(entry.getKey().getX(), entry.getKey().getY()));
            }
        }

        loadedChunks.keySet().retainAll(newLoadedChunks.keySet());
    }

    private ChunkData loadOrCreateChunkData(Vector2i chunkPos) {
        ChunkData chunkData = loadedChunks.get(chunkPos);
        if (chunkData == null) {
            byte[] chunkDataBytes = ChunkSender.createChunkData(new MinecraftCodecHelper());
            LightUpdateData lightData = getLightDataForChunk();
            chunkData = new ChunkData(chunkPos.getX(), chunkPos.getY(), chunkDataBytes, lightData);
        }
        return chunkData;
    }

    private void sendChunkToPlayer(Session session, ChunkData chunkData) {
        ClientboundLevelChunkWithLightPacket chunkPacket = new ClientboundLevelChunkWithLightPacket(
                chunkData.getChunkX(), chunkData.getChunkZ(), chunkData.getChunkData(), NbtMap.EMPTY, new BlockEntityInfo[]{}, chunkData.getLightData()
        );
        session.send(chunkPacket);
    }

    private LightUpdateData getLightDataForChunk() {
        return new LightUpdateData(new BitSet(), new BitSet(), new BitSet(), new BitSet(), List.of(), List.of());
    }

    private void saveChunkData(Vector2i chunkPos, Session session) {
        ChunkData chunkData = loadOrCreateChunkData(chunkPos);
        sendChunkToPlayer(session, chunkData);
        loadedChunks.put(chunkPos, chunkData);
    }

    public void broadcastChunkUpdate(int blockPos1, int blockPos2) {
        int chunkX = blockPos1 >> 4;
        int chunkZ = blockPos2 >> 4;
        System.out.println("X" + chunkX + ", Z" + chunkZ);

        ChunkData chunkData = loadOrCreateChunkData(Vector2i.from(chunkX, chunkZ));

        ClientboundLevelChunkWithLightPacket chunkPacket = new ClientboundLevelChunkWithLightPacket(
                chunkX, chunkZ, chunkData.getChunkData(), NbtMap.EMPTY, new BlockEntityInfo[]{}, chunkData.getLightData()
        );

        for (Session playerSession : MinecraftServer.playerSessions) {
            playerSession.send(new ClientboundForgetLevelChunkPacket(chunkX, chunkZ));
            playerSession.send(chunkPacket);
        }
    }

    public void updateChunkAt(Vector3i blockPos, int placedBlockId, Session session) {
        int chunkX = blockPos.getX() >> 4;
        int chunkZ = blockPos.getZ() >> 4;
        Vector2i chunkPos = Vector2i.from(chunkX, chunkZ);

        ChunkData chunkData = loadOrCreateChunkData(chunkPos);

        int blockX = blockPos.getX() & 15;
        int blockY = blockPos.getY();
        int blockZ = blockPos.getZ() & 15;
        chunkData.setBlock(blockX, blockY, blockZ, (byte) placedBlockId);

        saveChunkData(chunkPos, session);
    }
}