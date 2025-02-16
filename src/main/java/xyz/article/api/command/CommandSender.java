package xyz.article.api.command;

import java.util.UUID;

public class CommandSender {
    private final String name;
    private final UUID uuid;

    public CommandSender() {
        this.name = "Console";
        this.uuid = UUID.randomUUID();
    }

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }
}
