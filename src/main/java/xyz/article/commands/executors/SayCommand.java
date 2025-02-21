package xyz.article.commands.executors;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.RunningData;
import xyz.article.api.command.CommandExecutor;
import xyz.article.api.command.CommandSender;
import xyz.article.api.entity.player.Player;

public class SayCommand implements CommandExecutor {
    private static final Logger log = LoggerFactory.getLogger(SayCommand.class);

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if (args.length < 1) {
            if (commandSender instanceof Player player) {
                player.sendMessage(Component.text("请输入消息！").color(NamedTextColor.RED));
            }else {
                commandSender.sendMessage("请输入消息！");
            }
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            stringBuilder.append(args[i]);
            if (i != args.length - 1) {
                stringBuilder.append(" ");
            }
        }
        for (Player player : RunningData.playerList) {
            player.sendMessage(Component.text("[" + commandSender.getName() + "] " + stringBuilder));
        }
        log.info("[{}] {}", commandSender.getName(), stringBuilder);
    }
}
