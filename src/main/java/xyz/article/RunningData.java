package xyz.article;

import xyz.article.api.player.Player;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RunningData {
    public static List<Player> playerList = new CopyOnWriteArrayList<>();
}
