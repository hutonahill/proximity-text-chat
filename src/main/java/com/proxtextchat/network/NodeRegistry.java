package com.proxtextchat.network;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.PersistentState;
import net.minecraft.world.chunk.Chunk;

import java.util.HashSet;

// not sure what this is for...
public class NodeRegistry extends PersistentState {

    private static final String DATA_NAME = "node_registry";
    private HashSet<NetworkNode> nodeHashSet = new HashSet<>();


    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

        Chunk value;
        return null;
    }
}
