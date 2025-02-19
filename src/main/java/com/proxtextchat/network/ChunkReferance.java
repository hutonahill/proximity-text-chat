package com.proxtextchat.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

public class ChunkReferance {
    private final GlobalPos globalPos;

    private static final String Key = "GlobalPos";

    public static final Codec<ChunkReferance> CODEC;

    static{
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                GlobalPos.CODEC.fieldOf(Key).forGetter(ChunkReferance::getGlobalPos)
        ).apply(instance, ChunkReferance::new));
    }

    public ChunkReferance(RegistryKey<World> world, ChunkPos pos){
        globalPos = new GlobalPos(world, pos.getBlockPos(0,0,0));
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

    private ChunkReferance(GlobalPos pos){
        globalPos = pos;
    }

    public WorldChunk getWorldChunk(MinecraftServer server){
        return server.getWorld(globalPos.dimension()).getWorldChunk(globalPos.pos());
    }

}
