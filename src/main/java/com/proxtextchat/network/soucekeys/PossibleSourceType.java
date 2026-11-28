package com.proxtextchat.network.soucekeys;

import com.mojang.serialization.Codec;

public enum PossibleSourceType {
    /**
     * Any player
     */
    Player,
    /**
     * Any non-player entity
     */
    Entity,

    /**
     * any block, including command blocks, signs, and lecterns.
     */
    Block,

    /**
     * Any server console.
     */
    ServerConsole,

    /**
     * anything else.
     */
    UnknownType;

    public static final Codec<PossibleSourceType> CODEC =
        Codec.STRING.xmap(
            PossibleSourceType::valueOf,
            PossibleSourceType::name
        );
}
