package xyz.article.packetProcessors;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetContentPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory.ServerboundSetCreativeModeSlotPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.api.Slider;
import xyz.article.api.interfaces.PacketProcessor;
import xyz.article.api.inventory.Inventory;
import xyz.article.api.entity.player.Player;

public class SetCreativeModeSlotPacketProcessor implements PacketProcessor {
    private static final Logger log = LoggerFactory.getLogger(SetCreativeModeSlotPacketProcessor.class);

    @Override
    public void process(Packet packet, Session session) {
        if (packet instanceof ServerboundSetCreativeModeSlotPacket creativeModeSlotPacket) {
            //log.info("{} 拿取了ID为 {} 的物品", Objects.requireNonNull(Slider.getPlayer(session)).getProfile().getName(), Objects.requireNonNull(creativeModeSlotPacket.getClickedItem()).getId());

            ItemStack itemStack = creativeModeSlotPacket.getClickedItem();
            int slot = creativeModeSlotPacket.getSlot();
            Player player = Slider.getPlayer(session);
            if (itemStack == null) {
                if (player != null) {
                    ItemStack itemStack1 = player.getInventory().getItem(slot);
                    player.getInventory().setItem(slot, null);
                    player.getInventory().setItem(46, itemStack1); // slot46保存玩家鼠标目前拖动的方块
                    session.send(new ClientboundContainerSetContentPacket(player.getInventory().getContainerId(), 0, player.getInventory().getItems(), itemStack1));
                    return;
                }
            }

            if (player != null) {
                Inventory inventory = player.getInventory();
                inventory.setItem(slot, itemStack);
                int slot1 = player.getMainHand().getCurrentSlot();
                player.getMainHand().setCurrentItem(inventory.getItem(slot1 + 36));
                session.send(new ClientboundContainerSetContentPacket(player.getInventory().getContainerId(), 0, player.getInventory().getItems(), null));
            }
        }
    }
}
