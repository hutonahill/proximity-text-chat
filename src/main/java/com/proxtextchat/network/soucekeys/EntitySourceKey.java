package com.proxtextchat.network.soucekeys;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.Uuids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public final class EntitySourceKey extends MessageSourceKey {

    private final @NotNull UUID eId;

    public static final Codec<EntitySourceKey> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Uuids.CODEC.fieldOf("entity_id")
                 .forGetter(key -> key.eId)
        ).apply(instance, EntitySourceKey::of)
    );

    private EntitySourceKey(@NotNull UUID Id){
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
    protected boolean hasSameIdentity(@Nullable MessageSourceKey key) {
        if (!(key instanceof EntitySourceKey entityKey)) {
            return false;
        }

        return eId.equals(entityKey.eId);
    }

    private static final HashMap<UUID, EntitySourceKey> registry = new HashMap<>();
}
