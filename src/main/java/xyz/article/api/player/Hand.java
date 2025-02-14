package xyz.article.api.player;

import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;

public class Hand {
    private ItemStack currentItem;

    public Hand(int type) {
        currentItem = null;
    }

    public void setCurrentItem(ItemStack currentItem) {
        this.currentItem = currentItem;
    }

    public ItemStack getCurrentItem() {
        return currentItem;
    }
}
