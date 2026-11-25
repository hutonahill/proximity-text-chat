package com.proxtextchat.network;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;

// TODO: i think this class might be redundant...

/**
 * A collection of commands used to manually create nodes and mange networks.
 */
public class ManualNodeCommands {

    /**
     * The method that actually registers the new commands.
     * @param dispatcher
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        // don't know what this is actually doing here.
        dispatcher.register(CommandManager.literal("w")
                // Not at all confident about the 2nd argument here.
                .then(CommandManager.argument("rangeChunks", StringArgumentType.greedyString())
                        .then(CommandManager.argument("receiveChunks", StringArgumentType.greedyString())
                                .then(CommandManager.argument("channelIdentifier", StringArgumentType.greedyString())
                                        .executes(ManualNodeCommands::SampleMethod)
                                )

                        )
                )
        );


        // TODO: need to handle other commands.

        // /say, /me, /say, /teammsg, /tellraw, /tm
    }
    private static int SampleMethod(CommandContext<ServerCommandSource> context) throws CommandSyntaxException{
        Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
        String message = StringArgumentType.getString(context, "message");
        return 0;
    }

}
