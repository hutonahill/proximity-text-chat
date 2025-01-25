package com.proxtextchat.network;


import net.minecraft.util.Identifier;
import net.minecraft.world.chunk.WorldChunk;

import java.util.*;

public class NetworkNode {

    private static final List<Integer> idCounter = new ArrayList<>();
    private final Integer ID;

    private static Integer generateId(){

        if (idCounter.isEmpty()){
            idCounter.add(1);
            return 1;
        }

        while(true){
            Integer lastNum = idCounter.getLast();

            if(!idCounter.contains(lastNum++)){
                return lastNum;
            }
        }
    }
    private final Set<WorldChunk> ReceivingChunks;

    private final Identifier ChannelId;

    private final Set<WorldChunk> Range;


    public NetworkNode(Identifier channel, Set<WorldChunk> receivingChunks, Set<WorldChunk> rangeChunks){
        if (receivingChunks.isEmpty()){
            throw new IllegalArgumentException("Must have at least one receiving chunk.");
        }

        ChannelId = channel;
        ReceivingChunks = receivingChunks;
        Range = rangeChunks;

        ID = generateId();
    }

    public NetworkNode(Identifier channel, Set<WorldChunk> receivingChunks, Set<WorldChunk> chunks, Integer id){
        if (idCounter.contains(id)){
            throw new IllegalArgumentException("id `" + id + "` has already been assigned");
        }

        ID = id;
        idCounter.add(id);

        ChannelId = channel;
        ReceivingChunks = receivingChunks;
        Range = chunks;
    }

    public Set<WorldChunk> getReceivingChunks() {
        return Collections.unmodifiableSet(ReceivingChunks);
    }

    public Identifier getChannelId() {
        return ChannelId;
    }

    public Set<WorldChunk> getRange() {
        return Collections.unmodifiableSet(Range);
    }

    public Integer getID(){ return ID;}
}


