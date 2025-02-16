package xyz.article;

import xyz.article.api.interfaces.PacketProcessor;
import xyz.article.packetProcessors.*;

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
        registerPacketProcessor(new MovePlayerPosPacketProcessor());
        registerPacketProcessor(new MovePlayerPosRotPacketProcessor());
        registerPacketProcessor(new MovePlayerRotPacketProcessor());
        registerPacketProcessor(new SetCarriedItemPacketProcessor());
        registerPacketProcessor(new SetCreativeModeSlotPacketProcessor());
        registerPacketProcessor(new ChatCommandPacketProcessor());
    }
}
