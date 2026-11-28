package com.proxtextchat;

import com.proxtextchat.network.NetworkNode;
import com.proxtextchat.network.soucekeys.MessageSourceKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a message sent by an entity with additional metadata, such as alias and a trace.
 */
public class Message {
    private final MessageSourceKey TrueSender;

    private final Text Alias;

    private final Text Message;

    private List<NetworkNode> Trace;

    private final Instant SendTime;

    /**
     * Constructs a new Message with the given sender, alias, and message content.
     *
     * @param trueSender the entity sending the message.
     * @param alias the alias to be displayed with the message.
     * @param message the content of the message.
     */
    public Message(MessageSourceKey trueSender, Text alias, Text message){
        TrueSender = trueSender;
        Alias = alias;
        Message = message;
        Trace = new ArrayList<>();

        SendTime = Instant.now();
    }


    /**
     * Adds a network node to the trace of the message.
     *
     * @param node the network node to be added to the trace.
     */
    public void AddStep(NetworkNode node){
        Trace.add(node);
    }

    /**
     * Sets the entire trace of the message to a given list of network nodes.
     *
     * @param trace the list of network nodes representing the trace.
     */
    public void AddTrace(List<NetworkNode> trace){
        Trace = trace;
    }

    public Text getMessageContent(){
        return Message;
    }

    /**
     * Gets the formatted message, including the alias and the message content.
     *
     * @return the formatted message as a Text object.
     */
    public Text getMessage() {
        return Text.literal("[")
                   .append(Alias)
                   .append("] ")
                   .formatted(Formatting.GOLD)
                   .append(Message);
    }

    public MessageSourceKey getTrueSender() {
        return TrueSender;
    }

    public Text getAlias(){return Alias;}

    public List<NetworkNode> getTrace() {
        return Trace;
    }

    public Instant getSendTime() {
        return SendTime;
    }
}
