package xyz.article.perlinNoise;

import org.cloudburstmc.math.vector.Vector2i;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.BitStorage;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.DataPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.GlobalPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.PaletteType;
import org.geysermc.mcprotocollib.protocol.data.game.level.LightUpdateData;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityInfo;
import xyz.article.MinecraftServer;
import xyz.article.api.world.chunk.ChunkData;
import xyz.article.api.world.chunk.ChunkPos;
import xyz.article.api.world.chunk.HeightMap;

import java.util.BitSet;
import java.util.List;

public class TerrainGenerator {
    private final PerlinNoise perlinNoise;
    private final double SCALE;

    public TerrainGenerator(long seed, double SCALE){
        perlinNoise = new PerlinNoise(seed);
        this.SCALE = SCALE;
    }

    public ChunkData[][] generateTerrain(int rows, int columns) {
        ChunkData[][] chunks = new ChunkData[rows][columns];

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                Vector2i chunkPos = Vector2i.from(row, column);
                ChunkSection[] chunkSections = new ChunkSection[24];

                // 初始化高度图
                HeightMap heightMap = new HeightMap();

                for (int i = 0; i < 24; i++) {
                    DataPalette blockPalette = DataPalette.createForChunk();
                    DataPalette biomePalette = new DataPalette(GlobalPalette.INSTANCE, new BitStorage(16, 16 * 16 * 16), PaletteType.BIOME);
                    ChunkSection section = new ChunkSection(0, blockPalette, biomePalette);
                    chunkSections[i] = section;

                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            double noiseValue = perlinNoise.noise((row * 16 + x) * SCALE, 0, (column * 16 + z) * SCALE);
                            int height = (int) (noiseValue * 10 + 80); // 高度在 80 上下波动，幅度为 10

                            // 更新高度图
                            if (i == 0 || height > heightMap.getHeight(x, z)) {
                                heightMap.setHeight(x, z, height);
                            }

                            for (int y = 0; y < 16; y++) {
                                int blockY = i * 16 + y;
                                if (blockY < height) {
                                    section.setBlock(x, y, z, 1);
                                } else {
                                    section.setBlock(x, y, z, 0);
                                }
                                if (blockY == height) {
                                    section.setBlock(x, y, z, 9);
                                }
                            }
                        }
                    }
                }

                // 计算光照数据
                LightUpdateData lightUpdateData = calculateLighting(heightMap);

                BlockEntityInfo[] blockEntities = new BlockEntityInfo[]{};
                chunks[row][column] = new ChunkData(new ChunkPos(MinecraftServer.overworld, chunkPos), chunkSections, heightMap, blockEntities, lightUpdateData);
            }
        }

        return chunks;
    }

    /**
     * 计算光照数据
     *
     * @param heightMap 高度图
     * @return 光照数据
     */
    private LightUpdateData calculateLighting(HeightMap heightMap) {
        BitSet skyLight = new BitSet(16 * 16 * 256);
        BitSet blockLight = new BitSet(16 * 16 * 256);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int height = heightMap.getHeight(x, z);

                for (int y = 0; y < 256; y++) {
                    skyLight.set(y * 16 * 16 + z * 16 + x, y > height);
                }
                for (int y = 0; y < 256; y++) {
                    blockLight.set(y * 16 * 16 + z * 16 + x, false);
                }
            }
        }

        return new LightUpdateData(skyLight, blockLight, new BitSet(), new BitSet(), List.of(), List.of());
    }
}
