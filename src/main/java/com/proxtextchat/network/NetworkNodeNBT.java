package com.proxtextchat.network;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Tools for converting between NetworkNode and NBT data.
 */
public class NetworkNodeNBT {
    private ChunkNbtSet Receiving;
    private static final String ReceivingKey = "Location";

    private Identifier Channel;
    private static final String ChannelKey = "Channel";

    private ChunkNbtSet Range;
    private static final String RangeKey = "Range";

    private int ID;
    private static final String IDKey ="ID";

    /**
     * @param node The node to be sored as NBT data
     */
    public NetworkNodeNBT(@NotNull NetworkNode node){
        Receiving = new ChunkNbtSet(node.getReceivingChunks(), ReceivingKey);


        Channel = node.getChannelId();

        Set<WorldChunk> nodeChunks = node.getRange();

        Range = new ChunkNbtSet(nodeChunks, RangeKey);

        ID = node.getID();
    }

    /**
     * @param nbt The NBT data containing a node
     */
    public NetworkNodeNBT(@NotNull NbtCompound nbt){
        Range = new ChunkNbtSet(nbt, RangeKey);
        Receiving = new ChunkNbtSet(nbt, ReceivingKey);
        Channel = Identifier.of(nbt.getString(ChannelKey));
        ID = nbt.getInt(IDKey);
    }

    /**
     * @param nbt the NBTCompound the Network Node will be added to.
     * @return the NBTCompound passed to this method after the NetworkNode has been added.
     */
    public NbtCompound toNbt(NbtCompound nbt){

        Receiving.toNBT(nbt);
        nbt.putString(ChannelKey, Channel.toString());
        Range.toNBT(nbt);
        nbt.putInt(IDKey, ID);
        return nbt;

    }

    /**
     * @param server the server the nodes are member of.
     * @return the NetworkNode the current instance represents.
     */
    public NetworkNode toNode(@NotNull MinecraftServer server){
        Set<WorldChunk> tempRange = Range.toSet(server);

        Set<WorldChunk> tempReceiving = Receiving.toSet(server);

        // return the node
        return new NetworkNode(Channel, tempReceiving, tempRange);
    }
}
