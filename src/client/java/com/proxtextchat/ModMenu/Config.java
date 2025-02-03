package com.proxtextchat.ModMenu;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.proxtextchat.PlayerChatRageMethodCommand.PlayerChatRangeDefinition;

import com.proxtextchat.PlayerChatRageMethodCommand.StandardPlayerChatRangeMethod;
import com.proxtextchat.ProxChatBaseModClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Config {
    public static final Integer DEFAULT_PLAYER_TO_PLAYER_CHAT_RANGE = 64;
    public static final PlayerChatRangeDefinition DEFAULT_CHAT_METHOD = StandardPlayerChatRangeMethod.getInstance();

    private Integer playerChatRange;
    private PlayerChatRangeDefinition chatRangeDefinition;

    public static final Codec<Config> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    Codec.INT.optionalFieldOf("player_chat_range", DEFAULT_PLAYER_TO_PLAYER_CHAT_RANGE).forGetter(Config::getPlayerChatRange),
                    Codec.BOOL.optionalFieldOf("musicTurnoff", DEFAULT_CHAT_METHOD).forGetter(Config::getChatRangeDefinition)
            ).apply(instance, Config::new));

    public Config(Integer playerChatRange, boolean chatRangeDefinition) {
        this.playerChatRange = playerChatRange;
    }

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

    public PlayerChatRangeDefinition getChatRangeDefinition() {
        return chatRangeDefinition;
    }

    public void setChatRangeDefinition(PlayerChatRangeDefinition chatRangeDefinition) {
        this.chatRangeDefinition = chatRangeDefinition;
    }

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
}
