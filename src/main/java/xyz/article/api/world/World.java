package xyz.article.api.world;

import net.kyori.adventure.key.Key;
import org.cloudburstmc.math.vector.Vector2i;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.api.entity.Entity;
import xyz.article.api.world.chunk.ChunkData;

import java.util.*;
import java.util.concurrent.*;

public class World {
    private static final Logger log = LoggerFactory.getLogger(World.class);
    private Map<Vector2i, ChunkData> chunkDataMap = new ConcurrentHashMap<>();
    private final List<Entity> entities = new CopyOnWriteArrayList<>();
    private final List<Session> sessions = new CopyOnWriteArrayList<>();
    private final Key key;

    private static final int TPS = 20; // 目标TPS
    private static final long TICK_INTERVAL = 1000 / TPS; // 每次 tick 的时间间隔(ms)
    private final ScheduledExecutorService scheduler;

    protected int worldTime = 0;
    private final WorldTick worldTick;

    public World(Key key) {
        log.info("正在初始化世界 {}", key);
        WorldManager.worldMap.put(key, this);
        this.key = key;
        this.worldTick = new WorldTick(this);

        this.scheduler = Executors.newScheduledThreadPool(1); // 开启线程池处理Tick
        startTicking();
    }

    /**
     * 启动 Tick 循环
     */
    private void startTicking() {
        log.info("世界 {} 初始化完成", key);
        scheduler.scheduleAtFixedRate(worldTick::tick, 0, TICK_INTERVAL, TimeUnit.MILLISECONDS);
    }

    /**
     * 停止 Tick 循环
     */
    public void stop() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }

        save();
    }

    public synchronized void setChunkMap(Map<Vector2i, ChunkData> chunkDataMap) {
        this.chunkDataMap = chunkDataMap;
    }

    public synchronized Map<Vector2i, ChunkData> getChunkDataMap() {
        return chunkDataMap;
    }

    public synchronized Key getKey() {
        return key;
    }

    public synchronized List<Session> getSessions() {
        return sessions;
    }

    public synchronized void addSession(Session session) {
        sessions.add(session);
    }

    public synchronized void removeSession(Session session) {
        sessions.remove(session);
    }

    public synchronized List<Entity> getEntities() {
        return entities;
    }

    /**
     * 获取TPS
     *
     * @return index[0] 1s, [1] 5s, [2] 15s, [3] 1m, [4] 5m, [5] 15m
     */
    public synchronized List<Double> getTPS() {
        return worldTick.getTPS();
    }

    /**
     * 设置世界时间
     * @param worldTime 世界时间
     */
    public synchronized void setWorldTime(int worldTime) {
        this.worldTime = worldTime;
    }

    public synchronized int getWorldTime() {
        return worldTime;
    }

    public synchronized void setBlock(int x, int y, int z, int blockID) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        ChunkData chunk = chunkDataMap.get(Vector2i.from(chunkX, chunkZ));
        if (chunk != null) {
            int sectionHeight = 16;
            int worldBottom = -64;
            int sectionIndex = (y - worldBottom) / sectionHeight;
            ChunkSection[] chunkSections = chunk.getChunkSections();
            int localX = x & 15;
            int localY = y & 15;
            int localZ = z & 15;
            chunkSections[sectionIndex].setBlock(localX, localY, localZ, blockID);
            chunk.setChunkSections(chunkSections);
            chunkDataMap.put(chunk.getChunkPos().pos(), chunk);
        } else {
            log.error("Chunk {}, {} is null!", chunkX, chunkZ);
        }
    }

    public synchronized void save() {
        log.info("正在保存世界 {}", key);
    }
}