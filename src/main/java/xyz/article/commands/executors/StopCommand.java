package xyz.article.commands.executors;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import xyz.article.MinecraftServer;
import xyz.article.Settings;
import xyz.article.api.command.CommandExecutor;
import xyz.article.api.command.CommandSender;
import xyz.article.api.entity.player.Player;

public class StopCommand implements CommandExecutor {
    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if (commandSender instanceof Player player) {
            if (Settings.ALLOW_PLAYER_USE_CLOSE_COMMAND) {
                MinecraftServer.stop();
            } else {
                player.sendMessage(Component.text("玩家不能执行这个命令！").color(NamedTextColor.RED));
            }
        } else {
            MinecraftServer.stop();
        }
    }
}
