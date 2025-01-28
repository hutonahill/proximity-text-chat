package com.proxtextchat.network;


import net.minecraft.util.Identifier;
import net.minecraft.world.chunk.WorldChunk;

import java.util.*;

/**
 * A node in a channel network
 */
public class NetworkNode {

    private static final List<Integer> idCounter = new ArrayList<>();
    private final int ID;

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


    /**
     * @param channel The channel the node is a member of
     * @param receivingChunks a set of chunks the node can receive chunks from.
     * @param rangeChunks a set of chunks the node can send messages to.
     */
    public NetworkNode(Identifier channel, Set<WorldChunk> receivingChunks, Set<WorldChunk> rangeChunks){
        if (receivingChunks.isEmpty()){
            throw new IllegalArgumentException("Must have at least one receiving chunk.");
        }

        ChannelId = channel;
        ReceivingChunks = receivingChunks;
        Range = rangeChunks;

        ID = generateId();
    }

    /**
     * Used when generating a NetworkNode based on NBT data
     * @param channel  The channel the node is a member of
     * @param receivingChunks a set of chunks the node can receive chunks from.
     * @param chunks a set of chunks the node can send messages to.
     * @param id A manually set ID value. ID must be unique.
     */
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


