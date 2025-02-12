package xyz.article.chunk;

import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;

import java.util.Arrays;

public class HeightMap {
    private final int[] heightMap; // 存储每个 (x, z) 位置的高度
    private static final int CHUNK_SIZE = 16; // 区块大小为 16x16

    public HeightMap() {
        this.heightMap = new int[CHUNK_SIZE * CHUNK_SIZE]; // 初始化高度图数组
        Arrays.fill(heightMap, 0); // 默认高度为 0
    }

    /**
     * 设置某个 (x, z) 位置的高度
     *
     * @param x     区块内的 x 坐标 (0-15)
     * @param z     区块内的 z 坐标 (0-15)
     * @param height 高度值
     */
    public void setHeight(int x, int z, int height) {
        if (x < 0 || x >= CHUNK_SIZE || z < 0 || z >= CHUNK_SIZE) {
            throw new IllegalArgumentException("x and z must be between 0 and 15");
        }
        heightMap[z * CHUNK_SIZE + x] = height;
    }

    /**
     * 获取某个 (x, z) 位置的高度
     *
     * @param x 区块内的 x 坐标 (0-15)
     * @param z 区块内的 z 坐标 (0-15)
     * @return 高度值
     */
    public int getHeight(int x, int z) {
        if (x < 0 || x >= CHUNK_SIZE || z < 0 || z >= CHUNK_SIZE) {
            throw new IllegalArgumentException("x and z must be between 0 and 15");
        }
        return heightMap[z * CHUNK_SIZE + x];
    }

    /**
     * 将高度图转换为 NbtMap
     *
     * @return 高度图的 NBT 表示
     */
    public NbtMap toNbt() {
        NbtMapBuilder builder = NbtMap.builder();
        for (int z = 0; z < CHUNK_SIZE; z++) {
            for (int x = 0; x < CHUNK_SIZE; x++) {
                builder.putInt("height_" + x + "_" + z, heightMap[z * CHUNK_SIZE + x]);
            }
        }
        return builder.build();
    }

    /**
     * 从 NbtMap 加载高度图
     *
     * @param nbt 高度图的 NBT 数据
     */
    public void fromNbt(NbtMap nbt) {
        for (int z = 0; z < CHUNK_SIZE; z++) {
            for (int x = 0; x < CHUNK_SIZE; x++) {
                String key = "height_" + x + "_" + z;
                if (nbt.containsKey(key)) {
                    heightMap[z * CHUNK_SIZE + x] = nbt.getInt(key);
                }
            }
        }
    }

    /**
     * 获取高度图的原始数组
     *
     * @return 高度图数组
     */
    public int[] getHeightMap() {
        return heightMap;
    }
}