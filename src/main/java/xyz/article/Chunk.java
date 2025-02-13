package xyz.article;

import org.cloudburstmc.math.vector.Vector2i;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.BitStorage;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.DataPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.GlobalPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.PaletteType;
import org.geysermc.mcprotocollib.protocol.data.game.level.LightUpdateData;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityInfo;
import xyz.article.api.world.chunk.ChunkData;
import xyz.article.api.world.chunk.ChunkPos;
import xyz.article.api.world.chunk.HeightMap;

import java.util.BitSet;
import java.util.List;

public class Chunk {
    public static ChunkData createSimpleGrassChunk(int chunkX, int chunkZ) {
        ChunkSection[] chunkSections = new ChunkSection[24];
        DataPalette dataPalette = new DataPalette(GlobalPalette.INSTANCE, new BitStorage(16, 16 * 16 * 16), PaletteType.BIOME);
        for (int i = 0; i < 24; i++) {
            chunkSections[i] = new ChunkSection(0, DataPalette.createForChunk(), dataPalette);
        }

        for (int subChunkY = 1; subChunkY < 24; subChunkY++) {
            ChunkSection chunkSection = chunkSections[subChunkY];
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        chunkSection.setBlock(x, y, z, 0);
                    }
                }
            }
        }

        ChunkSection chunkSection = chunkSections[0];
        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    chunkSection.setBlock(x, y, z, 9);
                }
            }
        }
        ChunkSection chunkSection1 = chunkSections[3];
        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    chunkSection1.setBlock(x, y, z, 9);
                }
            }
        }

        return new ChunkData(new ChunkPos(MinecraftServer.overworld, Vector2i.from(chunkX, chunkZ)), chunkSections, new HeightMap(), new BlockEntityInfo[]{}, new LightUpdateData(new BitSet(), new BitSet(), new BitSet(), new BitSet(), List.of(new byte[]{(byte) 2048}), List.of(new byte[]{(byte) 2048})));
    }
}
