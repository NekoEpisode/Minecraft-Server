package xyz.article;

import xyz.article.api.world.World;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RunningData {
    public static List<World> worldList = new CopyOnWriteArrayList<>();
}
