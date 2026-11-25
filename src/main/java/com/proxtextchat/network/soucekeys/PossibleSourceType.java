package com.proxtextchat.network.soucekeys;

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
    UnknownType
}
