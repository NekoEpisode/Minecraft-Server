package xyz.article.api.world;

import net.kyori.adventure.key.Key;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorldManager {
    public static Map<Key, World> worldMap = new ConcurrentHashMap<>();

    /*
    注册世界应在Register类中使用'registerWorld'方法注册
    此外，在服务器向客户端发送World信息之前，所有的世界应当全部注册完毕
     */
}
