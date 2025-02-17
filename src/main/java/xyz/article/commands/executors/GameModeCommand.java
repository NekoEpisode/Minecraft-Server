package xyz.article.commands.executors;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundGameEventPacket;
import xyz.article.api.Slider;
import xyz.article.api.command.CommandExecutor;
import xyz.article.api.command.CommandSender;
import xyz.article.api.entity.player.Player;

public class GameModeCommand implements CommandExecutor {
    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if (commandSender instanceof Player player) {
            if (args.length < 1) {
                player.sendMessage(Component.text("用法：/gamemode <游戏模式> [玩家]").color(NamedTextColor.RED));
                return;
            }

            Player target = player;
            if (args.length > 1) {
                String name = args[1];
                Player player1 = Slider.getPlayer(name);
                if (player1 != null) {
                    target = player1;
                } else {
                    player.sendMessage(Component.text("此玩家不存在！").color(NamedTextColor.RED));
                    return;
                }
            }

            GameMode gameMode;
            try {
                float number = Float.parseFloat(args[0]);
                if (number < 0 || number > 3) {
                    player.sendMessage(Component.text("此游戏模式不存在！").color(NamedTextColor.RED));
                    return;
                }
                gameMode = GameMode.byId((int) number);
            } catch (NumberFormatException ignored) {
                try {
                    gameMode = GameMode.valueOf(args[0].toUpperCase());
                } catch (IllegalArgumentException ignored1) {
                    player.sendMessage(Component.text("此游戏模式不存在！").color(NamedTextColor.RED));
                    return;
                }
            }

            ByteBuf buf = Unpooled.buffer();
            MinecraftCodecHelper codecHelper = new MinecraftCodecHelper();
            buf.writeByte(3);
            buf.writeFloat(getGameModeId(gameMode));
            target.sendPacket(new ClientboundGameEventPacket(buf, codecHelper));
            target.setGameMode(gameMode);
            target.sendMessage(Component.text("您的游戏模式已被设为" + getFriendlyGameModeName(gameMode) + "模式").color(NamedTextColor.GREEN));
        } else {
            if (args.length < 2) {
                commandSender.sendMessage("用法：/gamemode <游戏模式> <玩家>");
                return;
            }
            Player target = Slider.getPlayer(args[1]);
            if (target != null) {
                GameMode gameMode;
                try {
                    float number = Float.parseFloat(args[0]);
                    if (number < 0 || number > 3) {
                        commandSender.sendMessage("此游戏模式不存在！");
                        return;
                    }
                    gameMode = GameMode.byId((int) number);
                } catch (NumberFormatException ignored) {
                    try {
                        gameMode = GameMode.valueOf(args[0].toUpperCase());
                    } catch (IllegalArgumentException ignored1) {
                        commandSender.sendMessage("此游戏模式不存在！");
                        return;
                    }
                }

                ByteBuf buf = Unpooled.buffer();
                MinecraftCodecHelper codecHelper = new MinecraftCodecHelper();
                buf.writeByte(3);
                buf.writeFloat(getGameModeId(gameMode));
                target.sendPacket(new ClientboundGameEventPacket(buf, codecHelper));
                target.setGameMode(gameMode);
                target.sendMessage(Component.text("您的游戏模式已被设为" + getFriendlyGameModeName(gameMode) + "模式").color(NamedTextColor.GREEN));
                commandSender.sendMessage("已将 " + target.getName() + " 的游戏模式设置为" + getFriendlyGameModeName(gameMode) + "模式");
            }
        }
    }

    private String getFriendlyGameModeName(GameMode gameMode) {
        return switch (gameMode) {
            case SURVIVAL -> "生存";
            case CREATIVE -> "创造";
            case ADVENTURE -> "冒险";
            case SPECTATOR -> "旁观";
        };
    }

    private float getGameModeId(GameMode gameMode) {
        return switch (gameMode) {
            case SURVIVAL -> 0f;
            case CREATIVE -> 1f;
            case ADVENTURE -> 2f;
            case SPECTATOR -> 3f;
        };
    }
}
