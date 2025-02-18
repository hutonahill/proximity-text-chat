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

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * An AbstractSet of WorldChunks that can be easily converted to NBT data.
 */
public class WorldChunkSetNbt extends AbstractSet<WorldChunk> {

    private final Set<WorldChunk> Chunks = new HashSet<>();

    private static final String LocationKey = "Location";

    private static final String DimensionKey = "Dimension";

    private final String idKey;

    /**
     * For creating an NBT compatible WorldChunk set
     * @param chunks The set of chunks to be stored.
     * @param key THe key by which the chunks with be Identified in NBT data.
     */
    public WorldChunkSetNbt(Set<WorldChunk> chunks, String key){

        idKey = key;

        Chunks.addAll(chunks);


    }

    /**
     * For extracting a WorldChunk set from an NBT Command.
     * @param nbt The NBT command containing the data for the Chunk Set
     * @param key The Key identifying the Chunk Set
     * @param server The server where the Chunks in the Set reside.
     */
    public WorldChunkSetNbt(NbtCompound nbt, String key, MinecraftServer server) {
        idKey = key;

        // Retrieve the long array from NBT
        long[] location = nbt.getLongArray(getRealKey(LocationKey));

        // Retrieve the string list from NBT
        NbtList stringList = nbt.getList(getRealKey(DimensionKey), 8);
        String[] dimension = new String[stringList.size()];

        // Populate the dimension array
        for (int i = 0; i < stringList.size(); i++) {
            dimension[i] = stringList.getString(i);
        }

        Chunks.clear();

        int index = 0;
        while (index < location.length){
            Identifier id = Identifier.of(dimension[index]);

            RegistryKey<World> WorldKey = RegistryKey.of(RegistryKeys.WORLD, id);

            ChunkPos pos = new ChunkPos(location[index]);

            World world = server.getWorld(WorldKey);

            if (world != null) {
                Chunks.add(world.getChunk(pos.x, pos.z));
            }
            else{

                String msg = "Unable to find world " + WorldKey.toString() + "When parsing nodes";
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()){
                    player.sendMessage(Text.literal(msg));
                }
            }

            index++;
        }
    }


    /**
     * For adding, the WorldChunk set to an NBT Command.
     * @param nbt The NBT Command you wish to add the WorldChunk Set to.
     */
    public void toNBT(NbtCompound nbt) {

        long[] location = new long[Chunks.size()];
        String[] dimension = new String[Chunks.size()];

        int index = 0;
        for (WorldChunk chunk : Chunks){
            location[index] = chunk.getPos().toLong();
            dimension[index] = chunk.getWorld().getRegistryKey().toString();

            index++;
        }

        NbtList StringArray = new NbtList();

        for (String str : dimension){
            StringArray.add(NbtString.of(str));
        }

        nbt.put(getRealKey(DimensionKey), StringArray);
        nbt.putLongArray(getRealKey(LocationKey), location);

    }


    private String getRealKey(String key){
        if(idKey != null){
            return key+idKey;
        }
        return key;
    }

    @Override
    public Iterator<WorldChunk> iterator() {
        return Chunks.iterator();
    }

    @Override
    public int size() {
        return Chunks.size();
    }
}
