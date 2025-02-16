package xyz.article.commands.executors;

import net.kyori.adventure.text.Component;

import xyz.article.api.entity.player.Player;
import xyz.article.api.command.CommandExecutor;
import xyz.article.api.command.CommandSender;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;

public class TestCommand implements CommandExecutor {
    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if (commandSender instanceof Player player) {
            player.sendPacket(new ClientboundSystemChatPacket(Component.text("Test!"), false));
        }
    }
}
