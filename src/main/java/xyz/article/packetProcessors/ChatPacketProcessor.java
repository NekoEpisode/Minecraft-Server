package xyz.article.packetProcessors;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.data.game.inventory.ContainerType;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundPlayerChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.MinecraftServer;
import xyz.article.PerlinNoise;
import xyz.article.RunningData;
import xyz.article.api.Slider;
import xyz.article.api.interfaces.PacketProcessor;
import xyz.article.api.inventory.Inventory;

import java.util.Objects;

public class ChatPacketProcessor implements PacketProcessor {
    private static final Logger log = LoggerFactory.getLogger(ChatPacketProcessor.class);

    @Override
    public void process(Packet packet, Session session) {
        if (packet instanceof ServerboundChatPacket chatPacket) {
            if (chatPacket.getMessage().startsWith(".setblock")) {
                String[] messages = chatPacket.getMessage().split(" ");
                if (messages.length < 5) {
                    session.send(new ClientboundSystemChatPacket(Component.text("参数不足！").color(NamedTextColor.RED), false));
                    return;
                }
                try {
                    MinecraftServer.overworld.setBlock(
                            Integer.parseInt(messages[1]),
                            Integer.parseInt(messages[2]),
                            Integer.parseInt(messages[3]),
                            Integer.parseInt(messages[4])
                    );
                }catch (NumberFormatException e) {
                    session.send(new ClientboundSystemChatPacket(Component.text("参数不足！").color(NamedTextColor.RED), false));
                    return;
                }
                return;
            }
            if (chatPacket.getMessage().startsWith(".gen")) {
                PerlinNoise perlinNoise = new PerlinNoise(12345L);
                int startX = 0;
                int startZ = 0;
                int width = 22;
                int length = 22;
                perlinNoise.generateTerrain(startX, startZ, width, length);
                return;
            }
            if (chatPacket.getMessage().startsWith(".open")) {
                Objects.requireNonNull(Slider.getPlayer(session)).openInventory(new Inventory("Test Inventory", ContainerType.GENERIC_9X6, 54, RunningData.inventories.size()));
                return;
            }
            if (chatPacket.getMessage().startsWith(".closeServer")) {
                MinecraftServer.getServer().close();
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
