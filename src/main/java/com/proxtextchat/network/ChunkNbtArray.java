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

import java.util.HashSet;
import java.util.Set;

public class ChunkNbtArray{
    private final long[] location;
    private final String LocationKey = "Location";

    private final String[] dimension;
    private final String DimensionKey = "Dimension";

    // Constructor to initialize the arrays
    public ChunkNbtArray(Set<WorldChunk> chunks){
        location = new long[chunks.size()];
        dimension = new String[chunks.size()];

        int index = 0;
        for (WorldChunk chunk : chunks){
            location[index] = chunk.getPos().toLong();
            dimension[index] = chunk.getWorld().getRegistryKey().getValue().toString();

            index++;
        }
    }

    public ChunkNbtArray(NbtCompound nbt) {
        // Retrieve the long array from NBT
        location = nbt.getLongArray(LocationKey);

        // Retrieve the string list from NBT
        NbtList stringList = nbt.getList(DimensionKey, 8);
        dimension = new String[stringList.size()];

        // Populate the dimension array
        for (int i = 0; i < stringList.size(); i++) {
            dimension[i] = stringList.getString(i);
        }
    }

    // Set a tuple at the given index


    // Method to add this array data to an NBT compound and return the modified NBT compound
    public void toNBT(NbtCompound nbt) {

        NbtList StringArray = new NbtList();

        for (String str : dimension){
            StringArray.add(NbtString.of(str));
        }

        nbt.put(DimensionKey, StringArray);
        nbt.putLongArray(LocationKey, location);

    }

    public Set<WorldChunk> toSet(MinecraftServer server){
        Set<WorldChunk> output = new HashSet<>();

        int index = 0;
        while (index < location.length){
            Identifier id = Identifier.of(dimension[index]);

            RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, id);

            ChunkPos pos = new ChunkPos(location[index]);

            World world = server.getWorld(key);

            if (world != null) {
                output.add(world.getChunk(pos.x, pos.z));
            }
            else{

                String msg = "Unable to find world " + key.toString() + "When parsing nodes";
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()){
                    player.sendMessage(Text.literal(msg));
                }
            }
        }

        return output;
    }

}
