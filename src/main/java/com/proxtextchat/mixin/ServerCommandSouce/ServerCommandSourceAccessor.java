package com.proxtextchat.mixin.ServerCommandSouce;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.rcon.RconCommandOutput;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.CommandBlockExecutor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerCommandSource.class)
abstract class ServerCommandSourceAccessor implements ServerCommandSourceInspection, ServerCommandSourceMutation {

    @Shadow
    @Final
    private CommandOutput output;

    @Shadow
    @Final
    private Vec3d position;

    @Shadow
    @Final
    private ServerWorld world;

    @Unique
    private boolean hasMeaningfulPosition;

    @Shadow
    @Nullable
    public abstract Entity getEntity();

    @Unique
    @Override
    public boolean isConsole(){
        return (output instanceof MinecraftServer
            || output instanceof RconCommandOutput);
    }

    @Unique
    @Override
    public boolean isCommandBlock(){
        return output instanceof CommandBlockExecutor;
    }

    @Unique
    @Override
    public boolean isBlockSource() {
        if (output instanceof CommandBlockExecutor) {
            return getEntity() == null;
        }

        // world is null when FunctionLoader calls commands.
        if (output != CommandOutput.DUMMY || world == null) {
            return false;
        }

        BlockEntity blockEntity =
            world.getBlockEntity(BlockPos.ofFloored(position));

        return blockEntity != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Unique
    public boolean hasMeaningfulPosition(){
        return hasMeaningfulPosition;
    }

    @Unique
    @Override
    public void setMeaningfulPosition(boolean value){
        hasMeaningfulPosition = value;
    }

    /**
     * Initializes whether an original command source has a meaningful
     * in-world position.
     *
     * <p>This method is injected by Mixin at the end of Minecraft's public
     * {@link ServerCommandSource} constructor.</p>
     *
     * @param callbackInfo information about the constructor injection
     */
    @Inject(
        method = "<init>("
            + "Lnet/minecraft/server/command/CommandOutput;"
            + "Lnet/minecraft/util/math/Vec3d;"
            + "Lnet/minecraft/util/math/Vec2f;"
            + "Lnet/minecraft/server/world/ServerWorld;"
            + "I"
            + "Ljava/lang/String;"
            + "Lnet/minecraft/text/Text;"
            + "Lnet/minecraft/server/MinecraftServer;"
            + "Lnet/minecraft/entity/Entity;"
            + ")V",
        at = @At("RETURN")
    )
    private void initializeMeaningfulPosition(CallbackInfo callbackInfo) {
        hasMeaningfulPosition =
            getEntity() != null
                || output instanceof CommandBlockExecutor
                || (
                    output.equals(CommandOutput.DUMMY)
                    && isSignOrLecternSource()
                );
    }

    /**
     * Copies meaningful-position metadata through command-source
     * transformations that do not establish a new position.
     *
     * @param callbackInfo information about the returned command source
     */
    @Inject( // I am trying to be as specific as I can so this breaks loudly if Mojang changes anything. This might break mods.
        method = {
            "withOutput("
                + "Lnet/minecraft/server/command/CommandOutput;"
                + ")Lnet/minecraft/server/command/ServerCommandSource;",

            "withEntity("
                + "Lnet/minecraft/entity/Entity;"
                + ")Lnet/minecraft/server/command/ServerCommandSource;",

            "withRotation("
                + "Lnet/minecraft/util/math/Vec2f;"
                + ")Lnet/minecraft/server/command/ServerCommandSource;",

            "withReturnValueConsumer("
                + "Lnet/minecraft/command/ReturnValueConsumer;"
                + ")Lnet/minecraft/server/command/ServerCommandSource;",

            "withSilent()"
                + "Lnet/minecraft/server/command/ServerCommandSource;",

            "withLevel("
                + "I"
                + ")Lnet/minecraft/server/command/ServerCommandSource;",

            "withEntityAnchor("
                + "Lnet/minecraft/command/argument/"
                + "EntityAnchorArgumentType$EntityAnchor;"
                + ")Lnet/minecraft/server/command/ServerCommandSource;",

            "withWorld("
                + "Lnet/minecraft/server/world/ServerWorld;"
                + ")Lnet/minecraft/server/command/ServerCommandSource;",

            "withSignedArguments("
                + "Lnet/minecraft/network/message/SignedCommandArguments;"
                + "Lnet/minecraft/util/thread/FutureQueue;"
                + ")Lnet/minecraft/server/command/ServerCommandSource;"
        },
        at = @At("RETURN"),
        require = 18,
        expect = 18,
        allow = 18
    )
    private void inheritMeaningfulPositionAfter(
        CallbackInfoReturnable<ServerCommandSource> callbackInfo
    ) {
        ServerCommandSource returnedSource =
            callbackInfo.getReturnValue();

        ((ServerCommandSourceMutation) returnedSource).setMeaningfulPosition(hasMeaningfulPosition);
    }

    /**
     * This mixin ensures that Command Sources that need to gain meaningfulness will
     * actually gain that meaningfulness in {@link establishMeaningfulPosition}
     *
     * @param currentPosition The current position to be tested.
     * @param requestedPosition The new position to be tested.
     * @return {@code true} when both positions are equal and the
     * current source already has a meaningful position
     */
    @Redirect(method = "withPosition("
            + "Lnet/minecraft/util/math/Vec3d;"
            + ")Lnet/minecraft/server/command/ServerCommandSource;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/math/Vec3d;"
                + "equals(Ljava/lang/Object;)Z"
        ),
        require = 1,
        allow = 1
    )
    private boolean mayReuseExistingPositionSource(
        Vec3d currentPosition,
        Object requestedPosition
    ) {
        return hasMeaningfulPosition
            && currentPosition.equals(requestedPosition);
    }

    /**
     * Marks every command source returned by {@code withPosition} as
     * having an explicitly established position.
     *
     * <p>This method is injected by the ServerCommandSourceAccessor
     * Mixin.</p>
     *
     * @param callbackInfo information about the returned command source
     */
    @Inject(
        method = "withPosition("
            + "Lnet/minecraft/util/math/Vec3d;"
            + ")Lnet/minecraft/server/command/ServerCommandSource;",
        at = @At("RETURN"),
        require = 2,
        expect = 2,
        allow = 2
    )
    private void establishMeaningfulPosition(
        CallbackInfoReturnable<ServerCommandSource> callbackInfo
    ) {
        ServerCommandSource returnedSource =
            callbackInfo.getReturnValue();

        ((ServerCommandSourceMutation) returnedSource)
            .setMeaningfulPosition(true);
    }

    /**
     * Determines whether this command source is positioned at a sign or
     * lectern.
     *
     * <p>This is a helper method added by the ServerCommandSourceAccessor
     * Mixin.</p>
     *
     * @return {@code true} when the source is positioned at a sign or
     * lectern
     */
    @Unique
    private boolean isSignOrLecternSource() {
        if (world == null) {
            return false;
        }

        BlockEntity block =
            world.getBlockEntity(BlockPos.ofFloored(position));

        return block instanceof SignBlockEntity
            || block instanceof LecternBlockEntity;
    }
}
