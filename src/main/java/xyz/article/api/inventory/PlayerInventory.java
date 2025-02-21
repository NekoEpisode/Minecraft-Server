package xyz.article.api.inventory;

import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;

public class PlayerInventory extends Inventory {
    private ItemStack dragging;

    public PlayerInventory(String name) {
        super(name, null, 46, 0);
    }

    public void setDragging(ItemStack dragging) {
        this.dragging = dragging;
    }

    public ItemStack getDragging() {
        return dragging;
    }
}
