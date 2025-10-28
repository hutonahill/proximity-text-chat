package com.proxtextchat.network;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.proxtextchat.DisableComCommands;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;

public class ManualNodeCommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        // dont know what this is actualy doing here.
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
    }

}
