package com.proxtextchat;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.proxtextchat.PlayerChatRageMethodCommand.ChatRangeRegistry;
import com.proxtextchat.PlayerChatRageMethodCommand.PlayerChatRangeDefinition;
import com.proxtextchat.PlayerChatRageMethodCommand.PlayerChatRangeMethodCommandSuggestionProvider;
import com.proxtextchat.PlayerChatRageMethodCommand.StandardPlayerChatRangeMethod;
import com.proxtextchat.network.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.*;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.chunk.Chunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static net.minecraft.server.command.CommandManager.argument;


/**
 * The initializer.This is the heart of the mod.
 * See Readme.md for more details about the purpose of this mod
 */
public class ProxChatBaseMod implements ModInitializer {

    /**
     * The method the system will use to determine the Alias of the player.
     * Replace this if you're using an Alias system
     */
    public static Function<ServerPlayerEntity, Text> getAlias = ProxChatBaseMod::AliasIsName;

    /**
     * A unique name for teh mod.
     */
    public static final String MOD_ID = "proxchatbasemod";

    /**
     * not sure how, but this is the standard logging setup, according to ChatGPT.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /**
     * The name of the folder containing all mod data
     */
    public static final String ModFolder = "ProxChatBaseMod";

    /**
     * The name of the argument in the chat method command
     */
    public static final String ChatMethodArgumentName = "Method";


    private static final PlayerChatRangeDefinition ChatMethod = StandardPlayerChatRangeMethod.getInstance();

    private static final ChannelManager manager = ChannelManager.Instance;




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

    public static final TagKey<Item> SEND_IN_INVENTORY = TagKey.of(RegistryKeys.ITEM, id("send_in_inventory"));
    public static final TagKey<Item> SEND_IN_HOTBAR = TagKey.of(RegistryKeys.ITEM, id("send_in_hotbar"));
    public static final TagKey<Item> SEND_IN_HAND = TagKey.of(RegistryKeys.ITEM, id("send_in_hand"));

    public static final TagKey<Item> RECEIVE_IN_INVENTORY = TagKey.of(RegistryKeys.ITEM, id("receive_in_inventory"));
    public static final TagKey<Item> RECEIVE_IN_HOTBAR = TagKey.of(RegistryKeys.ITEM, id("receive_in_hotbar"));
    public static final TagKey<Item> RECEIVE_IN_HAND = TagKey.of(RegistryKeys.ITEM, id("receive_in_hand"));

    private static boolean AllowChatMessageRule(SignedMessage message, ServerPlayerEntity sender,
                                                MessageType.Parameters params){
        MinecraftServer server = sender.getServer();
        ServerWorld world = sender.getServerWorld();

        int chunkX = sender.getChunkPos().x;
        int chunkZ = sender.getChunkPos().z;

        ChunkReference playerChunk = new ChunkReference(world.getChunk(chunkX, chunkZ));

        if (server == null) {
            return false;
        }

        boolean isProximityChatEnabled = server.getGameRules().get(ENABLE_PROXIMITY_TEXT_CHAT).get();

        if(isProximityChatEnabled){


            // Player to Network chat
            HashSet<NetworkNode> playerNodes;
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

            Message fomattedMessage = new Message(sender, getAlias.apply(sender), message.getContent());

            for(PlayerEntity receivingPlayer : playersInRage){

                receivingPlayer.sendMessage(fomattedMessage.getMessage());
            }

            return false;
        }

        return true;
    }

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

        ServerLifecycleEvents.SERVER_STARTED.register(ProxChatBaseMod::LoadChannelData);
        ServerLifecycleEvents.BEFORE_SAVE.register(ProxChatBaseMod::SaveChannelData);

        // triggered every time a player sends a message. If the method returns false server will not deliver message.
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register(ProxChatBaseMod::AllowChatMessageRule);




    }

    private static void LoadChannelData(MinecraftServer server){
        // define where we save data.
        Path savePath = server.getSavePath(WorldSavePath.ROOT)
                .resolve(ModFolder)
                .resolve(ChannelManager.ChannelManagerFolder)
                .resolve(ChannelManager.ChannelFile);

        try{

            // make sure we don't reference something that doesn't exist.
            Files.createDirectories(savePath.getParent());
            if (!Files.exists(savePath)) {
                Files.createFile(savePath);
            }


            FileInputStream fis = new FileInputStream(savePath.toFile());
            NbtCompound loadedData = NbtIo.readCompressed(fis, NbtSizeTracker.ofUnlimitedBytes());

            DataResult<Pair<List<Channel>, NbtElement>> dataResult = Channel.CODEC.listOf().decode(NbtOps.INSTANCE, loadedData);

            List<Channel> channelList = dataResult.resultOrPartial(LOGGER::error).orElseThrow().getFirst();

            ChannelManager.Instance.addAll(channelList);
        } catch (IOException e) {
            LOGGER.error("Failed to load channel data", e);
        }


    }

    private static void SaveChannelData(MinecraftServer server, boolean flush, boolean force){
        // Encode data to NbtElement
        DataResult<NbtElement> saveDataResult = Channel.CODEC.listOf().encodeStart(NbtOps.INSTANCE,
                new ArrayList<>(ChannelManager.Instance.getChannelSet()));

        NbtElement saveData = saveDataResult.resultOrPartial(LOGGER::error).orElseThrow();

        // Create the path where data will be saved
        Path savePath = server.getSavePath(WorldSavePath.ROOT)
                .resolve(ModFolder)
                .resolve(ChannelManager.ChannelManagerFolder)
                .resolve(ChannelManager.ChannelFile);


        try (FileOutputStream fos = new FileOutputStream(savePath.toFile())) {
            NbtIo.writeCompressed((NbtCompound) saveData, fos);  // Writing the NbtCompound to the file
        } catch (IOException e) {
            LOGGER.error("Failed to save channel data", e);
        }
    }

    /**
     * @param server The server where the chunks and players are.
     * @param chunks The chunks you wish to search.
     * @return a set of players in the specified chunks
     */
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

    /**
     * Default method for determining alias. Returns the player's name.
     * @param player The Player you want ot get the alias of
     * @return the alias of the target player
     */
    public static Text AliasIsName(ServerPlayerEntity player){
        return player.getName();
    }

}
