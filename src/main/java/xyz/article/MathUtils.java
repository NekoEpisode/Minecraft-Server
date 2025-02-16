package xyz.article;

import java.util.ArrayList;
import java.util.List;

public class MathUtils {
    /**
     * 获取圆内所有区块的坐标
     *
     * @param centerChunkX 中心区块的 X 坐标
     * @param centerChunkZ 中心区块的 Z 坐标
     * @param viewDistance 圆的半径（区块数）
     * @return 圆内所有区块的坐标列表
     */
    public static List<int[]> getChunkCoordinatesInCircle(int centerChunkX, int centerChunkZ, int viewDistance) {
        List<int[]> chunkCoordinates = new ArrayList<>();

        // 遍历以中心区块为原点的正方形区域
        for (int dx = -viewDistance; dx <= viewDistance; dx++) {
            for (int dz = -viewDistance; dz <= viewDistance; dz++) {
                // 计算当前区块到中心区块的距离
                double distanceSquared = dx * dx + dz * dz;

                // 如果距离小于或等于半径的平方，则在圆内
                if (distanceSquared <= viewDistance * viewDistance) {
                    // 计算当前区块的坐标
                    int chunkX = centerChunkX + dx;
                    int chunkZ = centerChunkZ + dz;

                    // 添加到结果列表
                    chunkCoordinates.add(new int[]{chunkX, chunkZ});
                }
            }
        }

        return chunkCoordinates;
    }
}
