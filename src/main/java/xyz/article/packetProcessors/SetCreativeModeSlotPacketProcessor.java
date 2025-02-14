package xyz.article.packetProcessors;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetContentPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory.ServerboundSetCreativeModeSlotPacket;
import xyz.article.api.Slider;
import xyz.article.api.interfaces.PacketProcessor;
import xyz.article.api.inventory.Inventory;
import xyz.article.api.player.Player;

public class SetCreativeModeSlotPacketProcessor implements PacketProcessor {
    @Override
    public void process(Packet packet, Session session) {
        if (packet instanceof ServerboundSetCreativeModeSlotPacket creativeModeSlotPacket) {
            ItemStack itemStack = creativeModeSlotPacket.getClickedItem();
            int slot = creativeModeSlotPacket.getSlot() - 9;
            Player player = Slider.getPlayer(session);
            if (player != null) {
                Inventory inventory = player.getInventory();
                inventory.setItem(slot, itemStack);
                session.send(new ClientboundContainerSetContentPacket(player.getInventory().getContainerId(), 0, player.getInventory().getItems().toArray(new ItemStack[0]), null));
            }
        }
    }
}
