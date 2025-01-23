package com.proxtextchat.network;

import com.proxtextchat.Message;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;

/**
 * manages all channels.
 */
public class ChannelManager {

    private HashMap<Identifier, NetworkGraph> Graphs = new HashMap<>();

    public void addNode(NetworkNode node){
        // check that we have the channel
        NetworkGraph graph;

        if (Graphs.containsKey(node.getChannel())){
            graph = Graphs.get(node.getChannel());

            graph.AddNode(node);
        }
        else{

            HashSet<NetworkNode> tempSet = new HashSet<>();
            tempSet.add(node);
            try {
                Graphs.put(node.getChannel(), new NetworkGraph(tempSet));
            } catch (ChannelMismatch e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void RemoveNode(NetworkNode node){
        if(Graphs.containsKey((node.getChannel()))){
            NetworkGraph graph = Graphs.get(node.getChannel());

            
            graph.RemoveNode(node);
        }
    }


    // stores subscriptions.
    // every node has a unique id, this allows to store a set of methods to be fired whenever we send a message.
    private static final HashMap<Integer, HashSet<Consumer<Message>>> NodeMessageEvent = new HashMap<>();

    public static void SubscribeToNodeMessage(NetworkNode node, Consumer<Message> method){
        // if there is no set in this slot, add one to avoid a nullptr
        if (!NodeMessageEvent.containsKey(node.getID())){
            NodeMessageEvent.put(node.getID(), new HashSet<>());
        }

        NodeMessageEvent.get(node.getID()).add(method);
    }

    public static void UnsubscribeToNodeMessage(NetworkNode node, Consumer<Message> method){
        if(NodeMessageEvent.containsKey(node.getID())){
            NodeMessageEvent.get(node.getID()).remove(method);
            // if a slot is empty, remove it from the registry to keep our trigger maximally efficient.
            if(NodeMessageEvent.get(node.getID()).isEmpty()){
                NodeMessageEvent.remove(node.getID());
            }
        }
    }

    public static void TriggerNodeMessage(NetworkNode node, Message message){
        if(NodeMessageEvent.containsKey(node.getID())){
            for (Consumer<Message> method : NodeMessageEvent.get(node.getID())){
                method.accept(message);
            }
        }
    }


}
