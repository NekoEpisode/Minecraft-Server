package xyz.article.api.world.chunk;

import org.cloudburstmc.math.vector.Vector2i;
import xyz.article.api.world.World;

public record ChunkPos(World world, Vector2i pos) {
}
