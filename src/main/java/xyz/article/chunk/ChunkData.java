package xyz.article.chunk;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.cloudburstmc.math.vector.Vector2i;
import org.cloudburstmc.nbt.NbtMap;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.BitStorage;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.DataPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.GlobalPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.PaletteType;
import org.geysermc.mcprotocollib.protocol.data.game.level.LightUpdateData;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityInfo;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundLevelChunkWithLightPacket;

import java.util.BitSet;
import java.util.List;

public class ChunkData {
    private final Vector2i chunkPos; // 使用Vector2i(基于int的向量)来记录区块位置，因为int类型的范围已足够覆盖Minecraft的世界边界，且性能更优，Slider不需要耗费多余性能来支持官方客户端不支持的东西
    private DataPalette palette;
    private LightUpdateData lightUpdateData;
    private HeightMap heightMap;
    private BlockEntityInfo[] blockEntities;

    public ChunkData(int chunkX, int chunkZ) {
        palette = new DataPalette(GlobalPalette.INSTANCE, new BitStorage(PaletteType.CHUNK.getMaxBitsPerEntry(), 16 * 16 * 16), PaletteType.CHUNK);
        chunkPos = Vector2i.from(chunkX, chunkZ); // 使用Vector2i来表示区块的X和Z坐标。在Minecraft中，Y坐标表示高度，而区块位置仅涉及X和Z。因此，这里Vector2i的getX()对应chunkX，getY()实际表示chunkZ
        blockEntities = new BlockEntityInfo[]{};
        heightMap = new HeightMap();
        lightUpdateData = new LightUpdateData(new BitSet(), new BitSet(), new BitSet(), new BitSet(), List.of(), List.of());
    }
    public ChunkData(int chunkX, int chunkZ, DataPalette palette, HeightMap heightMap, BlockEntityInfo[] blockEntities, LightUpdateData lightUpdateData) {
        this.palette = palette;
        chunkPos = Vector2i.from(chunkX, chunkZ);
        this.blockEntities = blockEntities;
        this.heightMap = heightMap;
        this.lightUpdateData = lightUpdateData;
    }

    // Getter/Setters
    public DataPalette getPalette() {
        return palette;
    }

    public void setPalette(DataPalette palette) {
        this.palette = palette;
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

    public ClientboundLevelChunkWithLightPacket getChunkPacket() { // 用于获取此区块构建成的区块包
        ByteBuf byteBuf = Unpooled.buffer();
        MinecraftCodecHelper helper = new MinecraftCodecHelper();
        helper.writeDataPalette(byteBuf, palette);
        return new ClientboundLevelChunkWithLightPacket(
                chunkPos.getX(),
                chunkPos.getY(),
                byteBuf.array(),
                heightMap.toNbt(),
                blockEntities,
                lightUpdateData
        );
    }
}
