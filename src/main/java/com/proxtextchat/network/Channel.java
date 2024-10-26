package com.proxtextchat.network;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.*;

public class Channel {
    private final Map<Chunk, Network> Range;

    public Channel(Collection<Network> networks){
        Range = HashMap.newHashMap(networks.size() * 5);

       for (Network network : networks){
           for (Chunk chunk : network.Range){
               Range.put(chunk, network);
           }
       }
    }

    public boolean InRange(Chunk location){
        return Range.containsKey(location);
    }

    public boolean InRange(ChunkPos location, RegistryKey<World> dimension, MinecraftServer server){
        World world = server.getWorld(dimension);

        if(world == null){
            throw new NullPointerException("World from worldKey `" + dimension.getValue() + "` is null.");
        }

        return Range.containsKey(world.getChunk(location.x, location.z));
    }
}
