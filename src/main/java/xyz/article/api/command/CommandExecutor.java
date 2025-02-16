package xyz.article.api.command;

public interface CommandExecutor {
    void execute(CommandSender commandSender, String[] args);
}
