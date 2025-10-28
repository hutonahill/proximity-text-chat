package com.proxtextchat.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A reference to a WorldChunk.
 */
public class ChunkReference {
    private final GlobalPos globalPos;

    private static final String Key = "GlobalPos";

    /**
     * provides the necessary logic for translating
     * between in-memory objects and their external representations.
     * See <a href="https://docs.fabricmc.net/1.21/develop/codecs">Fabric Codec Docs</a>.
     */
    public static final Codec<ChunkReference> CODEC;

    static{
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                GlobalPos.CODEC.fieldOf(Key).forGetter(ChunkReference::getGlobalPos)
        ).apply(instance, ChunkReference::new));
    }

    /**
     * Creates a reference to a chunk
     * @param world The world the chunk is in
     * @param pos The position of the chunk in the world
     */
    public ChunkReference(RegistryKey<World> world, ChunkPos pos){
        globalPos = new GlobalPos(world, pos.getBlockPos(0,0,0));
    }

    /**
     * Creates a reference to a chunk
     * @param chunk the chunk you want to reference
     */
    public ChunkReference(WorldChunk chunk){
        globalPos = new GlobalPos(chunk.getWorld().getRegistryKey(), chunk.getPos().getBlockPos(0,0,0));
    }

    /**
     *
     * @param world The world the chunk is in.
     * @param pos the chunk pos of the target chunk.
     */
    public ChunkReference(ServerWorld world, ChunkPos pos){
        this(world.getRegistryKey(), pos);
    }

    public RegistryKey<World> getWorldKey(){
        return globalPos.dimension();
    }

    public ChunkPos getChunkPos(){
        return new ChunkPos(globalPos.pos());
    }

    private GlobalPos getGlobalPos(){
        return globalPos;
    }

    private ChunkReference(GlobalPos pos){
        globalPos = pos;
    }

    /**
     * Gets the WorldChunk that was referenced.
     * @param server The server the chunk is a part of.
     * @return the World Chunk that was referenced.
     */
    public @Nullable WorldChunk getWorldChunk(@NotNull MinecraftServer server){
        World world = server.getWorld(globalPos.dimension());
        if (world != null) {
            return world.getWorldChunk(globalPos.pos());
        }
        else{
            return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        // Check if comparing to itself
        if (this == obj) return true;

        // Check for null or mismatched type
        if (obj == null || getClass() != obj.getClass()) return false;

        // Cast and compare underlying GlobalPos
        ChunkReference other = (ChunkReference) obj;
        return globalPos.equals(other.globalPos);
    }

    public int hashCode() {
        return globalPos.hashCode();
    }
}
