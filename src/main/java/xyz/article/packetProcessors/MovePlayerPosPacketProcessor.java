package xyz.article.packetProcessors;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundMoveEntityPosPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosPacket;
import xyz.article.MinecraftServer;
import xyz.article.api.Slider;
import xyz.article.api.interfaces.PacketProcessor;
import xyz.article.api.entity.player.Player;

public class MovePlayerPosPacketProcessor implements PacketProcessor {
    @Override
    public void process(Packet packet, Session session) {
        if (packet instanceof ServerboundMovePlayerPosPacket playerPosPacket) {
            Player player = Slider.getPlayer(session);
            for (Session session1 : MinecraftServer.playerSessions) {
                if (!session1.equals(session)) {
                    if (player != null) {
                        session1.send(new ClientboundMoveEntityPosPacket(player.getEntityID(), playerPosPacket.getX(), playerPosPacket.getY(), playerPosPacket.getZ(), playerPosPacket.isOnGround()));
                    }
                }
            }
        }
    }
}
