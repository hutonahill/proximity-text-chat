package com.proxtextchat.network;

/**
 * Thrown when a there is a problem with a valid channel such as un-rechignized channel,
 * or trying to add a node to a channel but thenode's channel doesn't match.
 */
public class ChannelMismatch extends Exception {
    public ChannelMismatch(String message) {
        super(message);
    }
}
