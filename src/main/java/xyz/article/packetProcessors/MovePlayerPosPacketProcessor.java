package xyz.article.packetProcessors;

import org.cloudburstmc.math.vector.Vector3d;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundMoveEntityPosPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosPacket;
import xyz.article.MinecraftServer;
import xyz.article.api.Location;
import xyz.article.api.Slider;
import xyz.article.api.interfaces.PacketProcessor;
import xyz.article.api.entity.player.Player;

public class MovePlayerPosPacketProcessor implements PacketProcessor {
    @Override
    public void process(Packet packet, Session session) {
        if (packet instanceof ServerboundMovePlayerPosPacket playerPosPacket) {
            Player player = Slider.getPlayer(session);
            if (player != null) {
                double moveX = playerPosPacket.getX() - player.getLocation().pos().getX();
                double moveY = playerPosPacket.getY() - player.getLocation().pos().getY();
                double moveZ = playerPosPacket.getZ() - player.getLocation().pos().getZ();

                player.setLocation(new Location(player.getWorld(), Vector3d.from(playerPosPacket.getX(), playerPosPacket.getY(), playerPosPacket.getZ())));

                for (Session session1 : MinecraftServer.playerSessions) {
                    if (!session1.equals(session)) {
                        session1.send(new ClientboundMoveEntityPosPacket(player.getEntityID(), moveX, moveY, moveZ, playerPosPacket.isOnGround()));
                    }
                }
            }
        }
    }
}