package com.proxtextchat.network;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class NetworkNodeNBT {
    public long Location;
    private static final String LocationKey = "Location";

    public Identifier Channel;
    private static final String ChannelKey = "Channel";

    public ChunkNbtArray Chunks;
    private static final String ChunkKey = "Chunks";

    public Identifier Dimension;
    private static final String DimensionKey = "Dimension";

    public NetworkNodeNBT(@NotNull NetworkNode node){
        Location = node.getLocation().getPos().toLong();

        Dimension = node.getLocation().getWorld().getRegistryKey().getValue();

        Channel = node.getChannel();

        Set<WorldChunk> nodeChunks = node.getRange();

        Chunks = new ChunkNbtArray(nodeChunks);

    }

    public NetworkNodeNBT(@NotNull NbtCompound nbt){
        Chunks = new ChunkNbtArray(nbt);
        Location = nbt.getLong(LocationKey);
        Channel = Identifier.of(nbt.getString(ChannelKey)) ;
        Dimension = Identifier.of(nbt.getString(DimensionKey));
    }

    public NbtCompound toNbt(){
        NbtCompound nbt = new NbtCompound();

        nbt.putLong(LocationKey, Location);
        nbt.putString(ChannelKey, Channel.toString());
        Chunks.toNBT(nbt);
        nbt.putString(DimensionKey, Dimension.toString());
        return nbt;

    }

    public NetworkNode toNode(@NotNull MinecraftServer server){
        Set<WorldChunk> tempChunks = Chunks.toSet(server);

        // parse the node position
        ChunkPos NodeLocation = new ChunkPos(Location);

        // Get the worldKey of the node
        RegistryKey<World> nodeWorldKey = RegistryKey.of(RegistryKeys.WORLD, Dimension);

        // use that worldKey to get the world
        World nodeWorld = server.getWorld(nodeWorldKey);

        // would rather find a way to recover here
        if(nodeWorld == null){
            throw new NullPointerException("World from nodeWorldKey `" + nodeWorldKey.getValue() + "` is null.");
        }

        // get the node's chunk
        WorldChunk NodeChunk = nodeWorld.getChunk(NodeLocation.x, NodeLocation.z);

        // return the node
        return new NetworkNode(Channel, NodeChunk, tempChunks);
    }
}
