package com.proxtextchat.util;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.proxtextchat.network.ChunkReference;
import com.proxtextchat.network.ChunkReferenceSet;
import net.minecraft.command.argument.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;

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

    private static final String CHUNK_NAME = "chunk";

    private static final String X_NAME = "x";

    private static final String Z_NAME = "y";

    private static final String WORLD_KEY_NAME = "world";

    //TODO: adds a system for generating chunk sets you can use to pass a set of chunks to a command.

    // 1: /createChunkSet <identifier>
    //    adds an identifier key to registry
    private static int CreateSetHandler(CommandContext<ServerCommandSource> context){
        // Get an identifier out of the command context.
        Identifier identifier = context.getArgument(IDENTIFIER_NAME, Identifier.class);

        // If the identifier already exists, return an error.
        if(_registry.containsKey(identifier)){
            sendError(context,"proxchatbasemod.chunk_already_exists", identifier.toString());
            return 0;
        }

        _registry.put(identifier, new ChunkReferenceSet());

        return Command.SINGLE_SUCCESS;
    }


    // 2: /addChunkToSet <x> <z> <worldKey> <setIdentifier>
    //    adds a target chunk to a set
    private static int AddToSetHandler(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Identifier identifier = extractIdentifier(context);

        if(identifier == null){
            return 0;
        }

        ChunkReference reference = extractChunk(context);

        if(reference == null){
            return 0;
        }

        _registry.get(identifier).add(reference);

        return Command.SINGLE_SUCCESS;
    }

    // 2: /removeChunkFromSet <x> <z> <worldKey> <setIdentifier>
    //    adds a target chunk to a set
    private static int RemoveFromSetHandler(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Identifier identifier = extractIdentifier(context);

        if(identifier == null){
            return 0;
        }

        ChunkReference reference = extractChunk(context);

        if(reference == null){
            return 0;
        }

        _registry.get(identifier).remove(reference);

        return Command.SINGLE_SUCCESS;
    }


    // 4: /addCurrentChunkToSet <setIdentifier>
    //    adds the chunk your in to a set.
    private static int AddCurrentSetHandler(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Identifier identifier = extractIdentifier(context);

        if(identifier == null){
            return 0;
        }

        ChunkReference reference = extractCurrentChunk(context);

        _registry.get(identifier).add(reference);

        return Command.SINGLE_SUCCESS;
    }

    // 5: /removeCurrentChunkToSet <setIdentifier>
    //    removes the chunk your in from a set.
    private static int RemoveCurrentSetHandler(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Identifier identifier = extractIdentifier(context);

        if(identifier == null){
            return 0;
        }

        ChunkReference reference;

        reference = extractCurrentChunk(context);

        _registry.get(identifier).remove(reference);

        return Command.SINGLE_SUCCESS;
    }

    // 6: /readChunkSet <setIdentifier>
    //    displays a chunk set.
    private static int ReadChunkSetHandler(CommandContext<ServerCommandSource> context){
        Identifier identifier = context.getArgument(IDENTIFIER_NAME, Identifier.class);

        if(!_registry.containsKey(identifier)){
            // no such chunk set found.
            context.getSource().sendFeedback(
                    () -> Text.literal("No chunk set found for identifier " + identifier.toString() + ".")
                            .formatted(Formatting.RED),
                    false
            );
            return 0;
        }

        ChunkReferenceSet chunkSet = _registry.get(identifier);

        context.getSource().sendFeedback( () -> Text.literal(chunkSet.toString()), false);

        // return chunkSet.ToString()

        return Command.SINGLE_SUCCESS;
    }

    private static @Nullable ChunkReference extractChunk(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        // It seems like I should have to pass WORLD_KEY_NAME in at some point...
        // what if there are two world keys in an input?
        StringReader reader = new StringReader(context.getCommand().toString());
        RegistryKey<World> worldKey = RegistryKeyArgumentType.registryKey(RegistryKeys.WORLD).parse(reader);

        ServerWorld world = context.getSource().getServer()
                .getWorld(worldKey);

        // When we throw, we also need to return an error to the player, so they know what happened.
        if (world == null){
            sendError(context, "proxchatbasemod.cant_parse_world_key", worldKey.toString());
            return null;
        }

        //  = context.getArgument(WORLD_KEY_NAME, RegistryKeyArgumentType.registryKey(RegistryKeys.WORLD).getClass());

        // I know there is a coordinate argument system. Don't know how it works.
        Vector2d vec2 = Vector2DArgumentType.getVec2(context, CHUNK_NAME);

        BlockPos blockPos = new BlockPos((int)vec2.x, 0, (int)vec2.y);

        // TODO: check its valid in the world

        if(!World.isValid(blockPos)){
            // TODO: question, does this return an error to the user?
            throw BlockPosArgumentType.OUT_OF_BOUNDS_EXCEPTION.create();
        }

        if(!world.getWorldBorder().contains(blockPos)){
            // TODO: question, does this return an error to the user?
            throw BlockPosArgumentType.OUT_OF_BOUNDS_EXCEPTION.create();
        }

        WorldChunk worldChunk = world
            .getChunkManager()
            .getWorldChunk((int) vec2.x, (int) vec2.y);

        if(worldChunk == null){
            throw new UnknownError("By this point we should have validated the cords.");
        }

        ChunkPos chunkPos = worldChunk
            .getPos();

        return new ChunkReference(worldKey, chunkPos);
    }

    private static ChunkReference extractCurrentChunk(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ChunkPos pos = context.getSource().getEntityOrThrow().getChunkPos();
        RegistryKey<World> worldKey = context.getSource().getWorld().getRegistryKey();

        return new ChunkReference(worldKey, pos);
    }

    private static @Nullable Identifier extractIdentifier(CommandContext<ServerCommandSource> context){
        // pull the identifier out of the command.
        Identifier identifier = context.getArgument(IDENTIFIER_NAME, Identifier.class);

        // if there is no key, return an error to the user.
        if(!_registry.containsKey(identifier)){
            sendError(context, "proxchatbasemod.chunk_set_not_found", identifier.toString());
            return null;
        }

        return identifier;
    }

    private static void sendError(CommandContext<ServerCommandSource> context, String key, Object... args){
        context.getSource()
            .sendFeedback(
                () -> Text.translatable(key, args).formatted(Formatting.RED),
                false
            );
    }
}