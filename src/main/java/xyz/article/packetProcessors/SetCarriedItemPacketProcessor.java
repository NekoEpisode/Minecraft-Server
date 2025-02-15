package xyz.article.packetProcessors;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundSetCarriedItemPacket;
import xyz.article.api.Slider;
import xyz.article.api.interfaces.PacketProcessor;
import xyz.article.api.inventory.Inventory;
import xyz.article.api.entity.player.Player;

public class SetCarriedItemPacketProcessor implements PacketProcessor {
    @Override
    public void process(Packet packet, Session session) {
        if (packet instanceof ServerboundSetCarriedItemPacket setCarriedItemPacket) {
            int slot = setCarriedItemPacket.getSlot();
            Player player = Slider.getPlayer(session);
            if (player != null) {
                Inventory inventory = player.getInventory();
                ItemStack item = inventory.getItem(slot + 36);
                player.getMainHand().setCurrentItem(item);
                player.getMainHand().setCurrentSlot(slot);
            }
        }
    }
}
