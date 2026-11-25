package com.proxtextchat.network.soucekeys;

import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public final class EntitySourceKey extends MessageSouceKey{

    private final UUID eId;

    private EntitySourceKey(UUID Id){
        eId = Id;
    }

    public static EntitySourceKey of(@NotNull Entity entity){
        Objects.requireNonNull(entity, "entity");
        return of(entity.getUuid());
    }

    public static EntitySourceKey of(@NotNull UUID id){
        Objects.requireNonNull(id, "id");

        if(registry.containsKey(id)){
            return registry.get(id);
        }
        else{
            EntitySourceKey newKey = new EntitySourceKey(id);
            registry.put(id, newKey);
            return newKey;
        }
    }

    @Override
    public PossibleSourceType getType() {
        return PossibleSourceType.Entity;
    }


    @Override
    public int hashCode() {
        return Objects.hash(eId, getType());
    }

    @Override
    protected boolean hasSameIdentity(@Nullable MessageSouceKey key) {
        if (!(key instanceof EntitySourceKey entityKey)) {
            return false;
        }

        return eId.equals(entityKey.eId);
    }

    private static final HashMap<UUID, EntitySourceKey> registry = new HashMap<>();
}
