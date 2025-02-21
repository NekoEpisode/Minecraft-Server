package xyz.article.api.entity;

import org.geysermc.mcprotocollib.protocol.data.game.entity.type.EntityType;
import xyz.article.api.Location;

public class Entity {
    private final EntityType type;
    private final int entityId;
    private Location location;
    private final long spawnTime;

    public Entity(Location location, EntityType entityType, int entityId, long spawnTime) {
        this.location = location;
        this.type = entityType;
        this.entityId = entityId;
        this.spawnTime = spawnTime;
        location.world().getEntities().add(this);
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public EntityType getType() {
        return type;
    }

    public int getEntityId() {
        return entityId;
    }

    public long getSpawnTime() {
        return spawnTime;
    }
}
