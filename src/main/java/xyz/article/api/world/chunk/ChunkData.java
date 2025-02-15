package xyz.article.api.world.chunk;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.BitStorage;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.DataPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.GlobalPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.PaletteType;
import org.geysermc.mcprotocollib.protocol.data.game.level.LightUpdateData;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityInfo;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundLevelChunkWithLightPacket;

import java.util.BitSet;
import java.util.List;

public class ChunkData {
    private final ChunkPos chunkPos;
    private ChunkSection[] chunkSections = new ChunkSection[24];
    private LightUpdateData lightUpdateData;
    private HeightMap heightMap;
    private BlockEntityInfo[] blockEntities;

    public ChunkData(ChunkPos chunkPos) {
        for (int i = 0; i < 24; i++) {
            chunkSections[i] = new ChunkSection(0, new DataPalette(GlobalPalette.INSTANCE, new BitStorage(PaletteType.CHUNK.getMaxBitsPerEntry(), 16 * 16 * 16), PaletteType.CHUNK), new DataPalette(GlobalPalette.INSTANCE, new BitStorage(16, 4 * 4 * 4), PaletteType.BIOME));
        }
        this.chunkPos = chunkPos; // 使用Vector2i来表示区块的X和Z坐标。在Minecraft中，Y坐标表示高度，而区块位置仅涉及X和Z。因此，这里Vector2i的getX()对应chunkX，getY()实际表示chunkZ
        blockEntities = new BlockEntityInfo[]{};
        heightMap = new HeightMap();
        lightUpdateData = new LightUpdateData(new BitSet(), new BitSet(), new BitSet(), new BitSet(), List.of(), List.of());

        chunkPos.world().getChunkDataMap().put(chunkPos.pos(), this);
    }
    public ChunkData(ChunkPos chunkPos, ChunkSection[] chunkSections, HeightMap heightMap, BlockEntityInfo[] blockEntities, LightUpdateData lightUpdateData) {
        this.chunkSections = chunkSections;
        this.chunkPos = chunkPos;
        this.blockEntities = blockEntities;
        this.heightMap = heightMap;
        this.lightUpdateData = lightUpdateData;

        chunkPos.world().getChunkDataMap().put(chunkPos.pos(), this);
    }

    // Getter/Setters
    public ChunkSection[] getChunkSections() {
        return chunkSections;
    }

    public void setChunkSections(ChunkSection[] chunkSections) {
        this.chunkSections = chunkSections;
    }

    public LightUpdateData getLightUpdateData() {
        return lightUpdateData;
    }

    public void setLightUpdateData(LightUpdateData lightUpdateData) {
        this.lightUpdateData = lightUpdateData;
    }

    public HeightMap getHeightMap() {
        return heightMap;
    }

    public void setHeightMap(HeightMap heightMap) {
        this.heightMap = heightMap;
    }

    public BlockEntityInfo[] getBlockEntities() {
        return blockEntities;
    }

    public void setBlockEntities(BlockEntityInfo[] blockEntities) {
        this.blockEntities = blockEntities;
    }

    public ChunkPos getChunkPos() {
        return chunkPos;
    }

    public ClientboundLevelChunkWithLightPacket getChunkPacket() { // 用于获取此区块构建成的区块包
        ByteBuf byteBuf = Unpooled.buffer();
        MinecraftCodecHelper helper = new MinecraftCodecHelper();
        for (int i = 0; i < 24; i++) {
            helper.writeChunkSection(byteBuf, chunkSections[i]);
        }
        return new ClientboundLevelChunkWithLightPacket(
                chunkPos.pos().getX(),
                chunkPos.pos().getY(),
                byteBuf.array(),
                heightMap.toNbt(),
                blockEntities,
                lightUpdateData
        );
    }
}
