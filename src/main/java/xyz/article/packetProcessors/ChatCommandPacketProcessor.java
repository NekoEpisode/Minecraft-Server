package xyz.article.packetProcessors;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatCommandPacket;
import xyz.article.Register;
import xyz.article.api.Slider;
import xyz.article.api.entity.player.Player;
import xyz.article.api.interfaces.PacketProcessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatCommandPacketProcessor implements PacketProcessor {
    @Override
    public void process(Packet packet, Session session) {
        if (packet instanceof ServerboundChatCommandPacket commandPacket) {
            Player player = Slider.getPlayer(session);
            String[] commands = commandPacket.getCommand().split(" ");
            List<String> args = new ArrayList<>(Arrays.asList(commands).subList(1, commands.length));
            if (player != null) {
                Register.getCommandExecutors().forEach((name, executor) -> {
                    if (name.equalsIgnoreCase(commands[0])) {
                        executor.execute(player, args.toArray(new String[0]));
                    }
                });
            }
        }
    }
}
