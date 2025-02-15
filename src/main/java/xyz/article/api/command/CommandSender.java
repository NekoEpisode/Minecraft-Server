package xyz.article.api.command;

import net.kyori.adventure.text.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.api.entity.player.Player;

import java.util.UUID;

public class CommandSender {
    private static final Logger log = LoggerFactory.getLogger(CommandSender.class);
    private final String name;
    private final UUID uuid;
    private final Player player;
    private final boolean isConsole;

    public CommandSender() {
        this.name = "Console";
        this.uuid = UUID.randomUUID();
        this.player = null;
        this.isConsole = true;
    }
    public CommandSender(Player player) {
        this.name = player.getProfile().getName();
        this.uuid = player.getProfile().getId();
        this.player = player;
        this.isConsole = false;
    }

    public void sendMessage(Component message) {
        if (!isConsole) {
            player.sendMessage(message);
        }else {
            log.info(message.insertion());
        }
    }

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }
}
