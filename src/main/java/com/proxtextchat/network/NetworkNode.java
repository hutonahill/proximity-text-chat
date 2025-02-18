package com.proxtextchat.network;


import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A node in a channel network
 */
public class NetworkNode {

    private static final List<Integer> idCounter = new ArrayList<>();
    private final int ID;
    private static final String IDKey ="ID";

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
    private final WorldChunkSetNbt ReceivingChunks;
    private static final String ReceivingKey = "Location";

    private final Identifier ChannelId;
    private static final String ChannelKey = "Channel";

    private final WorldChunkSetNbt Range;
    private static final String RangeKey = "Range";


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
        ReceivingChunks = new WorldChunkSetNbt(receivingChunks, ReceivingKey);
        Range = new WorldChunkSetNbt(rangeChunks, RangeKey);

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
        ReceivingChunks = new WorldChunkSetNbt(receivingChunks, ReceivingKey);
        Range = new WorldChunkSetNbt(chunks, RangeKey);
    }

    /**
     * For extracting a NetworkNode from NBT data.
     * @param nbt The NBT command containing the data.
     * @param server The server the Chunks reside in.
     */
    public NetworkNode(@NotNull NbtCompound nbt, MinecraftServer server){
        Range = new WorldChunkSetNbt(nbt, RangeKey, server);
        ReceivingChunks = new WorldChunkSetNbt(nbt, ReceivingKey, server);
        ChannelId = Identifier.of(nbt.getString(ChannelKey));
        ID = nbt.getInt(IDKey);
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


    /**
     * @param nbt the NBT command you wish to add the NetworkNode to
     * @return the NBT command with the networkNode added
     */
    public NbtCompound toNbt(NbtCompound nbt){

        ReceivingChunks.toNBT(nbt);
        nbt.putString(ChannelKey, ChannelId.toString());
        Range.toNBT(nbt);

        nbt.putInt(IDKey, ID);
        return nbt;

    }
}


