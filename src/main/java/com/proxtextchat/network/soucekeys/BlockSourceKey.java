package com.proxtextchat.network.soucekeys;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;

public final class BlockSourceKey extends MessageSouceKey{

    private final @NotNull BlockPos pos;

    private final @NotNull RegistryKey<World> worldKey;

    private final @NotNull Identifier blockType;

    private BlockSourceKey(@NotNull BlockPos pos, @NotNull RegistryKey<World> worldKey, @NotNull Identifier blockType){
        this.pos = pos;
        this.worldKey = worldKey;
        this.blockType = blockType;
    }

    public static BlockSourceKey of(@NotNull BlockEntity block){
        Objects.requireNonNull(block, "block");

        World world = Objects.requireNonNull(
            block.getWorld(),
            "Block entity is not attached to a world"
        );

        Identifier blockEntityType = Objects.requireNonNull(
            Registries.BLOCK_ENTITY_TYPE.getId(block.getType()),
            "Block entity type is not registered"
        );


        return of(block.getPos(), world.getRegistryKey(), blockEntityType);
    }

    public static BlockSourceKey of(@NotNull BlockPos pos, @NotNull RegistryKey<World> worldKey, @NotNull Identifier blockType){
        Objects.requireNonNull(pos, "pos");
        Objects.requireNonNull(worldKey, "worldKey");
        Objects.requireNonNull(blockType, "blockType");

        BlockIdentity block = new BlockIdentity(pos, worldKey, blockType);

        if(registry.containsKey(block)){
            return registry.get(block);
        }
        else{
            BlockSourceKey newKey = new BlockSourceKey(pos, worldKey, blockType);
            registry.put(block, newKey);
            return newKey;
        }
    }

    @Override
    public PossibleSourceType getType() {
        return getTypeStatic();
    }

    private static PossibleSourceType getTypeStatic(){
        return PossibleSourceType.Block;
    }

    @Override
    public int hashCode() {
        return hashCode(pos, worldKey, blockType);
    }

    private static int hashCode(BlockPos pos, RegistryKey<World> worldKey, Identifier blockType){
        return Objects.hash(pos, worldKey, blockType, getTypeStatic());
    }

    @Override
    protected boolean hasSameIdentity(MessageSouceKey key) {
        if (!(key instanceof BlockSourceKey blockKey)) {
            return false;
        }

        return pos.equals(blockKey.pos)
            && worldKey.equals(blockKey.worldKey)
            && blockType.equals(blockKey.blockType);
    }

    private static final HashMap<BlockIdentity, BlockSourceKey> registry = new HashMap<>();

    private record BlockIdentity(
        @NotNull BlockPos pos,
        @NotNull RegistryKey<World> worldKey,
        @NotNull Identifier blockEntityType
    ) {
    }
}
