package com.proxtextchat.network;

/**
 * Thrown when a there is a problem with a valid channel such as unrecognized channel,
 * or is trying to add a node to a channel, but the node's channel doesn't match.
 */
public class ChannelMismatch extends Exception {
    /**
     * Constructs a new {@link ChannelMismatch} exception with the specified detail message.
     * The message provides information about the cause of the exception.
     *
     * @param message the detail message, which provides additional information about the error
     *                (e.g., "Node's channel does not match the expected channel").
     */
    public ChannelMismatch(String message) {
        super(message);
    }
}
