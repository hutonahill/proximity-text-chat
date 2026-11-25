package com.proxtextchat.ModMenu;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.proxtextchat.PlayerChatRageMethodCommand.ChatRangeRegistry;
import com.proxtextchat.PlayerChatRageMethodCommand.PlayerChatRangeDefinition;

import com.proxtextchat.PlayerChatRageMethodCommand.StandardPlayerChatRangeMethod;
import com.proxtextchat.ProxChatBaseModClient;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;


/**
 * The definition for this mods configuration file.
 */
public class Config{
    /**
     * The default player chat range.
     */
    public static final Integer DEFAULT_PLAYER_TO_PLAYER_CHAT_RANGE = 64;
    /**
     * The default chat range method
     */
    public static final PlayerChatRangeDefinition DEFAULT_CHAT_METHOD = StandardPlayerChatRangeMethod.getInstance();
    /**
     * The minimum player chat range.
     */
    public static final int MIN_PLAYER_CHAT_RANGE = 0;
    /**
     * the maximum player chat range.
     */
    public static final int MAX_PLAYER_CHAT_RANGE = 512;

    static {
        //noinspection ConstantValue
        if (MIN_PLAYER_CHAT_RANGE > MAX_PLAYER_CHAT_RANGE) {
            throw new IllegalStateException(
                "Minimum player chat range cannot exceed maximum player chat range."
            );
        }

        if(MIN_PLAYER_CHAT_RANGE < 0){
            throw new IllegalStateException(
                "Minimum player chat range is currently "
                    + MIN_PLAYER_CHAT_RANGE
                    + ". This value must be non-negative."
            );
        }

        //noinspection ConstantValue
        if (DEFAULT_PLAYER_TO_PLAYER_CHAT_RANGE < MIN_PLAYER_CHAT_RANGE
            || DEFAULT_PLAYER_TO_PLAYER_CHAT_RANGE > MAX_PLAYER_CHAT_RANGE) {
            throw new IllegalStateException(
                "Default player chat range must be within the allowed range."
            );
        }
    }

    private Integer playerChatRange;
    private PlayerChatRangeDefinition chatRangeDefinition;

    /**
     * Used to convert between game memory and long term storage.
     */
    public static final Codec<Config> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    Codec.intRange(MIN_PLAYER_CHAT_RANGE,MAX_PLAYER_CHAT_RANGE).optionalFieldOf("player_chat_range", DEFAULT_PLAYER_TO_PLAYER_CHAT_RANGE)
                             .forGetter(Config::getPlayerChatRange),
                    Identifier.CODEC.optionalFieldOf("player_chat_range_definition", DEFAULT_CHAT_METHOD.getID())
                                    .forGetter(Config::getChatRangeDefinition)

            ).apply(instance, Config::new));

    /**
     * S constructor for {@link Config}.
     * @param playerChatRange The range at which a player can send messages to other players when proximity text chat is enabled.
     * @param chatRangeDefinitionId The identifier for the chat range finding method.
     */
    public Config(Integer playerChatRange, Identifier chatRangeDefinitionId) {
        // TODO: Validate these inputs.
        this.playerChatRange = playerChatRange;
        PlayerChatRangeDefinition definition =
            ChatRangeRegistry.registry.get(chatRangeDefinitionId);

        if (definition == null) {
            ProxChatBaseModClient.logger.warn(
                "Unknown chat range definition '{}'. Using default.",
                chatRangeDefinitionId
            );

            this.chatRangeDefinition = DEFAULT_CHAT_METHOD;
        } else {
            this.chatRangeDefinition = definition;
        }
    }

    /**
     * The constructor for {@link Config}.
     */
    public Config() {
        this.playerChatRange = DEFAULT_PLAYER_TO_PLAYER_CHAT_RANGE;
        this.chatRangeDefinition = DEFAULT_CHAT_METHOD;
    }

    public Integer getPlayerChatRange() {
        return playerChatRange;
    }
    public void setPlayerChatRange(Integer playerChatRange) {
        this.playerChatRange = playerChatRange;
    }

    public Identifier getChatRangeDefinition() {
        return chatRangeDefinition.getID();
    }

    public void setChatRangeDefinition(PlayerChatRangeDefinition chatRangeDefinition) {
        this.chatRangeDefinition = chatRangeDefinition;
    }

    /**
     * @param location Saves the config file to a location.
     */
    public void save(File location) {
        DataResult<JsonElement> result = CODEC.encodeStart(JsonOps.INSTANCE, this);
        if (result.error().isPresent() || result.isError() || result.result().isEmpty()) {
            ProxChatBaseModClient.logger.warn("Failed to encode the configuration. Configuration not saved.");
            result.error().ifPresent(error -> ProxChatBaseModClient.logger.error("Error details: {}", error.message()));
        } else {
            try {
                Files.writeString(
                        location.toPath(),
                        new GsonBuilder().setPrettyPrinting().create().toJson(result.result().get())
                );
            } catch (IOException exception) {
                ProxChatBaseModClient.logger.error("IOException occurred while saving configuration file.", exception);
                ProxChatBaseModClient.logger.warn("Failed to save configuration. Configuration not saved.");
            }
        }
    }


    /**
     * @param location The location of the config file.
     * @return the Config or a default config and any errors.
     */
    public static LoadResult load(File location){
        // there is no config, we need to create a new default config and save it.
        if (!location.exists()) {
            Config defaultConfig = new Config();
            defaultConfig.save(location);
            return new LoadResult(defaultConfig, Optional.empty()) ;
        }

        try{
            String fileContents = Files.readString(location.toPath());
            JsonElement json = JsonParser.parseString(fileContents);
            DataResult<Config> resultCodec = CODEC.parse(JsonOps.INSTANCE, json);

            if (resultCodec.error().isPresent()) {
                String error = resultCodec.error().get().message();

                String message = ("Invalid config file '%s'. Using default configuration. \nDetails: %s")
                    .formatted(location.getAbsolutePath(), error);

                ProxChatBaseModClient.logger.error(message);

                return new LoadResult(
                    new Config(),
                    Optional.of(message)
                );
            }

            if(resultCodec.result().isPresent()){
                return new LoadResult(resultCodec.result().get(), Optional.empty());
            }
            else{
                String message = (
                    "The config decoder returned neither a configuration nor an error "
                        + "while loading '%s'. Using default configuration."
                ).formatted(location.getAbsolutePath());

                ProxChatBaseModClient.logger.error(message);

                return new LoadResult(
                    new Config(),
                    Optional.of(message)
                );
            }
        }   catch (IOException exception){
            String message = ("Could not read config file '%s'. "
                + "Using default configuration. \nDetails: %s")
                .formatted(location.getAbsolutePath(), exception.getMessage());

            ProxChatBaseModClient.logger.error(message, exception);

            return new LoadResult(
                new Config(),
                Optional.of(message)
            );
        }
        catch (JsonParseException exception) {
            String message = ("Config file '%s' contains invalid JSON. " +
                "Check its commas, quotes, and braces. " +
                "Using default configuration. \n \nDetails: %s")
                .formatted(
                    location.getAbsolutePath(),
                    exception.getMessage()
                );

            ProxChatBaseModClient.logger.error(message);

            return new LoadResult(new Config(), Optional.of(message));
        }
    }

    /**
     * @param config the config. if there was an error this is the default config.
     * @param error an error message if an error occurred.
     */
    public record LoadResult(
        Config config,
        Optional<String> error
    ) {}
}
