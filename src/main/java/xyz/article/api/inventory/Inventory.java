package xyz.article.api.inventory;

import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.protocol.data.game.inventory.ContainerType;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetContentPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundOpenScreenPacket;
import org.geysermc.mcprotocollib.network.Session;

import java.util.ArrayList;
import java.util.List;

public class Inventory {
    private final int size;
    private final List<ItemStack> items;
    private final ContainerType containerType;
    private final int containerId;
    private final String name;

    public Inventory(String name, ContainerType containerType, int size, int containerId) {
        this.name = name;
        this.containerType = containerType;
        this.size = size;
        this.containerId = containerId;
        this.items = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            items.add(null);
        }
    }

    public void setItem(int slot, ItemStack item) {
        if (slot < 0 || slot >= size) {
            throw new IllegalArgumentException("Slot out of bounds");
        }
        items.set(slot, item);
    }

    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= size) {
            throw new IllegalArgumentException("Slot out of bounds");
        }
        return items.get(slot);
    }

    public void sync(Session session) {
        session.send(new ClientboundContainerSetContentPacket(containerId, 0, items.toArray(new ItemStack[0]), null));
    }

    public int getSize() {
        return size;
    }

    public int getContainerId() {
        return containerId;
    }

    public ContainerType getContainerType() {
        return containerType;
    }

    public String getName() {
        return name;
    }
}