package com.proxtextchat.PlayerChatRageMethodCommand;

import com.proxtextchat.ProximityTextChat;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;

import java.util.HashSet;
import java.util.Set;

public class StandardPlayerChatRangeMethod implements PlayerChatRangeDefinition{

    private StandardPlayerChatRangeMethod(){}

    private static final StandardPlayerChatRangeMethod INSTANCE = new StandardPlayerChatRangeMethod();

    public Set<Chunk> getChunks(Entity sender, int range) {
        Set<Chunk> chunksInRange = new HashSet<>();
        ServerWorld world = (ServerWorld) sender.getWorld();
        ChunkPos senderChunkPos = world.getChunk(sender.getBlockPos()).getPos();

        for (int dx = -range; dx <= range; dx++) {
            for (int dz = -range; dz <= range; dz++) {
                ChunkPos chunkPos = new ChunkPos(senderChunkPos.x + dx, senderChunkPos.z + dz);
                Chunk chunk = world.getChunk(chunkPos.x, chunkPos.z);
                chunksInRange.add(chunk);
            }
        }

        return chunksInRange;
    }

    public static PlayerChatRangeDefinition getInstance() {
        return INSTANCE;
    }

    @Override
    public Identifier getID() {
        return Identifier.of(ProximityTextChat.MOD_ID, "standard_chat_range_method");
    }
}
