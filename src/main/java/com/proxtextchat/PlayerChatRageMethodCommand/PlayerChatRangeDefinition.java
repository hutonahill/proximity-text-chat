package com.proxtextchat.PlayerChatRageMethodCommand;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.chunk.Chunk;

import java.util.Set;

/**
 * Interface for methods of determining Player to Player chat range.
 */
public interface PlayerChatRangeDefinition {
    /**
     * @param sender The entity broadcasting the message.
     * @param range range of the player in blocks
     * @return a set of players the method identifies as in range.
     */
    Set<PlayerEntity> getPlayers(Entity sender, int range);

    static PlayerChatRangeDefinition getInstance() {
        throw new UnsupportedOperationException("Implementations must override this method to return a singleton instance.");
    }

    Identifier getID();
}
