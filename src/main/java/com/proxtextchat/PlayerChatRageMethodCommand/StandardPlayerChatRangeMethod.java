package com.proxtextchat.PlayerChatRageMethodCommand;

import com.proxtextchat.ProxChatBaseMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

/**
 * The default method for determining Player to Player chat.
 */
public class StandardPlayerChatRangeMethod implements PlayerChatRangeDefinition{

    private StandardPlayerChatRangeMethod(){}

    private static final StandardPlayerChatRangeMethod INSTANCE = new StandardPlayerChatRangeMethod();

    public Set<PlayerEntity> getPlayers(Entity sender, int range) {
        Set<PlayerEntity> playersInRange = new HashSet<>();
        ServerWorld world = (ServerWorld) sender.getWorld();

        // Iterate through chunks within range of the sender
        for (int dx = -range; dx <= range; dx++) {
            for (int dz = -range; dz <= range; dz++) {

                // Iterate through all players in the chunk
                for (PlayerEntity player : world.getPlayers()) {
                    if (player.squaredDistanceTo(sender) <= range * range) {
                        playersInRange.add(player);
                    }
                }
            }
        }

        return playersInRange;
    }

    public static PlayerChatRangeDefinition getInstance() {
        return INSTANCE;
    }

    @Override
    public Identifier getID() {
        return Identifier.of(ProxChatBaseMod.MOD_ID, "standard_chat_range_method");
    }
}
