package xyz.article.packetProcessors;

import org.cloudburstmc.math.vector.Vector2f;
import org.cloudburstmc.math.vector.Vector2i;
import org.cloudburstmc.math.vector.Vector3d;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.entity.Effect;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundMoveEntityPosRotPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundRotateHeadPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundUpdateMobEffectPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosRotPacket;
import xyz.article.MinecraftServer;
import xyz.article.api.Location;
import xyz.article.api.Slider;
import xyz.article.api.interfaces.PacketProcessor;
import xyz.article.api.entity.player.Player;

import java.util.Random;

public class MovePlayerPosRotPacketProcessor implements PacketProcessor {
    @Override
    public void process(Packet packet, Session session) {
        if (packet instanceof ServerboundMovePlayerPosRotPacket posRotPacket) {
            Player player = Slider.getPlayer(session);
            if (player != null) {
                double moveX = posRotPacket.getX() - player.getLocation().pos().getX();
                double moveY = posRotPacket.getY() - player.getLocation().pos().getY();
                double moveZ = posRotPacket.getZ() - player.getLocation().pos().getZ();

                float newYaw = posRotPacket.getYaw();
                float newPitch = posRotPacket.getPitch();

                player.setLocation(new Location(player.getWorld(), Vector3d.from(posRotPacket.getX(), posRotPacket.getY(), posRotPacket.getZ())));
                player.setAngle(Vector2f.from(newYaw, newPitch));

                for (Session session1 : MinecraftServer.playerSessions) {
                    if (!session1.equals(session)) {
                        session1.send(new ClientboundMoveEntityPosRotPacket(player.getEntityID(), moveX, moveY, moveZ, posRotPacket.getYaw(), posRotPacket.getPitch(), posRotPacket.isOnGround()));
                        session1.send(new ClientboundRotateHeadPacket(player.getEntityID(), newYaw));
                    }
                }

                if (posRotPacket.getY() < -400) {
                    session.send(new ClientboundUpdateMobEffectPacket(player.getEntityID(), Effect.BLINDNESS, 255, 30, true, false, false, false));
                    session.send(new ClientboundPlayerPositionPacket(posRotPacket.getX(), 1000d, posRotPacket.getZ(), player.getAngle().getX(), player.getAngle().getY(), new Random().nextInt()));
                }
                if (posRotPacket.getY() > 1000) {
                    session.send(new ClientboundUpdateMobEffectPacket(player.getEntityID(), Effect.BLINDNESS, 255, 30, true, false, false, false));
                    session.send(new ClientboundPlayerPositionPacket(posRotPacket.getX(), -400d, posRotPacket.getZ(), player.getAngle().getX(), player.getAngle().getY(), new Random().nextInt()));
                }
            }
        }
    }
}