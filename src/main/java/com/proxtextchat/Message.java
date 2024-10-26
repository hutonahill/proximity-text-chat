package com.proxtextchat;

import com.proxtextchat.network.NetworkNode;
import com.proxtextchat.network.NetworkNodeNBT;
import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class Message {
    private final Entity TrueSender;

    private final String Alias;

    private final String Message;

    private List<NetworkNode> Trace;

    Message(Entity trueSender, String alias, String message){
        TrueSender = trueSender;
        Alias = alias;
        Message = message;
        Trace = new ArrayList<>();
    }

    public void Add(NetworkNode node){
        Trace.add(node);
    }

    public String getMessage() {
        return Message;
    }

    public Entity getTrueSender() {
        return TrueSender;
    }

    public List<NetworkNode> getTrace() {
        return Trace;
    }
}
