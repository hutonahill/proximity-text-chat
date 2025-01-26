package com.proxtextchat.PlayerChatRageMethodCommand;

import com.proxtextchat.ProxChatBaseMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;

import java.util.HashSet;
import java.util.Set;

public class StandardPlayerChatRangeMethod implements PlayerChatRangeDefinition{

    private StandardPlayerChatRangeMethod(){}

    private static final StandardPlayerChatRangeMethod INSTANCE = new StandardPlayerChatRangeMethod();

    public Set<PlayerEntity> getPlayers(Entity sender, int range) {
        Set<PlayerEntity> playersInRange = new HashSet<>();
        ServerWorld world = (ServerWorld) sender.getWorld();
        BlockPos senderPos = sender.getBlockPos();

        // Iterate through chunks within range of the sender
        for (int dx = -range; dx <= range; dx++) {
            for (int dz = -range; dz <= range; dz++) {
                ChunkPos chunkPos = new ChunkPos(senderPos.getX() >> 4 + dx, senderPos.getZ() >> 4 + dz);
                Chunk chunk = world.getChunk(chunkPos.x, chunkPos.z);

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
