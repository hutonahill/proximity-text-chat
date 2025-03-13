package com.proxtextchat;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.proxtextchat.network.ChannelManager;
import com.proxtextchat.network.ChannelMismatch;
import com.proxtextchat.network.ChunkReference;
import com.proxtextchat.network.NetworkNode;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collection;
import java.util.HashSet;

import static com.proxtextchat.ProxChatBaseMod.ENABLE_PROXIMITY_TEXT_CHAT;
import static com.proxtextchat.ProxChatBaseMod.getAlias;

public class DisableComCommands {

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

        // need to handle
    }

    private static final ChannelManager manager = ChannelManager.Instance;

    private static int newDirectCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {



        Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
        String message = StringArgumentType.getString(context, "message");
        ServerPlayerEntity sender = context.getSource().getPlayer();
        if (sender == null) {
            context.getSource().sendFeedback(
                    () -> Text.literal("You must be a player to use this command.").formatted(Formatting.RED),
                    false);
            return 0;
        }

        //TODO: need to check if the gamerule is enabled.

        ServerWorld world = sender.getServerWorld();

        int chunkX = sender.getChunkPos().x;
        int chunkY = sender.getChunkPos().z;
        ChunkReference playerChunk = new ChunkReference(world.getChunk(chunkX, chunkY));

        //TODO: via proximity
        
        // via network
        HashSet<NetworkNode> playerNodes;
        try {
            // get the nodes in the chunk the player is in, for the channels the user is registered to send to.
            playerNodes = manager.NodesReceivingInChunk(playerChunk,
                    manager.getReceivingFromChannelsForPlayer(sender));
        } catch (ChannelMismatch e) {
            throw new RuntimeException(e);
        }


        for(ServerPlayerEntity player : targets){
            for(NetworkNode node : playerNodes){
                manager.directToPlayerMessage(node, player, new Message(sender, getAlias.apply(sender), Text.literal(message)));
            }
        }

        return Command.SINGLE_SUCCESS;


    }


}
