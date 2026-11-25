package com.proxtextchat.mixin.ServerCommandSouce;

interface ServerCommandSourceMutation {
    /**
     * Sets internally tracked position metadata on a transformed
     * command source.
     *
     * @param value whether the source has a meaningful position
     */
    void setMeaningfulPosition(boolean value);
}
