package xyz.article.packetProcessors;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerClosePacket;
import xyz.article.api.Slider;
import xyz.article.api.entity.player.Player;
import xyz.article.api.interfaces.PacketProcessor;
import xyz.article.api.inventory.Inventory;

public class ContainerClosePacketProcessor implements PacketProcessor {
    @Override
    public void process(Packet packet, Session session) {
        if (packet instanceof ClientboundContainerClosePacket closePacket) {
            if (closePacket.getContainerId() == 0) {
                Player player = Slider.getPlayer(session);
                if (player != null) {
                    Inventory inventory = player.getInventory();
                    if (inventory.getItem(0) != null) {

                    }
                    if (inventory.getItem(1) != null) {

                    }
                    if (inventory.getItem(2) != null) {

                    }
                    if (inventory.getItem(3) != null) {

                    }
                    if (inventory.getItem(4) != null) {

                    }
                }
            }
        }
    }
}
