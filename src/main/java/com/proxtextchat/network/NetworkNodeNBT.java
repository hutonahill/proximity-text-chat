package com.proxtextchat.network;

import com.proxtextchat.util.Tuple;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;

import java.util.*;

public class NetworkNodeNBT {
    public long Location;
    private static final String LocationKey = "Location";

    public String Channel;
    private static final String ChannelKey = "Channel";

    public ChunkNbtArray Chunks;
    private static final String ChunkKey = "Chunks";

    public String Dimension;
    private static final String DimensionKey = "Dimension";

    public NbtCompound toNbt(){
        NbtCompound nbt = new NbtCompound();

        nbt.putLong(LocationKey, Location);
        nbt.putString(ChannelKey, Channel);
        Chunks.toNBT(nbt);
        nbt.putString(DimensionKey, Dimension);
        return nbt;

    }

    public NetworkNodeNBT(NetworkNode node){
        Location = node.getLocation().getPos().toLong();

        Channel = node.getChannel();

        List<WorldChunk> nodeChunks = node.getChunks().stream().toList();




        int size = nodeChunks.size();

        Chunks = new ChunkNbtArray(size);

        long[] LocationArray = new long[size];
        String[] DimensionArray = new String[size];

        int index = 0;

        while(index < nodeChunks.size()){

            Chunk TargetChunk = nodeChunks.get(index);
            LocationArray[index] = TargetChunk.getPos().toLong();

            index ++;
        }

    }

    public NetworkNodeNBT(NbtCompound nbt){
        Chunks = new ChunkNbtArray(nbt);
        Location = nbt.getLong(LocationKey);
        Channel = nbt.getString(ChannelKey);
        Dimension = nbt.getString(DimensionKey);
    }

    public NetworkNode toNode(MinecraftServer server){
        Set<WorldChunk> tempChunks = new HashSet<>();

        for (Tuple<Long, String> chunk : Chunks.iterable()){
            // parse the position of the new chunk
            ChunkPos tempPos = new ChunkPos(chunk.one);

            // parse the worldKey of the new chunk
            RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(chunk.two));

            // use the world key to get the world
            World world = server.getWorld(worldKey);

            // make sure the world is not null
            if(world == null){
                throw new NullPointerException("World from worldKey `" + worldKey.getValue() + "` is null.");
            }

            // get the new chunk object out of the world
            WorldChunk tempChunk = world.getChunk(
                    ChunkSectionPos.getSectionCoord(tempPos.x),
                    ChunkSectionPos.getSectionCoord(tempPos.z)
            );

            tempChunks.add(tempChunk);
        }

        // parse the node position
        ChunkPos NodeLocation = new ChunkPos(Location);

        // Get the worldKey of the node
        RegistryKey<World> nodeWorldKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(Dimension));

        // use that worldKey to get the world
        World nodeWorld = server.getWorld(nodeWorldKey);

        if(nodeWorld == null){
            throw new NullPointerException("World from nodeWorldKey `" + nodeWorldKey.getValue() + "` is null.");
        }

        // get the node's chunk
        WorldChunk NodeChunk = nodeWorld.getChunk(
                ChunkSectionPos.getSectionCoord(NodeLocation.x),
                ChunkSectionPos.getSectionCoord(NodeLocation.z)
        );

        // return the node
        return new NetworkNode(Channel, NodeChunk, tempChunks, nodeWorldKey);
    }
}
