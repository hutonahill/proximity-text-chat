package com.proxtextchat;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.serialization.Codec;
import com.proxtextchat.PlayerChatRageMethodCommand.ChatRangeRegistry;
import com.proxtextchat.PlayerChatRageMethodCommand.PlayerChatRangeDefinition;
import com.proxtextchat.PlayerChatRageMethodCommand.PlayerChatRangeMethodCommandSuggestionProvider;
import com.proxtextchat.PlayerChatRageMethodCommand.StandardPlayerChatRangeMethod;
import com.proxtextchat.network.ChannelManager;
import com.proxtextchat.network.ChannelMismatch;
import com.proxtextchat.network.NetworkNode;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;



import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;


import static net.minecraft.server.command.CommandManager.*;

//import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

public class ProxChatBaseMod implements ModInitializer {


    public static final String MOD_ID = "proxchatbasemod";

    public static final String ChatMethodArgumentName = "Method";

    private static final PlayerChatRangeDefinition ChatMethod = StandardPlayerChatRangeMethod.getInstance();

    private static final ChannelManager manager = ChannelManager.Instance;

    //TODO: this should be a mod page button or a config option.
    public static final GameRules.Key<GameRules.BooleanRule> ENABLE_PROXIMITY_TEXT_CHAT = GameRuleRegistry.register(
            "enableProximityTextChat",
            GameRules.Category.CHAT,
            GameRuleFactory.createBooleanRule(false)
    );

    public static final GameRules.Key<GameRules.IntRule> PLAYER_TO_PLAYER_CHAT_RANGE = GameRuleRegistry.register(
            "playerToPlayerChatRange",
            GameRules.Category.CHAT,
            GameRuleFactory.createIntRule(200)
    );

    public static final ComponentType<String> CHANNEL = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            id("channel"),
            ComponentType.<String>builder().codec(Codec.STRING).build()
    );
    public static final TagKey<Item> SEND_IN_INVENTORY = TagKey.of(RegistryKeys.ITEM, id("send_in_inventory"));
    public static final TagKey<Item> SEND_IN_HOTBAR = TagKey.of(RegistryKeys.ITEM, id("send_in_hotbar"));
    public static final TagKey<Item> SEND_IN_HAND = TagKey.of(RegistryKeys.ITEM, id("send_in_hand"));

    public static final TagKey<Item> RECEIVE_IN_INVENTORY = TagKey.of(RegistryKeys.ITEM, id("receive_in_inventory"));
    public static final TagKey<Item> RECEIVE_IN_HOTBAR = TagKey.of(RegistryKeys.ITEM, id("receive_in_hotbar"));
    public static final TagKey<Item> RECEIVE_IN_HAND = TagKey.of(RegistryKeys.ITEM, id("receive_in_hand"));


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

        // triggered every time a player sends a message. If the method returns false server will not deliver message.
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            MinecraftServer server = sender.getServer();
            ServerWorld world = sender.getServerWorld();

            int chunkX = sender.getChunkPos().x;
            int chunkZ = sender.getChunkPos().z;

            WorldChunk playerChunk = world.getChunk(chunkX, chunkZ);

            if (server == null) {
                return false;
            }

            boolean isProximityChatEnabled = server.getGameRules().get(ENABLE_PROXIMITY_TEXT_CHAT).get();

            if(isProximityChatEnabled){


                // Player to Network chat
                HashSet<NetworkNode> playerNodes = null;
                try {
                    // get the nodes in the chunk the player is in, for the channels he user is registered to send to.
                    playerNodes = manager.NodesReceivingInChunk(playerChunk,
                            manager.getReceivingFromChannelsForPlayer(sender));
                } catch (ChannelMismatch e) {
                    throw new RuntimeException(e);
                }

                for(NetworkNode node : playerNodes){
                    manager.broadcastMessage(node, new Message(sender, message.getContent()));
                }

                int PlayerChatRange = server.getGameRules().get(PLAYER_TO_PLAYER_CHAT_RANGE).get();


                //Player to Player chat
                Set<PlayerEntity> playersInRage = ChatRangeRegistry.Run(sender, PlayerChatRange);

                Message fomattedMessage = new Message(sender, message.getContent());

                for(PlayerEntity receivingPlayer : playersInRage){

                    receivingPlayer.sendMessage(fomattedMessage.getMessage());
                }

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
    public static Identifier id(String path)
    {
        return Identifier.of(MOD_ID, path);
    }

}
