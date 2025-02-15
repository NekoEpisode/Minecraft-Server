package xyz.article;

import org.cloudburstmc.math.vector.Vector2i;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.BitStorage;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.DataPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.GlobalPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.ListPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.PaletteType;
import org.geysermc.mcprotocollib.protocol.data.game.level.LightUpdateData;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityInfo;
import xyz.article.api.world.chunk.ChunkData;
import xyz.article.api.world.chunk.ChunkPos;
import xyz.article.api.world.chunk.HeightMap;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

public class PerlinNoise {

    private static final int OCTAVES = 1; // 噪声层数，控制地形的细节
    private static final double PERSISTENCE = 0.5; // 持久度，控制每层噪声的影响
    public static final double SCALE = 0.5; // 缩放比例，控制地形的平滑度

    private final int[] permutations; // 排列数组，用于噪声计算

    public PerlinNoise(long seed) {
        Random random = new Random(seed);
        permutations = new int[512];
        int[] p = new int[256];
        for (int i = 0; i < 256; i++) {
            p[i] = i;
        }
        // 打乱数组
        for (int i = 0; i < 256; i++) {
            int j = random.nextInt(256);
            int temp = p[i];
            p[i] = p[j];
            p[j] = temp;
        }
        // 复制到 permutations 数组
        System.arraycopy(p, 0, permutations, 0, 256);
        System.arraycopy(p, 0, permutations, 256, 256);
    }

    /**
     * 计算多层级Perlin噪声值
     *
     * @param x X 坐标
     * @param y Y 坐标
     * @param z Z 坐标
     * @return 噪声值，范围 [-1, 1]
     */
    public double noise(double x, double y, double z) {
        double total = 0;
        double frequency = 1;
        double amplitude = 1;
        double maxValue = 0;

        for (int i = 0; i < OCTAVES; i++) {
            total += noiseSingle(x * frequency, y * frequency, z * frequency) * amplitude;

            maxValue += amplitude;

            amplitude *= PERSISTENCE;
            frequency *= 2;
        }

        return total / maxValue;
    }

    /**
     * 计算单层Perlin噪声值
     *
     * @param x X 坐标
     * @param y Y 坐标
     * @param z Z 坐标
     * @return 噪声值，范围 [-1, 1]
     */
    private double noiseSingle(double x, double y, double z) {
        // 找到单位立方体的坐标
        int xi = (int) Math.floor(x) & 255;
        int yi = (int) Math.floor(y) & 255;
        int zi = (int) Math.floor(z) & 255;

        // 计算相对坐标
        double xf = x - Math.floor(x);
        double yf = y - Math.floor(y);
        double zf = z - Math.floor(z);

        // 计算渐变值
        double u = fade(xf);
        double v = fade(yf);
        double w = fade(zf);

        // 哈希值
        int a = permutations[xi] + yi;
        int aa = permutations[a] + zi;
        int ab = permutations[a + 1] + zi;
        int b = permutations[xi + 1] + yi;
        int ba = permutations[b] + zi;
        int bb = permutations[b + 1] + zi;

        // 插值
        double x1 = lerp(grad(permutations[aa], xf, yf, zf),
                grad(permutations[ba], xf - 1, yf, zf), u);
        double x2 = lerp(grad(permutations[ab], xf, yf - 1, zf),
                grad(permutations[bb], xf - 1, yf - 1, zf), u);
        double y1 = lerp(x1, x2, v);

        double z1 = lerp(grad(permutations[aa + 1], xf, yf, zf - 1),
                grad(permutations[ba + 1], xf - 1, yf, zf - 1), u);
        double z2 = lerp(grad(permutations[ab + 1], xf, yf - 1, zf - 1),
                grad(permutations[bb + 1], xf - 1, yf - 1, zf - 1), u);
        double z3 = lerp(z1, z2, v);

        return lerp(y1, z3, w);
    }

    /**
     * 线性插值
     */
    private double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    /**
     * 渐变函数
     */
    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    /**
     * 梯度函数
     */
    private double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y,
                v = h < 4 ? y : h == 12 || h == 14 ? x : z;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    /**
     * 构建地形并返回区块数组
     *
     * @param rows    行数
     * @param columns 列数
     * @return 区块数组
     */
    public ChunkData[][] generateTerrain(int rows, int columns) {
        ChunkData[][] chunks = new ChunkData[rows][columns];

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                Vector2i chunkPos = Vector2i.from(row, column);
                ChunkSection[] chunkSections = new ChunkSection[24];

                for (int i = 0; i < 24; i++) {
                    DataPalette blockPalette = DataPalette.createForChunk();
                    DataPalette biomePalette = new DataPalette(GlobalPalette.INSTANCE, new BitStorage(16, 16 * 16 * 16), PaletteType.BIOME);
                    ChunkSection section = new ChunkSection(0, blockPalette, biomePalette);
                    chunkSections[i] = section;

                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            int height = (int) (noise((row * 16 + x) * PerlinNoise.SCALE, 0, (column * 16 + z) * PerlinNoise.SCALE) * 32 + 64);
                            for (int y = 0; y < 16; y++) {
                                int blockY = i * 16 + y;
                                if (blockY < height) {
                                    // 填充方块，例如石头
                                    section.setBlock(x, y, z, 1);
                                } else {
                                    // 设置空气方块
                                    section.setBlock(x, y, z, 0);
                                }
                                if (blockY == height) {
                                    // 表面方块，例如草方块
                                    section.setBlock(x, y, z, 9);
                                }
                            }
                        }
                    }
                }

                HeightMap heightMap = new HeightMap();
                BlockEntityInfo[] blockEntities = new BlockEntityInfo[]{};
                LightUpdateData lightUpdateData = new LightUpdateData(new BitSet(), new BitSet(), new BitSet(), new BitSet(), List.of(), List.of());

                chunks[row][column] = new ChunkData(new ChunkPos(MinecraftServer.overworld, chunkPos), chunkSections, heightMap, blockEntities, lightUpdateData);
            }
        }

        return chunks;
    }
}