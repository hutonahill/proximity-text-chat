package com.proxtextchat.network.soucekeys;

import com.mojang.serialization.Codec;

import java.util.Objects;

public class ConsoleSourceKey extends MessageSourceKey {
    public static final ConsoleSourceKey INSTANCE = new ConsoleSourceKey();

    public static final Codec<ConsoleSourceKey> CODEC =
        Codec.unit(INSTANCE);

    @Override
    public PossibleSourceType getType() {
        return PossibleSourceType.ServerConsole;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), 0);
    }

    @Override
    protected boolean hasSameIdentity(MessageSourceKey key) {
        return key == this;
        }

    private ConsoleSourceKey(){}
}
