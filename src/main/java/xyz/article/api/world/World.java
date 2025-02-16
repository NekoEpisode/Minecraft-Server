package xyz.article.api.world;

import net.kyori.adventure.key.Key;
import org.cloudburstmc.math.vector.Vector2i;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.api.world.chunk.ChunkData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.util.LinkedList;
import java.util.Queue;

public class World {
    private static final Logger log = LoggerFactory.getLogger(World.class);
    private Map<Vector2i, ChunkData> chunkDataMap = new ConcurrentHashMap<>();
    private final Key key;

    private static final int TPS = 20; // 每秒 20 次 tick
    private static final long TICK_INTERVAL = 1000 / TPS; // 每次 tick 的时间间隔(ms)
    private final ScheduledExecutorService scheduler;
    private int tickCounter = 0;

    // 用于记录 tick 时间
    private final Queue<Long> tickTimes = new LinkedList<>();

    public World(Key key) {
        WorldManager.worldMap.put(key, this);
        this.key = key;

        this.scheduler = Executors.newScheduledThreadPool(1);
        startTicking();
    }

    /**
     * 启动 Tick 循环
     */
    private void startTicking() {
        scheduler.scheduleAtFixedRate(this::tick, 0, TICK_INTERVAL, TimeUnit.MILLISECONDS);
    }

    private void tick() {
        long currentTime = System.currentTimeMillis();

        // 记录当前 tick 的时间
        tickTimes.add(currentTime);

        // 清理超过 15 分钟的记录
        while (!tickTimes.isEmpty() && currentTime - tickTimes.peek() > 15 * 60 * 1000) {
            tickTimes.poll();
        }

        // 每 10 秒输出一次 TPS 信息
        tickCounter++;
        if (tickCounter >= TPS * 10) { // 10 秒的 tick 次数
            log.info("1s TPS: {}, 1m TPS: {}, 5m TPS: {}, 15m TPS: {}",
                    calculateTPS(1),
                    calculateTPS(60),
                    calculateTPS(5 * 60),
                    calculateTPS(15 * 60));
            tickCounter = 0;
        }
    }

    /**
     * 计算指定时间范围内的 TPS
     *
     * @param timeRangeInSeconds 时间范围（秒）
     * @return TPS
     */
    private double calculateTPS(int timeRangeInSeconds) {
        long currentTime = System.currentTimeMillis();
        long startTime = currentTime - timeRangeInSeconds * 1000L;

        // 统计在时间范围内的 tick 次数
        int tickCount = 0;
        for (long tickTime : tickTimes) {
            if (tickTime >= startTime) {
                tickCount++;
            }
        }

        return (double) tickCount / timeRangeInSeconds;
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
    }

    public void setChunkMap(Map<Vector2i, ChunkData> chunkDataMap) {
        this.chunkDataMap = chunkDataMap;
    }

    public Map<Vector2i, ChunkData> getChunkDataMap() {
        return chunkDataMap;
    }

    public Key getKey() {
        return key;
    }

    public void setBlock(int x, int y, int z, int blockID) {
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

    public void save() {
        log.info("正在保存世界 {}", key);
    }
}