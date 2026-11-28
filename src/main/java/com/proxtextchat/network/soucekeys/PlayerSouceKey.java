package com.proxtextchat.network.soucekeys;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Uuids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class PlayerSouceKey extends MessageSourceKey {

    private final UUID playerId;

    private PlayerSouceKey(UUID Id){
        playerId = Id;
    }

    public static final Codec<PlayerSouceKey> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Uuids.CODEC.fieldOf("player_id")
                       .forGetter(key -> key.playerId)
        ).apply(instance, PlayerSouceKey::of)
    );

    public static PlayerSouceKey of(ServerPlayerEntity player){
        return of(player.getUuid());
    }

    public static PlayerSouceKey of(@NotNull UUID id){
        Objects.requireNonNull(id, "id");

        if(registry.containsKey(id)){
            return registry.get(id);
        }
        else{
            PlayerSouceKey newKey = new PlayerSouceKey(id);
            registry.put(id, newKey);
            return newKey;
        }
    }

    @Override
    public PossibleSourceType getType() {
        return PossibleSourceType.Player;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, getType());
    }

    @Override
    protected boolean hasSameIdentity(@Nullable MessageSourceKey key) {
        if (!(key instanceof PlayerSouceKey playerKey)) {
            return false;
        }

        return playerId.equals(playerKey.playerId);
    }

    private static final HashMap<UUID, PlayerSouceKey> registry = new HashMap<>();
}
