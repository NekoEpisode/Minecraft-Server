package xyz.article.api.inventory;

import org.geysermc.mcprotocollib.protocol.data.game.inventory.ContainerType;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;

public class Inventory {
    private final int size;
    private final ItemStack[] items;
    private final ContainerType containerType;
    private final int containerId;
    private final String name;

    public Inventory(String name, ContainerType containerType, int size, int containerId) {
        this.name = name;
        this.containerType = containerType;
        this.size = size;
        this.containerId = containerId;
        this.items = new ItemStack[size];
        for (int i = 0; i < size; i++) {
            items[i] = null;
        }
    }

    public synchronized void setItem(int slot, ItemStack item) {
        if (slot < 0 || slot >= size) {
            throw new IllegalArgumentException("Slot out of bounds");
        }
        items[slot] = item;
    }

    public synchronized ItemStack getItem(int slot) {
        if (slot < 0 || slot >= size) {
            throw new IllegalArgumentException("Slot out of bounds");
        }
        return items[slot];
    }

    public synchronized ItemStack[] getItems() {
        return items;
    }

    public synchronized int getSize() {
        return size;
    }

    public synchronized int getContainerId() {
        return containerId;
    }

    public synchronized ContainerType getContainerType() {
        return containerType;
    }

    public synchronized String getName() {
        return name;
    }
}