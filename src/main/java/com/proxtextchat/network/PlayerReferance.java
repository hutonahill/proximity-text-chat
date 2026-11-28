package com.proxtextchat.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Uuids;
import net.minecraft.util.WorldSavePath;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class PlayerReferance {
    @NotNull
    private final UUID playerId;

    @NotNull
    private final String username;

    @Override
    public int hashCode(){
        return playerId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj
            || obj instanceof PlayerReferance other
            && playerId.equals(other.playerId);
    }

    public static Codec<PlayerReferance> codec(@NotNull MinecraftServer server) {
        Objects.requireNonNull(server, "server");

        return StoredPlayerReference.CODEC.comapFlatMap(
            stored -> {
                if (!hasJoined(server, stored.playerId())) {
                    return DataResult.error(
                        () -> "No record of a player with this UUID on the server: "
                            + stored.playerId()
                            + ". This may indicate a file corruption issue."
                    );
                }

                return DataResult.success(
                    PlayerReferance.of(
                        stored.playerId(),
                        stored.username(),
                        server
                    )
                );
            },
            reference -> new StoredPlayerReference(
                reference.playerId,
                reference.username
            )
        );
    }

    public static PlayerReferance of(@NotNull PlayerEntity player){
        Objects.requireNonNull(player, "player");

        if(registry.containsKey(player.getUuid())){
            return registry.get(player.getUuid());
        }

        PlayerReferance newRef = new PlayerReferance(player);
        registry.put(player.getUuid(), newRef);
        return newRef;
    }

    public static PlayerReferance of(@NotNull UUID id, @NotNull String name, @NotNull MinecraftServer server){
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(server, "server");

        if(registry.containsKey(id)){
            return registry.get(id);
        }

        PlayerReferance newRef = new PlayerReferance(id, name, server);
        registry.put(id, newRef);
        return newRef;
    }

    private PlayerReferance(@NotNull PlayerEntity player){
        Objects.requireNonNull(player, "player");
        playerId = player.getUuid();
        username = player.getGameProfile().getName();
    }

    private PlayerReferance(@NotNull UUID id, @NotNull String name, @NotNull MinecraftServer server){
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(server, "server");

        if(!hasJoined(server, id)){
            throw new IllegalArgumentException("Provided UUID does not correspond to any player known to be on the server.");
        }
        playerId = id;
        username = name;
    }

    private static HashMap<UUID, PlayerReferance> registry = new HashMap<>();

    public static boolean hasJoined(@NotNull MinecraftServer server, @NotNull UUID uuid) {
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(uuid, "uuid");
        if (server.getPlayerManager().getPlayer(uuid) != null) {
            return true;
        }

        Path playerData = server
            .getSavePath(WorldSavePath.PLAYERDATA)
            .resolve(uuid + ".dat");

        return Files.isRegularFile(playerData);
    }

    public Optional<ServerPlayerEntity> getServerPlayer(@NotNull MinecraftServer server){
        Objects.requireNonNull(server, "server");

        return Optional.ofNullable(server.getPlayerManager().getPlayer(playerId));
    }

    public Optional<PlayerEntity> getPlayer(@NotNull MinecraftServer server) {
        return getServerPlayer(server)
            .map(player -> player);
    }

    private record StoredPlayerReference(
        UUID playerId,
        String username
    ) {
        private static final Codec<StoredPlayerReference> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                Uuids.CODEC
                    .fieldOf("player_id")
                    .forGetter(StoredPlayerReference::playerId),

                Codec.STRING
                    .fieldOf("username")
                    .forGetter(StoredPlayerReference::username)
            ).apply(instance, StoredPlayerReference::new));
    }
}
