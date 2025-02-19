package com.proxtextchat.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * An AbstractSet of WorldChunks that can be easily converted to NBT data.
 */
public class ChunkReferenceSet extends AbstractSet<ChunkReferance> {

    private final Set<ChunkReferance> Chunks;

    private static final String ChunkReferenceListKey = "ChunkReferenceList";

    private static final String DimensionKey = "Dimension";

    public static final Codec<ChunkReferenceSet> CODEC;

    static{
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ChunkReferance.CODEC.listOf().fieldOf(ChunkReferenceListKey).forGetter(ChunkReferenceSet::getList)
        ).apply(instance, ChunkReferenceSet::new));
    }

    /**
     * For creating an NBT compatible WorldChunk set
     * @param chunks The set of chunks to be stored.
     */
    public ChunkReferenceSet(Collection<ChunkReferance> chunks){
        Chunks = new HashSet<>(chunks);
    }

    public ChunkReferenceSet(Set<ChunkReferance> chunks){
        Chunks = chunks;
    }

    private List<ChunkReferance> getList(){
        return new ArrayList<>(Chunks);
    }

    /**
     * Returns an iterator over the elements contained in this collection.
     *
     * @return an iterator over the elements contained in this collection
     */
    @Override
    public @NotNull Iterator<ChunkReferance> iterator() {
        return Chunks.iterator();
    }

    @Override
    public int size() {
        return Chunks.size();
    }
}
