package xyz.article.perlinNoise;

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
import xyz.article.MinecraftServer;
import xyz.article.api.world.chunk.ChunkData;
import xyz.article.api.world.chunk.ChunkPos;
import xyz.article.api.world.chunk.HeightMap;

import java.util.BitSet;
import java.util.List;
import java.util.Random;

public class TerrainGenerator {
    private static final Logger log = LoggerFactory.getLogger(TerrainGenerator.class);
    private final PerlinNoise perlinNoise;
    private final double SCALE;

    public TerrainGenerator(long seed, double SCALE){
        perlinNoise = new PerlinNoise(seed);
        this.SCALE = SCALE;
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

    public ChunkData[][] generateTerrain(int rows, int columns) {
        ChunkData[][] chunks = new ChunkData[rows][columns];
        int totalChunks = rows * columns; // 总区块数量
        int completedChunks = 0; // 已完成的区块数量

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
                                    section.setBlock(x, y, z, 1); // 设置地面方块（例如石头）
                                } else if (blockY == height) {
                                    section.setBlock(x, y, z, 9); // 设置地表方块（例如草方块）
                                } else {
                                    section.setBlock(x, y, z, 0); // 设置空气方块
                                }
                            }
                        }
                    }
                }

                // 生成树
                //generateTrees(chunkSections, heightMap);

                // 计算光照数据
                LightUpdateData lightUpdateData = calculateLighting(heightMap);

                BlockEntityInfo[] blockEntities = new BlockEntityInfo[]{};
                chunks[row][column] = new ChunkData(new ChunkPos(MinecraftServer.overworld, chunkPos), chunkSections, heightMap, blockEntities, lightUpdateData);

                // 更新已完成区块数量
                completedChunks++;
                // 计算进度百分比
                double progress = (double) completedChunks / totalChunks * 100;
                // 输出进度信息
                System.out.printf("进度: %.2f%% (已完成 %d / 总区块 %d)%n", progress, completedChunks, totalChunks);
            }
        }

        return chunks;
    }

    /**
     * 在区块中生成树
     *
     * @param chunkSections 区块的 ChunkSection 数组
     * @param heightMap     高度图
     */
    private void generateTrees(ChunkSection[] chunkSections, HeightMap heightMap) {
        Random random = new Random();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                // 检查是否适合生成树（例如地表是草方块）
                int surfaceY = heightMap.getHeight(x, z);
                if (surfaceY < -63 || surfaceY >= 319) continue; // 确保高度在有效范围内

                // 随机决定是否生成树
                if (random.nextDouble() < 0.02) { // 2% 的概率生成树
                    int treeHeight = random.nextInt(4) + 4; // 树的高度在 4 到 7 之间
                    generateTree(chunkSections, x, surfaceY + 1, z, treeHeight);
                }
            }
        }
    }

    /**
     * 在指定位置生成一棵树
     *
     * @param chunkSections 区块的 ChunkSection 数组
     * @param x             树的 X 坐标
     * @param y             树的 Y 坐标
     * @param z             树的 Z 坐标
     * @param height        树的高度
     */
    private void generateTree(ChunkSection[] chunkSections, int x, int y, int z, int height) {
        // 生成树干
        for (int i = 0; i < height; i++) {
            setBlock(chunkSections, x, y + i, z, 5); // 5 是原木的方块 ID
        }

        // 生成树叶
        int leavesStart = y + height - 2; // 树叶从树干顶部向下 2 格开始
        for (int dy = leavesStart; dy <= y + height; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    // 限制树叶的范围
                    if (Math.abs(dx) + Math.abs(dz) <= 3) {
                        setBlock(chunkSections, x + dx, dy, z + dz, 6); // 6 是树叶的方块 ID
                    }
                }
            }
        }
    }

    /**
     * 在区块中设置方块
     *
     * @param chunkSections 区块的 ChunkSection 数组
     * @param x             方块的 X 坐标
     * @param y             方块的 Y 坐标
     * @param z             方块的 Z 坐标
     * @param blockId       方块的 ID
     */
    private void setBlock(ChunkSection[] chunkSections, int x, int y, int z, int blockId) {
        if (x < 0 || x >= 16 || y < 0 || y >= 256 || z < 0 || z >= 16) return; // 确保坐标在区块范围内

        int sectionIndex = y / 16;
        int localY = y % 16;
        ChunkSection section = chunkSections[sectionIndex];
        if (section != null) {
            section.setBlock(x, localY, z, blockId);
        }
    }
}
