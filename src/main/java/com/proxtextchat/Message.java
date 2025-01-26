package com.proxtextchat;

import com.proxtextchat.network.NetworkNode;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Message {
    private final Entity TrueSender;

    private final Text Alias;

    private final Text Message;

    private List<NetworkNode> Trace;

    private final Instant SendTime;

    public Message(Entity trueSender, Text alias, Text message){
        TrueSender = trueSender;
        Alias = alias;
        Message = message;
        Trace = new ArrayList<>();

        SendTime = Instant.now();
    }

    Message(Entity trueSender, Text message){
        this(trueSender, trueSender.getName(), message);
    }

    public void AddStep(NetworkNode node){
        Trace.add(node);
    }

    public void AddTrace(List<NetworkNode> trace){
        Trace = trace;
    }

    public Text getMessageContent(){
        return Message;
    }

    public Text getMessage() {
        return Text.literal("[" + Alias + "] ").formatted(Formatting.GOLD).append(Message);
    }

    public Entity getTrueSender() {
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
