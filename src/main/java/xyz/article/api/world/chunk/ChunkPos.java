package xyz.article.api.world.chunk;

import org.cloudburstmc.math.vector.Vector2i;
import xyz.article.api.world.World;

public class ChunkPos {
    private final World world;
    private final Vector2i pos;

    public ChunkPos(World world, Vector2i pos) {
        this.world = world;
        this.pos = pos;
    }

    public World getWorld() {
        return world;
    }

    public Vector2i getPos() {
        return pos;
    }
}
