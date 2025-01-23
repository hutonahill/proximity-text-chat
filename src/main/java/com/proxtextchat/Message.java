package com.proxtextchat;

import com.proxtextchat.network.NetworkNode;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Message {
    private final Entity TrueSender;

    private final String Alias;

    private final Text Message;

    private List<NetworkNode> Trace;

    private final Instant SendTime;

    Message(Entity trueSender, String alias, Text message){
        TrueSender = trueSender;
        Alias = alias;
        Message = message;
        Trace = new ArrayList<>();

        SendTime = Instant.now();
    }

    Message(Entity trueSender, Text message){
        this(trueSender, trueSender.getName().toString(), message);
    }

    public void AddStep(NetworkNode node){
        Trace.add(node);
    }

    public void AddTrace(List<NetworkNode> trace){
        Trace = trace;
    }

    public Text getMessage() {
        return Message;
    }

    public Entity getTrueSender() {
        return TrueSender;
    }

    public List<NetworkNode> getTrace() {
        return Trace;
    }

    public Instant getSendTime() {
        return SendTime;
    }
}
