package com.proxtextchat.network.soucekeys;

import java.util.Objects;

public class ConsoleSourceKey extends MessageSouceKey{
    @Override
    public PossibleSourceType getType() {
        return PossibleSourceType.ServerConsole;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), 0);
    }

    @Override
    protected boolean hasSameIdentity(MessageSouceKey key) {
        return key == this;
        }

    private ConsoleSourceKey(){}

    public static final ConsoleSourceKey INSTANCE = new ConsoleSourceKey();
}
