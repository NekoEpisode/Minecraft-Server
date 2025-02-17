package xyz.article.api.entity;

import org.geysermc.mcprotocollib.protocol.data.game.entity.type.EntityType;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import xyz.article.api.Location;

public class ItemEntity extends Entity {
    private final ItemStack itemStack;

    public ItemEntity(Location location, EntityType entityType, int entityId, ItemStack itemStack) {
        super(location, entityType, entityId);
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
