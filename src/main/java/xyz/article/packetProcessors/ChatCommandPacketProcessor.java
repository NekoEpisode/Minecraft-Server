package xyz.article.packetProcessors;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundGameEventPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatCommandPacket;
import xyz.article.api.Slider;
import xyz.article.api.interfaces.PacketProcessor;

import java.util.Objects;

public class ChatCommandPacketProcessor implements PacketProcessor {
    @Override
    public void process(Packet packet, Session session) {
        if (packet instanceof ServerboundChatCommandPacket commandPacket) {
            String[] commands = commandPacket.getCommand().split(" ");
            if (commands[0].equalsIgnoreCase("gamemode")) {
                if (commands.length < 2) {
                    session.send(new ClientboundSystemChatPacket(Component.text("需要更多参数！").color(NamedTextColor.RED), false));
                    return;
                }

                ByteBuf buf = Unpooled.buffer();
                MinecraftCodecHelper codecHelper = new MinecraftCodecHelper();
                float number;
                try {
                    number = Float.parseFloat(commands[1]);
                }catch (NumberFormatException e) {
                    session.send(new ClientboundSystemChatPacket(Component.text("需要数字！").color(NamedTextColor.RED), false));
                    return;
                }
                buf.writeByte(3);
                buf.writeFloat(number);
                if (number < 0 || number > 3) {
                    session.send(new ClientboundSystemChatPacket(Component.text("未知的游戏模式！").color(NamedTextColor.RED), false));
                    return;
                }
                session.send(new ClientboundGameEventPacket(buf, codecHelper));
                Objects.requireNonNull(Slider.getPlayer(session)).setGameMode(GameMode.byId((int) number));
            }
        }
    }
}
