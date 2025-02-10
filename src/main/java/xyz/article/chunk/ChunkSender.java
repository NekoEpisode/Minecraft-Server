package xyz.article.chunk;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.cloudburstmc.nbt.NbtMap;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.BitStorage;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.DataPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.GlobalPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.PaletteType;
import org.geysermc.mcprotocollib.protocol.data.game.level.LightUpdateData;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityInfo;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundLevelChunkWithLightPacket;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.network.Session;

import java.util.BitSet;
import java.util.List;

public class ChunkSender {

    public static void sendGrassChunk(Session session, int chunkX, int chunkZ, MinecraftCodecHelper helper) {
        byte[] chunkData = createChunkData(helper);

        LightUpdateData lightData = new LightUpdateData(new BitSet(), new BitSet(), new BitSet(), new BitSet(), List.of(new byte[]{(byte) 2048}), List.of(new byte[]{(byte) 2048}));

        ClientboundLevelChunkWithLightPacket chunkPacket = new ClientboundLevelChunkWithLightPacket(
                chunkX, chunkZ, chunkData, NbtMap.EMPTY, new BlockEntityInfo[]{}, lightData
        );

        session.send(chunkPacket);
    }

    public static byte[] createChunkData(MinecraftCodecHelper helper) {
        ByteBuf buf = Unpooled.buffer();

        int grassBlockId = 9;

        int bitsPerBlock = 8;

        DataPalette palette = new DataPalette(GlobalPalette.INSTANCE, new BitStorage(bitsPerBlock, 16 * 16 * 16), PaletteType.CHUNK);

        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    palette.set(x, y, z, grassBlockId);
                }
            }
        }

        helper.writeDataPalette(buf, palette);

        return buf.array();
    }
}