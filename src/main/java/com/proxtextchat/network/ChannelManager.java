package com.proxtextchat.network;

import com.proxtextchat.Message;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;

/**
 * manages all channels.
 */
public class ChannelManager {

    public static final ChannelManager Instance = new ChannelManager();

    private ChannelManager(){}

    private static final HashMap<Identifier, Channel> Graphs = new HashMap<>();

    public void addNode(@NotNull NetworkNode node){
        // check that we have the channel
        Channel graph;

        if (Graphs.containsKey(node.getChannel())){
            graph = Graphs.get(node.getChannel());

            try {
                graph.AddNode(node);
            } catch (ChannelMismatch e) {
                throw new RuntimeException(e);
            }
        }
        else{
            HashSet<NetworkNode> tempSet = new HashSet<>();
            tempSet.add(node);
            try {
                Graphs.put(node.getChannel(), new Channel(tempSet));
            } catch (ChannelMismatch e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void RemoveNode(@NotNull NetworkNode node){
        if(Graphs.containsKey((node.getChannel()))){
            Channel graph = Graphs.get(node.getChannel());

            
            graph.RemoveNode(node);
        }
    }


    // stores subscriptions.
    // every node has a unique id, this allows to store a set of methods to be fired whenever we send a message.
    private static final HashMap<Integer, HashSet<Consumer<Message>>> NodeMessageEvent = new HashMap<>();

    public void addPlayer(PlayerEntity player, Identifier channel) throws ChannelMismatch {
        if(Graphs.containsKey(channel)){
            Channel graph = Graphs.get(channel);

            graph.addPlayer(player);
        }
        else{
            throw new ChannelMismatch("No registered channel with that Identifier.");
        }
    }



    public void addPlayer(PlayerEntity player, Collection<Identifier> channels) throws ChannelMismatch{
        for(Identifier channel : channels){
            addPlayer(player, channel);
        }
    }

    public void removePlayerEverywhere(PlayerEntity player) {
        for(Identifier channel : Graphs.keySet()){
            Graphs.get(channel).removePlayer(player);
        }
    }

    public void removePlayer(PlayerEntity player, Identifier channel) throws ChannelMismatch{
        if(Graphs.containsKey(channel)){
            Channel graph = Graphs.get(channel);

            graph.removePlayer(player);
        }
        else{
            throw new ChannelMismatch("No registered channel with that Identifier.");
        }
    }

    public void removePlayer(PlayerEntity player, Collection<Identifier> channels)  throws ChannelMismatch{
        for(Identifier channel : channels){
            removePlayer(player, channel);
        }
    }

    public HashSet<Identifier> getChannelsForPlayer(PlayerEntity player){
        HashSet<Identifier> output = new HashSet<>();
        for(Identifier channel : Graphs.keySet()){
            if(Graphs.get(channel).hasPlayer(player)){
                output.add(channel);
            }
        }

        return output;
    }

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
