package xyz.article.packetProcessors;

import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.MinecraftServer;
import xyz.article.api.Slider;
import xyz.article.api.interfaces.PacketProcessor;

import java.util.Objects;

public class ChatPacketProcessor implements PacketProcessor {
    private static final Logger log = LoggerFactory.getLogger(ChatPacketProcessor.class);

    @Override
    public void process(Packet packet, Session session) {
        if (packet instanceof ServerboundChatPacket chatPacket) {
            if (chatPacket.getMessage().startsWith(".time")) {
                String[] strings = chatPacket.getMessage().split(" ");
                if (strings.length < 2) {
                    return;
                }

                Objects.requireNonNull(Slider.getPlayer(session)).getWorld().setWorldTime(Integer.parseInt(strings[1]));
                return;
            }
            GameProfile profile = session.getFlag(MinecraftConstants.PROFILE_KEY);
            log.info("{}: {}", profile.getName(), chatPacket.getMessage());
            Component msg = Component.text("<" + profile.getName() + "> " + chatPacket.getMessage());
            for (Session session1 : MinecraftServer.playerSessions) {
                session1.send(new ClientboundSystemChatPacket(msg, false));
            }
        }
    }
}
