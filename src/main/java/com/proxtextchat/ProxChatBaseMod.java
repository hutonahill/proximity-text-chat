package com.proxtextchat;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.serialization.DataResult;
import com.proxtextchat.PlayerChatRageMethodCommand.ChatRangeRegistry;
import com.proxtextchat.PlayerChatRageMethodCommand.PlayerChatRangeDefinition;
import com.proxtextchat.PlayerChatRageMethodCommand.PlayerChatRangeMethodCommandSuggestionProvider;
import com.proxtextchat.PlayerChatRageMethodCommand.StandardPlayerChatRangeMethod;
import com.proxtextchat.network.*;
import com.proxtextchat.network.soucekeys.ConsoleSourceKey;
import com.proxtextchat.network.soucekeys.MessageSourceKey;
import com.proxtextchat.network.soucekeys.PlayerSouceKey;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.Consumer;

import static net.minecraft.server.command.CommandManager.argument;


/**
 * The initializer.This is the heart of the mod.
 * See Readme.md for more details about the purpose of this mod
 */
public class ProxChatBaseMod implements ModInitializer {

    /**
     * The method the system will use to determine the Alias of the player.
     * Replace this if you're using an Alias system
     * <p>
     * This method is public and no final so other mods can add their own code for determining what a player's alias should be.
     * </p>
     */
    @SuppressWarnings("CanBeFinal") // This is exposed as public and not final so other mods can modify
    public static AliasResolver getAlias = (source, sourceKey) -> {
        if (sourceKey instanceof ConsoleSourceKey) {
            return Text.literal("⟦Server Console⟧");
        }

        return source.getDisplayName();
    };

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
    public static final String ModFolder = "ProxChatBaseModData";

    /**
     * The name of the argument in the chat method command
     */
    public static final String ChatMethodArgumentName = "Method";


    private static final PlayerChatRangeDefinition ChatMethod = StandardPlayerChatRangeMethod.getInstance();

    private static final ChannelManager manager = ChannelManager.Instance;


    /**
     * The gamerule that enables proximity text chat.
     */
    public static final GameRules.Key<GameRules.BooleanRule> ENABLE_PROXIMITY_TEXT_CHAT = GameRuleRegistry.register(
            "enableProximityTextChat",
            GameRules.Category.CHAT,
            GameRuleFactory.createBooleanRule(false)
    );

    /**
     * The game rule that sets the player to player chat range.
     */
    public static final GameRules.Key<GameRules.IntRule> PLAYER_TO_PLAYER_CHAT_RANGE = GameRuleRegistry.register(
            "playerToPlayerChatRange",
            GameRules.Category.CHAT,
            GameRuleFactory.createIntRule(200)
    );


    // TODO: ask delta about what these are exactly. Then write Javadoc for them.

    public static final TagKey<Item> SEND_IN_INVENTORY = TagKey.of(RegistryKeys.ITEM, id("send_in_inventory"));
    public static final TagKey<Item> SEND_IN_HOTBAR = TagKey.of(RegistryKeys.ITEM, id("send_in_hotbar"));
    public static final TagKey<Item> SEND_IN_HAND = TagKey.of(RegistryKeys.ITEM, id("send_in_hand"));

    public static final TagKey<Item> RECEIVE_IN_INVENTORY = TagKey.of(RegistryKeys.ITEM, id("receive_in_inventory"));
    public static final TagKey<Item> RECEIVE_IN_HOTBAR = TagKey.of(RegistryKeys.ITEM, id("receive_in_hotbar"));
    public static final TagKey<Item> RECEIVE_IN_HAND = TagKey.of(RegistryKeys.ITEM, id("receive_in_hand"));

    private static boolean AllowChatMessageRule(
        SignedMessage message,
        ServerPlayerEntity sender,
        MessageType.Parameters params
    ){
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
            MessageSourceKey senderKey = PlayerSouceKey.of(sender);
            try {
                // get the nodes in the chunk the player is in, for the channels he user is registered to send to.
                playerNodes = manager.NodesReceivingInChunk(playerChunk,
                        manager.getReceivingFromChannelsForSource(senderKey));
            } catch (ChannelMismatch e) {
                throw new RuntimeException(e);
            }
            MessageSourceKey sourceKey = PlayerSouceKey.of(sender);
            ServerCommandSource commandSource = sender.getCommandSource();

            for(NetworkNode node : playerNodes){
                manager.broadcastMessage(node, new Message(sourceKey, getAlias.resolve(commandSource, sourceKey), message.getContent()));
            }

            int PlayerChatRange = server.getGameRules().get(PLAYER_TO_PLAYER_CHAT_RANGE).get();


            //Player to Player chat
            Set<PlayerEntity> playersInRage = ChatRangeRegistry.Run(sender, PlayerChatRange);

            Message fomattedMessage = new Message(sourceKey, getAlias.resolve(commandSource, sourceKey), message.getContent());

            for(PlayerEntity receivingPlayer : playersInRage){

                receivingPlayer.sendMessage(fomattedMessage.getMessage());
            }

            return false;
        }

        return true;
    }

    @Override
    public void onInitialize() {

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ChannelManager.Instance.attachServer(server);
            PlayerReferance.clearRegistry();
            LoadChannelData(server);
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(ChannelManager.Instance::detachServer);

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

        ServerLifecycleEvents.BEFORE_SAVE.register(ProxChatBaseMod::SaveChannelData);

        // triggered every time a player sends a message. If the method returns false server will not deliver message.
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register(ProxChatBaseMod::AllowChatMessageRule);

    }

    private static Path addCorruptedTag(Path input) {
        String corruptedTag = "[CORRUPTED]";
        String fileName = input.getFileName().toString();

        int lastDotIndex = fileName.lastIndexOf('.');
        String newFileName;

        if (lastDotIndex == -1) {
            // No '.' in the filename
            newFileName = fileName + corruptedTag;
        } else {
            newFileName = fileName.substring(0, lastDotIndex) + corruptedTag + fileName.substring(lastDotIndex);
        }

        return input.resolveSibling(newFileName);
    }

    private static Path getUniquePath(Path targetPath) {
        if (!Files.exists(targetPath)) return targetPath;

        String fileName = targetPath.getFileName().toString();
        Path parent = targetPath.getParent();

        int lastDotIndex = fileName.lastIndexOf('.');
        String baseName = (lastDotIndex == -1) ? fileName : fileName.substring(0, lastDotIndex);
        String extension = (lastDotIndex == -1) ? "" : fileName.substring(lastDotIndex);

        int counter = 1;
        Path newPath;
        do {
            newPath = parent.resolve(baseName + "(" + counter + ")" + extension);
            counter++;
        } while (Files.exists(newPath));

        return newPath;
    }

    private static @Nullable Path saveCorruptedFile(Path current) {
        try {
            if (!Files.exists(current)) {
                throw new IllegalArgumentException("File does not exist: " + current);
            }

            // Apply a tag to the path so devs know it's corrupted
            Path taggedPath = addCorruptedTag(current);

            // Ensure the corrupted filename is unique
            Path uniqueTaggedPath = getUniquePath(taggedPath);

            // Move the original file to the corrupted filename
            Files.move(current, uniqueTaggedPath);

            return uniqueTaggedPath;
        } catch (IOException e) {
            LOGGER.error("Failed to copy corrupted file", e);
        }

        return null;
    }

    private static void LoadChannelData(MinecraftServer server){
        ChannelManager.Instance.clear();

        // define where we save data.
        Path savePath = server.getSavePath(WorldSavePath.ROOT)
                .resolve(ModFolder)
                .resolve(ChannelManager.ChannelManagerFolder)
                .resolve(ChannelManager.ChannelFile);


        // make sure we don't reference something that doesn't exist.
        try{
            Files.createDirectories(savePath.getParent());
        }
        catch (IOException e){

            LOGGER.error("Failed to generate Channel Data filepath. " +
                    "Saving a copy of the corrupted file ", e);
        }


        List<Channel> channelList;

        if (Files.exists(savePath)) {

            NbtCompound loadedData;
            try{
                loadedData = NbtIo.readCompressed(savePath, NbtSizeTracker.ofUnlimitedBytes());

            } catch (IOException eOne) {
                LOGGER.warn(
                        "Failed to read compressed channel data file at `{}`. Attempting to recover by reading uncompressed...",
                        savePath, eOne
                );

                try {
                    NbtCompound recoveredData = NbtIo.read(savePath);

                    if (recoveredData == null) {
                        throw new IOException(
                                "Uncompressed channel data at " + savePath + " was empty."
                        );
                    }

                    loadedData = recoveredData;
                    LOGGER.warn("Successfully recovered channel data by reading it uncompressed channel data.");
                }
                catch (IOException eTwo){
                    Path newCorruptPath = saveCorruptedFile(savePath);

                    LOGGER.error(
                        "Failed to read channel data. The corrupted file was preserved at '{}'. " +
                            "All channels have been lost.",
                        newCorruptPath,
                        eTwo
                    );
                    for (Consumer<ChannelDataLossContext> handler : recoveryHandlers){
                        handler.accept(new ChannelDataLossContext(server, newCorruptPath, eTwo));
                    }
                    return;
                }
            }

            NbtElement loadedElement = new NbtCompound();
            if(loadedData != null){
                if (loadedData.contains("data")){
                    loadedElement = loadedData.get("data");
                }
            }

            DataResult<List<Channel>> dataResult = Channel.codec(server).listOf().parse(NbtOps.INSTANCE, loadedElement);
            if(dataResult.error().isPresent()){
                Path newCorruptPath = saveCorruptedFile(savePath);

                String details = dataResult.error().orElseThrow().message();

                String message = (
                    "Channel data at '%s' was readable as NBT data but failed validation. " +
                        "The corrupted file has been preserved at %s. Details: %s"
                ).formatted(savePath, newCorruptPath, details);

                Exception cause = new IllegalArgumentException(message);

                LOGGER.error(message, cause);



                for (Consumer<ChannelDataLossContext> handler : recoveryHandlers) {
                    handler.accept(
                        new ChannelDataLossContext(server, newCorruptPath, cause)
                    );
                }

                return;
            }
            else{
                channelList = dataResult.resultOrPartial(LOGGER::error).orElseThrow();
            }


        } else {
            return;
        }
        ChannelManager.Instance.addAll(channelList);
    }

    /**
     * A context for anyone else who wants to deal with corrupted network data :)
     * @param server The server that owns the data that was unreadable
     * @param dataPath The path to the file with the unreadable data. Or if the file was not able to be saved, null.
     * @param cause The reason the data could not be read.
     */
    public record ChannelDataLossContext(
            @NotNull MinecraftServer server,
            @Nullable Path dataPath,
            @NotNull Exception cause
    ) {}

    private static final HashSet<Consumer<ChannelDataLossContext>> recoveryHandlers = new HashSet<>();

    /**
     * @param handler This code will be executed when Channel data is lost.
     * @return {@code true} if the registry did not already contain the handler
     */
    public static boolean RegisterChannelDataLossRecoveryHandler(Consumer<ChannelDataLossContext> handler){
        return recoveryHandlers.add(handler);
    }

    /**
     * @param handler This code will no longer be run when Channel Data is lost.
     * @return {@code true} if the registry contained the handler
     */
    public static boolean UnRegisterChannelDataLossRecoveryHandler(Consumer<ChannelDataLossContext> handler){
        return recoveryHandlers.remove(handler);
    }



    private static void SaveChannelData(MinecraftServer server, boolean flush, boolean force){
        // Encode data to NbtElement
        DataResult<NbtElement> saveDataResult = Channel.codec(server).listOf().encodeStart(NbtOps.INSTANCE,
                new ArrayList<>(ChannelManager.Instance.getChannelSet()));

        if(saveDataResult.error().isPresent()){
            String details = saveDataResult.error().orElseThrow().message();

            LOGGER.error(
                "Failed to encode channel data. The existing save file was not " +
                    "overwritten. Details: {}",
                details
            );

            return;
        }

        Optional<NbtElement> optionalElement = saveDataResult.result();

        if (optionalElement.isEmpty()) {
            LOGGER.error(
                "Failed to encode channel data: the codec returned neither data nor an error. " +
                    "The existing save file was not overwritten."
            );

            return;
        }

        NbtElement saveDataElement = optionalElement.get();

        NbtCompound nbt = new NbtCompound();
        nbt.put("data",saveDataElement);

        Path savePath = server.getSavePath(WorldSavePath.ROOT)
                .resolve(ModFolder)
                .resolve(ChannelManager.ChannelManagerFolder)
                .resolve(ChannelManager.ChannelFile);




        try {
            // make sure the directory exists
            Files.createDirectories(savePath.getParent());

            // Create a temporary file to write to so we don't corrupt good data if save is interrupted.
            Path atomicSavePath = Files.createTempFile(
                savePath.getParent(),
                savePath.getFileName().toString(),
                ".tmp"
            );

            // Save data to the temporary file.

            try{
                NbtIo.writeCompressed(nbt, atomicSavePath);

                // Move that file to overwrite the right normal save file as quickly as possible.
                Files.move(
                    atomicSavePath,
                    savePath,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
                );
            } finally {
                try {
                    Files.deleteIfExists(atomicSavePath);
                } catch (IOException cleanupException) {
                    LOGGER.warn(
                        "Failed to remove temporary channel data file '{}'.",
                        atomicSavePath,
                        cleanupException
                    );
                }
            }
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


    /**
     * The code for setting the player range method.
     *
     * @param context the context for the command.
     * @return a success key
     */
    @SuppressWarnings("SameReturnValue")
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

        // TODO: this should probably modify the config.

        return 1;
    }


    /**
     * Creates an identifier within this mod's namespace.
     * @param path The path of the desired identifier
     * @return THe identifier.
     */
    public static Identifier id (String path)
    {
        return Identifier.of(MOD_ID, path);
    }

}

