package com.proxtextchat.PlayerChatRageMethodCommand;

import net.minecraft.entity.Entity;
import net.minecraft.world.chunk.Chunk;

import java.util.Set;

public interface PlayerChatRangeDefinition {
    Set<Chunk> getChunks(Entity sender, int range);
}
