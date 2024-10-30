package com.proxtextchat.network;

import net.minecraft.util.Identifier;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Set;

public class NetworkNode {
    private final WorldChunk Location;

    private final Identifier Channel;

    private final Set<WorldChunk> Range;

    public NetworkNode(Identifier channel, WorldChunk location, Set<WorldChunk> chunks){
        Channel = channel;
        Location = location;
        Range = chunks;
    }

    public WorldChunk getLocation() {
        return Location;
    }

    public Identifier getChannel() {
        return Channel;
    }

    public Set<WorldChunk> getRange() {
        return Range;
    }

}


