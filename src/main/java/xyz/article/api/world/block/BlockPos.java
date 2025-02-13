package xyz.article.api.world.block;

import org.cloudburstmc.math.vector.Vector3i;
import xyz.article.api.world.World;

public record BlockPos(World world, Vector3i pos) {
}
