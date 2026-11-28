package com.proxtextchat;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.proxtextchat.mixin.ServerCommandSouce.ServerCommandSourceInspection;
import com.proxtextchat.network.ChannelManager;
import com.proxtextchat.network.ChannelMismatch;
import com.proxtextchat.network.ChunkReference;
import com.proxtextchat.network.NetworkNode;
import com.proxtextchat.network.soucekeys.MessageSourceKey;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.Collection;
import java.util.HashSet;

import static com.proxtextchat.ProxChatBaseMod.ENABLE_PROXIMITY_TEXT_CHAT;
import static com.proxtextchat.ProxChatBaseMod.getAlias;


/**
 * Overwrites the vanilla chat commands so we can implement proximity chat.
 */
public class DisableComCommands {


    /**
     * Actually overwrites Minecraft's chat commands.
     * @param dispatcher
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("msg")
                .then(CommandManager.argument("targets", EntityArgumentType.players())
                        .then(CommandManager.argument("message", StringArgumentType.greedyString())
                                .executes(DisableComCommands::newDirectCommand)
                        )
                )
        );

        dispatcher.register(CommandManager.literal("tell")
                .then(CommandManager.argument("targets", EntityArgumentType.players())
                        .then(CommandManager.argument("message", StringArgumentType.greedyString())
                                .executes(DisableComCommands::newDirectCommand)
                        )
                )
        );

        dispatcher.register(CommandManager.literal("w")
                .then(CommandManager.argument("targets", EntityArgumentType.players())
                        .then(CommandManager.argument("message", StringArgumentType.greedyString())
                                .executes(DisableComCommands::newDirectCommand)
                        )
                )
        );

        // TODO: need to handle other commands.

        // /say, /me, /teammsg, /tellraw, /tm
    }

    private static final ChannelManager manager = ChannelManager.Instance;

    private static int newDirectCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
        String message = StringArgumentType.getString(context, "message");
        ServerCommandSource sender = context.getSource();
        if (sender == null) {

            return 0;
        }

        MinecraftServer server =sender.getServer();

        if(server == null){
            context.getSource().sendFeedback(
                    () -> Text.literal("Cannot find server").formatted(Formatting.RED),
                    false
            );
            return 0;
        }

        if(server.getGameRules().get(ENABLE_PROXIMITY_TEXT_CHAT).get()){


            ServerCommandSourceInspection inspection =
                (ServerCommandSourceInspection) sender;

            if (!inspection.hasMeaningfulPosition()) {
                sender.sendError(
                    Text.empty()
                        .append(Text.literal("["))
                        .append(Text.translatable("proxchatbasemod.mod_name"))
                        .append(Text.literal("] "))
                        .append(Text.translatable(    "proxchatbasemod.no_meaningful_pos_error"
                        ))
                );

                return 0;
            }

            ChunkReference SourceChunk = new ChunkReference(
                sender.getWorld().getRegistryKey(),
                new ChunkPos(BlockPos.ofFloored(sender.getPosition()))
            );

            // TODO: based on what is sending the message, get its location, or return an error message.

            //TODO: via proximity


            // via network
            HashSet<NetworkNode> playerNodes;
            try {
                // get the nodes in the chunk the player is in, for the channels the user is registered to send to.
                playerNodes = manager.NodesReceivingInChunk(
                    SourceChunk,
                    manager.getReceivingFromChannelsForSource(sender)
                );
            } catch (ChannelMismatch e) {
                throw new RuntimeException(e);
            }


            for(ServerPlayerEntity player : targets){
                for(NetworkNode node : playerNodes){

                    MessageSourceKey sourceKey = MessageSourceKey.of(sender);
                    manager.directToPlayerMessage( // TODO: we should use this return value to validate. somehow.
                        node, player, new Message(sourceKey, getAlias.resolve(sender, sourceKey), Text.literal(message))
                    );
                    // TODO: This method returns a success value. Do we need to use it here?
                }
            }

            return Command.SINGLE_SUCCESS;
        }
        else{

            // TODO: we need to implement the standard command here.
            return Command.SINGLE_SUCCESS;
        }
    }

    // TODO: write /blast

}
