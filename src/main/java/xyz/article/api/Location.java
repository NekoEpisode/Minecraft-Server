package xyz.article.api;

import org.cloudburstmc.math.vector.Vector3d;
import xyz.article.api.world.World;

public class Location {
    private final Vector3d pos;
    private final World world;

    public Location(World world, Vector3d pos) {
        this.pos = pos;
        this.world = world;
    }

    public World getWorld() {
        return world;
    }

    public Vector3d getPos() {
        return pos;
    }
}
