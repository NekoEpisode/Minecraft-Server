package xyz.article;

import org.cloudburstmc.math.vector.Vector2i;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.BitStorage;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.DataPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.GlobalPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.PaletteType;
import org.geysermc.mcprotocollib.protocol.data.game.level.LightUpdateData;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.api.world.chunk.ChunkData;
import xyz.article.api.world.chunk.ChunkPos;
import xyz.article.api.world.chunk.HeightMap;

import java.util.BitSet;
import java.util.List;
import java.util.Random;

public class Chunk {
    private static final Logger log = LoggerFactory.getLogger(Chunk.class);
    private static final int OCTAVES = 4; // 噪声层数，控制地形的细节
    private static final double PERSISTENCE = 0.5; // 持久度，控制每层噪声的影响
    public static final double SCALE = 0.05; // 缩放比例，控制地形的平滑度

    public static ChunkData createSimpleGrassChunk(int chunkX, int chunkZ) {
        if (MinecraftServer.overworld.getChunkDataMap().get(Vector2i.from(chunkX, chunkZ)) != null) {
            return MinecraftServer.overworld.getChunkDataMap().get(Vector2i.from(chunkX, chunkZ));
        }
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

    private static final PerlinNoise noise = new PerlinNoise(new Random().nextLong());

    public static ChunkData[] generate5x5ChunkTerrain(int startX, int startZ) {
        int chunkSize = 16; // 每个区块的尺寸
        int chunksPerRow = 7; // 每行的区块数量
        ChunkData[] chunkDataArray = new ChunkData[chunksPerRow * chunksPerRow]; // 存储所有区块数据的数组

        for (int chunkX = 0; chunkX < chunksPerRow; chunkX++) {
            for (int chunkZ = 0; chunkZ < chunksPerRow; chunkZ++) {
                int chunkIndex = chunkX * chunksPerRow + chunkZ;
                Vector2i chunkVec = Vector2i.from(startX + chunkX * chunkSize, startZ + chunkZ * chunkSize);
                // 检查内存中是否已有该区块的数据
                ChunkData existingChunkData = MinecraftServer.overworld.getChunkDataMap().get(chunkVec);
                if (existingChunkData != null) {
                    chunkDataArray[chunkIndex] = existingChunkData;
                } else {
                    // 创建新的区块数据
                    ChunkSection[] chunkSections = new ChunkSection[24]; // 假设世界高度为256，即16个子区块
                    for (int i = 0; i < chunkSections.length; i++) {
                        chunkSections[i] = new ChunkSection(0, new DataPalette(GlobalPalette.INSTANCE, new BitStorage(16, 16 * 16 * 16),  PaletteType.CHUNK), new DataPalette(GlobalPalette.INSTANCE, new BitStorage(16, 4 * 4 * 4),  PaletteType.BIOME));
                    }

                    for (int x = 0; x < chunkSize; x++) {
                        for (int z = 0; z < chunkSize; z++) {
                            int globalX = startX + chunkX * chunkSize + x;
                            int globalZ = startZ + chunkZ * chunkSize + z;
                            // 使用generateTerrain的方法生成高度
                            double noiseValue = 0;
                            double amplitude = 1;
                            double frequency = 1;
                            for (int i = 0; i < OCTAVES; i++) {
                                noiseValue += noise.noise((globalX * SCALE + i) * frequency, 0, (globalZ * SCALE + i) * frequency) * amplitude;
                                amplitude *= PERSISTENCE;
                                frequency *= 2;
                            }
                            // 将噪声值映射到地形高度
                            int height = (int) (noiseValue * 10 + 0);
                            // 设置方块
                            for (int y = 0; y < height; y++) {
                                int subChunkY = y / 16;
                                int localY = y % 16;
                                ChunkSection chunkSection = chunkSections[subChunkY];
                                chunkSection.setBlock(x, localY, z, y == height - 1 ? 9 : 1); // 9 是草方块，1 是石头
                            }
                        }
                    }

                    ChunkData newChunkData = new ChunkData(new ChunkPos(MinecraftServer.overworld, chunkVec), chunkSections, new HeightMap(), new BlockEntityInfo[]{}, new LightUpdateData(new BitSet(), new BitSet(), new BitSet(), new BitSet(), List.of(new byte[]{(byte) 2048}), List.of(new byte[]{(byte) 2048})));
                    chunkDataArray[chunkIndex] = newChunkData;
                    MinecraftServer.overworld.getChunkDataMap().put(chunkVec, newChunkData);
                }
            }
        }

        return chunkDataArray;
    }
}
