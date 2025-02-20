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

    /**
     * 注册数据包处理器
     * @param processor 数据包处理器实例，需实现PacketProcessor接口
     */
    public static void registerPacketProcessor(PacketProcessor processor) {
        packetProcessors.add(processor);
    }

    /**
     * 注册一个命令
     *
     * @param name 命令名称
     * @param commandExecutor 命令处理器实例，需实现CommandExecutor接口
     */
    public static void registerCommand(String name, CommandExecutor commandExecutor) {
        commandExecutors.put(name.toLowerCase(), commandExecutor);
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
        
        registerCommand("tps", new TPSCommand());
        registerCommand("gamemode", new GameModeCommand());
        registerCommand("stop", new StopCommand());
    }

    protected static void destroy() {
        packetProcessors.clear();
        commandExecutors.clear();
    }
}
