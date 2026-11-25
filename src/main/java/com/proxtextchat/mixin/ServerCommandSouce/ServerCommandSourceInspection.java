package com.proxtextchat.mixin.ServerCommandSouce;


/**
 * Provides read-only information added to {@link net.minecraft.server.command.ServerCommandSource}
 * by the Proximity Text Chat Mixin.
 *
 * <p>These methods are not part of the original Minecraft class. At
 * runtime, the Mixin makes each {@code ServerCommandSource} implement
 * this interface.</p>
 */
public interface ServerCommandSourceInspection {
    /**
     * Determines whether the command's execution chain has established
     * a meaningful in-world position.
     *
     * <p>This is a Mixin-provided inspection method. The value is
     * tracked as Minecraft creates modified command sources through
     * operations such as {@code execute as}, {@code execute at}, and
     * {@code execute positioned}.</p>
     *
     * @return {@code true} when the command has a meaningful world
     * position
     */
    boolean hasMeaningfulPosition();

    boolean isConsole();

    boolean isCommandBlock();

    boolean isBlockSource();
}
