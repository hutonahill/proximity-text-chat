package com.proxtextchat.network;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Set;

public class NetworkNode {
    private WorldChunk Location;

    private String Channel;

    private Set<WorldChunk> Chunks;

    public NetworkNode(String channel, WorldChunk location, Set<WorldChunk> chunks, RegistryKey<World> dimension){
        Channel = channel;
        Location = location;
        Chunks = chunks;
    }

    public WorldChunk getLocation() {
        return Location;
    }

    public String getChannel() {
        return Channel;
    }

    public Set<WorldChunk> getChunks() {
        return Chunks;
    }

}


