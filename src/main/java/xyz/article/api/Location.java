package xyz.article.api;

import org.cloudburstmc.math.vector.Vector3d;
import xyz.article.api.world.World;

public record Location(World world, Vector3d pos) {
}
