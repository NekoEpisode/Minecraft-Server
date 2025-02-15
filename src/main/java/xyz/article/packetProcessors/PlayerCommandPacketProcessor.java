package xyz.article.packetProcessors;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.entity.EntityEvent;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundEntityEventPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundPlayerCommandPacket;
import xyz.article.MinecraftServer;
import xyz.article.api.Slider;
import xyz.article.api.entity.player.Player;
import xyz.article.api.interfaces.PacketProcessor;

import java.util.Objects;

public class PlayerCommandPacketProcessor implements PacketProcessor {
    @Override
    public void process(Packet packet, Session session) {
        if (packet instanceof ServerboundPlayerCommandPacket commandPacket) {
            Player player = Slider.getPlayer(session);
            if (player != null) {
                switch (commandPacket.getState()) {
                    case START_SNEAKING -> {
                        for (Session session1 : MinecraftServer.playerSessions) {
                            if (!(session1.equals(session))) {
                            }
                        }
                    }
                }
            }
        }
    }
}
