package xyz.article.packetProcessors;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.entity.EquipmentSlot;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.Equipment;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundSetEquipmentPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundSetCarriedItemPacket;
import xyz.article.MinecraftServer;
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

                for (Session session1 : MinecraftServer.playerSessions) {
                    if (!(session1.equals(session))) {
                        session1.send(new ClientboundSetEquipmentPacket(player.getEntityID(), new Equipment[]{new Equipment(EquipmentSlot.MAIN_HAND, player.getMainHand().getCurrentItem())}));
                    }
                }
            }
        }
    }
}
