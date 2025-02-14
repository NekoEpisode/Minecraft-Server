package xyz.article;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WhenClose {
    private static final Logger log = LoggerFactory.getLogger(WhenClose.class);

    public WhenClose() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Thread.currentThread().setName("Shutdown Thread");
            log.info("正在关闭服务器");
            MinecraftServer.getServer().close();
        }));
    }
}
