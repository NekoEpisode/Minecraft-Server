package xyz.article;

import xyz.article.api.interfaces.PacketProcessor;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import xyz.article.packetProcessors.ChatPacketProcessor;
import xyz.article.packetProcessors.PlayerActionPacketProcessor;
import xyz.article.packetProcessors.UseItemOnPacketProcessor;

import java.util.ArrayList;
import java.util.List;

public class Register {
    private static final List<PacketProcessor> packetProcessors = new ArrayList<>();

    public static void registerPacketProcessor(PacketProcessor processor) {
        packetProcessors.add(processor);
    }

    public static List<PacketProcessor> getPacketProcessors() {
        return packetProcessors;
    }

    public static void registerALL() {
        registerPacketProcessor(new ChatPacketProcessor());
        registerPacketProcessor(new PlayerActionPacketProcessor());
        registerPacketProcessor(new UseItemOnPacketProcessor());
    }
}
