package xyz.article.api;

import org.cloudburstmc.math.vector.Vector3d;
import org.jetbrains.annotations.NotNull;
import xyz.article.api.world.World;

public record Location(World world, Vector3d pos) {
    /**
     * 计算这个位置与另一个位置之间的距离。
     *
     * @param other 另一个位置
     * @return 两个位置之间的距离
     */
    public double distance(@NotNull Location other) {
        double dx = this.pos.getX() - other.pos.getX();
        double dy = this.pos.getY() - other.pos.getY();
        double dz = this.pos.getZ() - other.pos.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
