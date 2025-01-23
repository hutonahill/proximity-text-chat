package com.proxtextchat;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.proxtextchat.PlayerChatRageMethodCommand.ChatRangeRegistry;
import com.proxtextchat.PlayerChatRageMethodCommand.PlayerChatRangeDefinition;
import com.proxtextchat.PlayerChatRageMethodCommand.PlayerChatRangeMethodCommandSuggestionProvider;
import com.proxtextchat.PlayerChatRageMethodCommand.StandardPlayerChatRangeMethod;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.chunk.Chunk;

import java.util.HashSet;
import java.util.Set;

import static net.minecraft.server.command.CommandManager.*;

//import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

public class ProxChatBaseMod implements ModInitializer {

    public static final GameRules.Key<GameRules.BooleanRule> CUSTOM_RULE = GameRuleRegistry.register(
            "EnableProximityTextChat",
            GameRules.Category.CHAT,
            GameRuleFactory.createBooleanRule(false)
    );

    private static int chatRange = 5;

    public static final String MOD_ID = "proxchatbasemod";

    public static final String ChatMethodArgumentName = "Method";

    private static final PlayerChatRangeDefinition ChatMethod = StandardPlayerChatRangeMethod.getInstance();

    @Override
    public void onInitialize() {

        ChatRangeRegistry.registerMethod(ChatMethod.getID(), ChatMethod);

        ChatRangeRegistry.selectKey(ChatMethod.getID());

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LiteralCommandNode<ServerCommandSource> PlayerChatRangeMethodNode = CommandManager
                    .literal("PlayerChatRangeMethod")
                    .requires(source -> source.hasPermissionLevel(4))
                    .then(argument(ChatMethodArgumentName, IdentifierArgumentType.identifier())
                            .suggests(new PlayerChatRangeMethodCommandSuggestionProvider())
                            .executes(this::PlayerRangeMethodCommand)
                    )
                    .build();

            dispatcher.getRoot().addChild(PlayerChatRangeMethodNode);
        });

        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            MinecraftServer server = sender.getServer();

            if (server == null) {
                return false;
            }

            boolean isProximityChatEnabled = server.getGameRules().get(CUSTOM_RULE).get();

            if(isProximityChatEnabled){
                // other stuff

                Set<Chunk> ChunksToSend = ChatMethod.getChunks(sender, chatRange);

                return false;
            }


            return true;
        });
    }

    public static Set<ServerPlayerEntity> getPlayersInChunks(MinecraftServer server, Set<Chunk> chunks) {
        Set<ServerPlayerEntity> playersInChunks = new HashSet<>();

        // Loop through all players on the server
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            // Get the player's current position
            BlockPos playerPos = player.getBlockPos();

            // Get the chunk the player is currently in
            Chunk playerChunk = player.getEntityWorld().getWorldChunk(playerPos);

            // Check if the player's chunk is in the given set of chunks
            if (chunks.contains(playerChunk)) {
                playersInChunks.add(player); // Add the player if they are in one of the chunks
            }
        }

        return playersInChunks;
    }

    private int PlayerRangeMethodCommand(CommandContext<ServerCommandSource> context) {
        Identifier methodName = IdentifierArgumentType.getIdentifier(context, ChatMethodArgumentName);

        boolean exists =  ChatRangeRegistry.getKeys().contains(methodName);

        if (exists == true){
            ChatRangeRegistry.selectKey(methodName);
            context.getSource().sendFeedback(() -> Text.literal("PlayerChatRangeMethod has been defined as " + methodName + "."), true);
        }
        else{
            context.getSource().sendFeedback(() -> Text.literal("Could not find PlayerChatRangeMethod " + methodName + "."), true);
        }


        return 1;
    }
}
