package xyz.article;

import xyz.article.api.command.CommandExecutor;
import xyz.article.api.interfaces.PacketProcessor;
import xyz.article.packetProcessors.*;
import xyz.article.commands.executors.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Register {
    private static final List<PacketProcessor> packetProcessors = new CopyOnWriteArrayList<>();
    private static final Map<String, CommandExecutor> commandExecutors = new ConcurrentHashMap<>();

    public static void registerPacketProcessor(PacketProcessor processor) {
        packetProcessors.add(processor);
    }

    public static void registerCommand(String name, CommandExecutor commandExecutor) {
        commandExecutors.put(name, commandExecutor);
    }

    public static List<PacketProcessor> getPacketProcessors() {
        return packetProcessors;
    }

    public static Map<String, CommandExecutor> getCommandExecutors() {
        return commandExecutors;
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
        
        registerCommand("Test", new TestCommand());
    }

    protected static void destroy() {
        packetProcessors.clear();
        commandExecutors.clear();
    }
}
