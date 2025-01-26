package com.proxtextchat.PlayerChatRageMethodCommand;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.chunk.Chunk;

import java.util.Set;

public interface PlayerChatRangeDefinition {
    Set<PlayerEntity> getPlayers(Entity sender, int range);

    static PlayerChatRangeDefinition getInstance() {
        throw new UnsupportedOperationException("Implementations must override this method to return a singleton instance.");
    }

    Identifier getID();
}
