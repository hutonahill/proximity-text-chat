package com.proxtextchat.network.soucekeys;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.proxtextchat.mixin.ServerCommandSouce.ServerCommandSourceInspection;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class MessageSourceKey {
    public static final Codec<MessageSourceKey> CODEC =
        PossibleSourceType.CODEC.dispatch(
            "source_type",
            MessageSourceKey::getType,
            MessageSourceKey::getCodec
        );

    private static MapCodec<? extends MessageSourceKey> getCodec(
        PossibleSourceType type
    ) {
        return switch (type) {
            case Player ->
                PlayerSouceKey.CODEC.fieldOf("data");
            case Entity ->
                EntitySourceKey.CODEC.fieldOf("data");
            case Block ->
                BlockSourceKey.CODEC.fieldOf("data");
            case ServerConsole ->
                ConsoleSourceKey.CODEC.fieldOf("data");
            case UnknownType ->
                throw new IllegalArgumentException(
                    "Unknown message source type"
                );
        };
    }

    public abstract PossibleSourceType getType();

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof MessageSourceKey key)) {
            return false;
        }

        return hasSameIdentity(key);
    }

    @Override
    public abstract int hashCode();

    protected abstract boolean hasSameIdentity(MessageSourceKey key);

    public static MessageSourceKey of(@NotNull ServerCommandSource source){
        Objects.requireNonNull(source, "source");


        if(source.getPlayer() != null){
            return PlayerSouceKey.of(source.getPlayer());
        }
        else if (source.getEntity() != null){
            return EntitySourceKey.of(source.getEntity());
        }
        else if (((ServerCommandSourceInspection)source).isBlockSource()){
            BlockPos blockPosition =
                BlockPos.ofFloored(source.getPosition());

            ServerWorld world = source.getWorld();

            BlockEntity blockEntity = Objects.requireNonNull(
                world.getBlockEntity(blockPosition),
                 "Command source was classified as a block source, "
                    + "but no block entity exists at "
                    + blockPosition
                    + " in "
                    + world.getRegistryKey().getValue()
            );

            Identifier blockEntityType = Objects.requireNonNull(
                Registries.BLOCK_ENTITY_TYPE.getId(blockEntity.getType()),
                 "Block entity "
                    + blockEntity.getClass().getName()
                    + " at "
                    + blockPosition
                    + " in "
                    + world.getRegistryKey().getValue()
                    + " has an unregistered block entity type"
            );

            return BlockSourceKey.of(
                BlockPos.ofFloored(source.getPosition()),
                source.getWorld().getRegistryKey(),
                blockEntityType
            );
        }
        else if(((ServerCommandSourceInspection)source).isConsole()){
            return ConsoleSourceKey.INSTANCE;
        }
        else{
            ServerWorld sourceWorld = source.getWorld();

            String worldDescription =
                sourceWorld == null
                    ? "<no world>"
                    : sourceWorld.getRegistryKey().getValue().toString();

            throw new IllegalArgumentException(
                "Cannot create a message source key for command source '"
                    + source.getName()
                    + "' at "
                    + source.getPosition()
                    + " in "
                    + worldDescription
                    + ". The source is not a player, entity, recognized block "
                    + "source, or server console."
            );
        }
    }
}

