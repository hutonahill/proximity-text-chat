package com.proxtextchat.network;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.w3c.dom.Node;

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class NetworkNodeSetNBT extends AbstractSet<NetworkNode> {
    private final Set<NetworkNode> Nodes = new HashSet<>();

    private final String idKey;

    public static NetworkNodeSetNBT fromNbt(NbtCompound nbt, String key, MinecraftServer server){

    }

    public NetworkNodeSetNBT(Set<NetworkNode> nodes, String key){
        idKey = key;

        Nodes.addAll(nodes);
    }

    public NetworkNodeSetNBT(NbtCompound nbt, String key, MinecraftServer server) {
        idKey = key;




    }

    public void toNBT(NbtCompound nbt) {

       for(NetworkNode node : Nodes){
           nbt = node.toNbt(nbt);
       }

    }

    private String getRealKey(String key){
        if(idKey != null){
            return key+idKey;
        }
        return key;
    }

    /**
     * Returns an iterator over the elements contained in this collection.
     *
     * @return an iterator over the elements contained in this collection
     */
    @Override
    public Iterator<NetworkNode> iterator() {
        return Nodes.iterator();
    }

    @Override
    public int size() {
        return Nodes.size();
    }
}
