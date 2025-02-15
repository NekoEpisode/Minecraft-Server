package xyz.article.packetProcessors;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundMoveEntityPosRotPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosRotPacket;
import xyz.article.MinecraftServer;
import xyz.article.api.Slider;
import xyz.article.api.interfaces.PacketProcessor;
import xyz.article.api.entity.player.Player;

public class MovePlayerPosRotPacketProcessor implements PacketProcessor {
    @Override
    public void process(Packet packet, Session session) {
        if (packet instanceof ServerboundMovePlayerPosRotPacket rotPacket) {
            Player player = Slider.getPlayer(session);
            for (Session session1 : MinecraftServer.playerSessions) {
                if (!session1.equals(session)) {
                    if (player != null) {
                        session1.send(new ClientboundMoveEntityPosRotPacket(player.getEntityID(), rotPacket.getX(), rotPacket.getY(), rotPacket.getZ(), rotPacket.getYaw(), rotPacket.getPitch(), rotPacket.isOnGround()));
                    }
                }
            }
        }
    }
}
