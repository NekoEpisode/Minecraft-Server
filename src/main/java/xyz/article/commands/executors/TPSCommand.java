package xyz.article.commands.executors;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import xyz.article.api.command.CommandExecutor;
import xyz.article.api.command.CommandSender;
import xyz.article.api.entity.player.Player;
import xyz.article.api.world.WorldManager;

import java.util.List;

public class TPSCommand implements CommandExecutor {
    @Override
    public void execute(CommandSender commandSender, String[] args) {
        WorldManager.worldMap.forEach((key, world) -> {
            if (commandSender instanceof Player player) {
                Component component = Component.text("TPS from the last ").color(NamedTextColor.GOLD).append(Component.text("1s, 5s, 15s, 1m, 5m, 15m (" + world.getKey() + ")").color(NamedTextColor.YELLOW));
                Component component1 = Component.text("");

                List<Double> tps = world.getTPS();
                for (int i = 0; i < 6; i++) {
                    double tps1 = tps.get(i);
                    component1 = component1.append(Component.text(tps1)).color(getColor(tps1));
                    if (i != 6 - 1) {
                        component1 = component1.append(Component.text(", ").color(NamedTextColor.GRAY));
                    }
                }

                player.sendMessage(component);
                player.sendMessage(component1);
            }else {
                String builder = "TPS from the last 1s, 5s, 15s, 1m, 5m, 15m (" + world.getKey() + ")";
                StringBuilder builder1 = new StringBuilder();

                List<Double> tps = world.getTPS();
                for (int i = 0; i < 6; i++) {
                    double tps1 = tps.get(i);
                    builder1.append(tps1);
                    if (i != 6 - 1) {
                        builder1.append(", ");
                    }
                }

                commandSender.sendMessage(builder);
                commandSender.sendMessage(builder1.toString());
            }
        });
    }

    private NamedTextColor getColor(double tps) {
        if ((int) tps > 19) {
            return NamedTextColor.GREEN;
        } else if ((int) tps >= 15) {
            return NamedTextColor.YELLOW;
        } else if ((int) tps < 15) {
            return NamedTextColor.RED;
        } else {
            return NamedTextColor.GRAY;
        }
    }
}
