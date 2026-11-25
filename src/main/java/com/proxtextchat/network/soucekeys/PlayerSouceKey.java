package com.proxtextchat.network.soucekeys;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class PlayerSouceKey extends MessageSouceKey {

    private final UUID playerId;

    private PlayerSouceKey(UUID Id){
        playerId = Id;
    }

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
    protected boolean hasSameIdentity(@Nullable MessageSouceKey key) {
        if (!(key instanceof PlayerSouceKey playerKey)) {
            return false;
        }

        return playerId.equals(playerKey.playerId);
    }

    private static final HashMap<UUID, PlayerSouceKey> registry = new HashMap<>();
}
