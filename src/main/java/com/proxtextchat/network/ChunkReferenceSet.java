package com.proxtextchat.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * An AbstractSet of WorldChunks that can be easily converted to NBT data.
 */
public class ChunkReferenceSet extends AbstractSet<ChunkReference> {
// We need this class to provide the CODEC

    private final Set<ChunkReference> Chunks;

    private static final String ChunkReferenceListKey = "ChunkReferenceList";

    /**
     * provides the necessary logic for translating
     * between in-memory objects and their external representations.
     * See <a href="https://docs.fabricmc.net/1.21/develop/codecs">Fabric Codec Docs</a>.
     */
    public static final Codec<ChunkReferenceSet> CODEC;

    static{
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ChunkReference.CODEC.listOf().fieldOf(ChunkReferenceListKey).forGetter(ChunkReferenceSet::getList)
        ).apply(instance, ChunkReferenceSet::new));
    }

    /**
     * For creating an NBT compatible WorldChunk set
     * @param chunks The set of chunks to be stored.
     */
    public ChunkReferenceSet(Collection<ChunkReference> chunks){
        Chunks = new HashSet<>(chunks);
    }

    /**
     * @param chunks converts a Set of {@link ChunkReference} into a
     */
    public ChunkReferenceSet(Set<ChunkReference> chunks){
        Chunks = chunks;
    }

    private List<ChunkReference> getList(){
        return new ArrayList<>(Chunks);
    }

    /**
     * Returns an iterator over the elements contained in this collection.
     *
     * @return an iterator over the elements contained in this collection
     */
    @Override
    public @NotNull Iterator<ChunkReference> iterator() {
        return Chunks.iterator();
    }

    @Override
    public int size() {
        return Chunks.size();
    }
}
