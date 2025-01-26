package com.proxtextchat.network;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class NetworkNodeNBT {
    public ChunkNbtSet Receiving;
    private static final String ReceivingKey = "Location";

    public Identifier Channel;
    private static final String ChannelKey = "Channel";

    public ChunkNbtSet Range;
    private static final String RangeKey = "Range";

    public NetworkNodeNBT(@NotNull NetworkNode node){
        Receiving = new ChunkNbtSet(node.getReceivingChunks(), ReceivingKey);


        Channel = node.getChannelId();

        Set<WorldChunk> nodeChunks = node.getRange();

        Range = new ChunkNbtSet(nodeChunks, RangeKey);

    }

    public NetworkNodeNBT(@NotNull NbtCompound nbt){
        Range = new ChunkNbtSet(nbt, RangeKey);
        Receiving = new ChunkNbtSet(nbt, ReceivingKey);
        Channel = Identifier.of(nbt.getString(ChannelKey));
    }

    public NbtCompound toNbt(){
        NbtCompound nbt = new NbtCompound();

        Receiving.toNBT(nbt);
        nbt.putString(ChannelKey, Channel.toString());
        Range.toNBT(nbt);
        return nbt;

    }

    public NetworkNode toNode(@NotNull MinecraftServer server){
        Set<WorldChunk> tempRange = Range.toSet(server);

        Set<WorldChunk> tempReceiving = Receiving.toSet(server);

        // return the node
        return new NetworkNode(Channel, tempReceiving, tempRange);
    }
}
