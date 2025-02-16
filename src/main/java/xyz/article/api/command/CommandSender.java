package xyz.article.api.command;

import net.kyori.adventure.text.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class CommandSender {
    private static final Logger log = LoggerFactory.getLogger(CommandSender.class);
    private final String name;
    private final UUID uuid;

    public CommandSender(String name) {
        this.name = name;
        this.uuid = UUID.randomUUID();
    }
    public CommandSender() {
        this.name = "CommandSender";
        this.uuid = UUID.randomUUID();
    }

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void sendMessage(String message) {
        log.info(message);
    }
}
