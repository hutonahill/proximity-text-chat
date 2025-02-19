package com.proxtextchat.network;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

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
    private final ChunkReferenceSet ReceivingChunks;
    private static final String ReceivingKey = "Location";

    private final Identifier ChannelId;
    private static final String ChannelKey = "Channel";

    private final ChunkReferenceSet RangeChunks;
    private static final String RangeKey = "Range";


    public static final Codec<NetworkNode> CODEC;

    static{
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf(ChannelKey).forGetter(NetworkNode::getChannelId),
                ChunkReferenceSet.CODEC.fieldOf(ReceivingKey).forGetter(NetworkNode::getReceivingSet),
                ChunkReferenceSet.CODEC.fieldOf(RangeKey).forGetter(NetworkNode::getRangeSet),
                Codec.INT.fieldOf(IDKey).forGetter(NetworkNode::getID)

        ).apply(instance, NetworkNode::new));
    }

    private ChunkReferenceSet getReceivingSet(){
        return ReceivingChunks;
    }

    private ChunkReferenceSet getRangeSet(){
        return RangeChunks;
    }

    /**
     * Used when generating a NetworkNode based on NBT data
     * @param channel  The channel the node is a member of
     * @param receivingChunks a set of chunks the node can receive chunks from.
     * @param chunks a set of chunks the node can send messages to.
     * @param id A manually set ID value. ID must be unique.
     */
    public NetworkNode(Identifier channel, Collection<ChunkReferance> receivingChunks, Collection<ChunkReferance> chunks, Integer id){
        if (idCounter.contains(id)){
            throw new IllegalArgumentException("id `" + id + "` has already been assigned");
        }

        ID = id;
        idCounter.add(id);

        ChannelId = channel;
        ReceivingChunks = new ChunkReferenceSet(receivingChunks);
        RangeChunks = new ChunkReferenceSet(chunks);
    }



    /**
     * @param channel The channel the node is a member of
     * @param receivingChunks a collection of chunks the node can receive chunks from.
     * @param rangeChunks a collection of chunks the node can send messages to.
     */
    public NetworkNode(Identifier channel, Collection<ChunkReferance> receivingChunks, Collection<ChunkReferance> rangeChunks){
        this(channel, new HashSet<>(receivingChunks), new HashSet<>(rangeChunks));
    }

    /**
     * @param channel The channel the node is a member of
     * @param receivingChunks a set of chunks the node can receive chunks from.
     * @param rangeChunks a set of chunks the node can send messages to.
     */
    public NetworkNode(Identifier channel, Set<ChunkReferance> receivingChunks, Set<ChunkReferance> rangeChunks){
        if (receivingChunks.isEmpty() && rangeChunks.isEmpty()){
            throw new IllegalArgumentException("Must have at least one receiving or range chunk.");
        }

        ChannelId = channel;
        ReceivingChunks = new ChunkReferenceSet(receivingChunks);
        RangeChunks = new ChunkReferenceSet(rangeChunks);

        ID = generateId();
    }





    public Set<ChunkReferance> getReceivingChunks() {
        return Collections.unmodifiableSet(ReceivingChunks);
    }

    public Identifier getChannelId() {
        return ChannelId;
    }

    public Set<ChunkReferance> getRangeChunks() {
        return Collections.unmodifiableSet(RangeChunks);
    }

    public int getID(){ return ID;}

}


