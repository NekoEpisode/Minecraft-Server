package xyz.article.packetProcessors;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatCommandPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.Register;
import xyz.article.api.Slider;
import xyz.article.api.entity.player.Player;
import xyz.article.api.interfaces.PacketProcessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatCommandPacketProcessor implements PacketProcessor {
    private static final Logger log = LoggerFactory.getLogger(ChatCommandPacketProcessor.class);

    @Override
    public void process(Packet packet, Session session) {
        if (packet instanceof ServerboundChatCommandPacket commandPacket) {
            Player player = Slider.getPlayer(session);
            String[] commands = commandPacket.getCommand().split(" ");
            List<String> args = new ArrayList<>(Arrays.asList(commands).subList(1, commands.length));
            if (player != null) {
                final boolean[] executed = new boolean[1];
                Register.getCommandExecutors().forEach((name, executor) -> {
                    if (name.equalsIgnoreCase(commands[0])) {
                        executor.execute(player, args.toArray(new String[0]));
                        executed[0] = true;
                    }
                });

                if (!executed[0]) {
                    session.send(new ClientboundSystemChatPacket(Component.text("未知的命令！").color(NamedTextColor.RED), false));
                }

                log.info(player.getName() + " 使用了命令 /" + commandPacket.getCommand());
            }
        }
    }
}
