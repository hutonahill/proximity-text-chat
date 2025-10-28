package com.proxtextchat.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.proxtextchat.network.ChunkReference;
import com.proxtextchat.network.ChunkReferenceSet;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Builder for commands to create chunkSets that can be used in other commands.
 */
public class ChunkSetManager {
    private static final HashMap<Identifier, ChunkReferenceSet> _registry = new HashMap<>();

    public static Map<Identifier, ChunkReferenceSet> getRegistry(){
        return Collections.unmodifiableMap(_registry);
    }

    private static final String IDENTIFIER_NAME = "identifier";

    private static final String X_NAME = "x";

    private static final String Z_NAME = "y";

    private static final String WORLD_KEY_NAME = "world";

    //TODO: adds a system for generating chunk sets you can use to pass a set of chunks to a command.

    // 1: /createChunkSet <identifier>
    //    adds an identifier key to registry
    private static int CreateSetHandler(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        // Get an identifier out of the command context.
        Identifier identifier = context.getArgument(IDENTIFIER_NAME, Identifier.class);

        _registry.put(identifier, new ChunkReferenceSet());
    }


    // 2: /addChunkToSet <x> <z> <worldKey> <setIdentifier>
    //    adds a target chunk to a set
    private static int AddToSetHandler(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Identifier identifier = context.getArgument(IDENTIFIER_NAME, Identifier.class);

        ChunkReference reference = extractChunk(context);

        _registry.get(identifier).add(reference);
    }

    // 2: /removeChunkFromSet <x> <z> <worldKey> <setIdentifier>
    //    adds a target chunk to a set
    private static int RemoveFromSetHandler(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ChunkReference reference = extractChunk(context);

        Identifier identifier = context.getArgument(IDENTIFIER_NAME, Identifier.class);

        _registry.get(identifier).remove(reference);
    }


    // 4: /addCurrentChunkToSet <setIdentifier>
    //    adds the chunk your in to a set.
    private static int AddCurrentSetHandler(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ChunkReference reference = extractCurrentChunk(context);

        Identifier identifier = context.getArgument(IDENTIFIER_NAME, Identifier.class);

        _registry.get(identifier).add(reference);
    }

    // 5: /removeCurrentChunkToSet <setIdentifier>
    //    removes the chunk your in from a set.
    private static int AddCurrentSetHandler(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ChunkReference reference = extractCurrentChunk(context);

        Identifier identifier = context.getArgument(IDENTIFIER_NAME, Identifier.class);

        _registry.get(identifier).remove(reference);
    }

    private static ChunkReference extractChunk(CommandContext<ServerCommandSource> context){

        // I know there is a coordinate argument system. Don't know how it works.
        int xInput = context.getArgument(X_NAME, int.class);
        int zInput = context.getArgument(Z_NAME, int.class);

        RegistryKey<World> worldKey;

        ChunkPos pos = new ChunkPos(xInput, zInput);
        return new ChunkReference(worldKey, pos);
    }

    private static ChunkReference extractCurrentChunk(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ChunkPos pos = context.getSource().getEntityOrThrow().getChunkPos();
        RegistryKey<World> worldKey = context.getSource().getWorld().getRegistryKey();

        return new ChunkReference(worldKey, pos);
    }

}
