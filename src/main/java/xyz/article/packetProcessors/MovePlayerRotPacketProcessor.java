package xyz.article.packetProcessors;

import org.cloudburstmc.math.vector.Vector2f;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundMoveEntityRotPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundRotateHeadPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerRotPacket;
import xyz.article.MinecraftServer;
import xyz.article.api.Slider;
import xyz.article.api.entity.player.Player;
import xyz.article.api.interfaces.PacketProcessor;

public class MovePlayerRotPacketProcessor implements PacketProcessor {
    @Override
    public void process(Packet packet, Session session) {
        if (packet instanceof ServerboundMovePlayerRotPacket rotPacket) {
            Player player = Slider.getPlayer(session);
            if (player != null) {
                float newYaw = rotPacket.getYaw();
                float newPitch = rotPacket.getPitch();

                player.setAngle(Vector2f.from(newYaw, newPitch));

                for (Session session1 : MinecraftServer.playerSessions) {
                    if (!session1.equals(session)) {
                        session1.send(new ClientboundRotateHeadPacket(player.getEntityID(), newYaw));
                        session1.send(new ClientboundMoveEntityRotPacket(player.getEntityID(), rotPacket.getYaw(), rotPacket.getPitch(), rotPacket.isOnGround()));
                    }
                }
            }
        }
    }

    private float calculateAngleDifference(float current, float newAngle) {
        float difference = newAngle - current;
        if (difference > 180) {
            difference -= 360;
        } else if (difference < -180) {
            difference += 360;
        }
        return difference;
    }
}