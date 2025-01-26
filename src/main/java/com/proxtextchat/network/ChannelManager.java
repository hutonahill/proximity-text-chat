package com.proxtextchat.network;

import com.proxtextchat.Message;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;

import java.util.*;
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

        if (Graphs.containsKey(node.getChannelId())){
            Channel graph = Graphs.get(node.getChannelId());

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
                Graphs.put(node.getChannelId(), new Channel(tempSet));
            } catch (ChannelMismatch e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void RemoveNode(@NotNull NetworkNode node){
        if(Graphs.containsKey((node.getChannelId()))){
            Channel graph = Graphs.get(node.getChannelId());

            
            graph.RemoveNode(node);
        }
    }


    // stores subscriptions.
    // every node has a unique id, this allows storing a set of methods to be fired whenever we send a message.
    private static final HashMap<Integer, HashSet<Consumer<Message>>> NodeMessageEvent = new HashMap<>();

    public HashSet<NetworkNode> NodesInChunk(WorldChunk chunk, Set<Identifier> channels) throws ChannelMismatch {
        HashSet<NetworkNode> output = new HashSet<>();
        for(Identifier channel : channels){
            if(!Graphs.containsKey(channel)){
                throw new ChannelMismatch("Could not find channel " + channel.toString());
            }

            Map<WorldChunk, HashSet<NetworkNode>> nodeReceivingRegistry = Graphs.get(channel).getNodeReceivingRegistry();

            if(nodeReceivingRegistry.containsKey(chunk)){
                output.addAll(nodeReceivingRegistry.get(chunk));
            }
        }

        return  output;
    }



    public void addReceivingFromPlayer(@NotNull PlayerEntity player, @NotNull Identifier channel) throws ChannelMismatch {
        if(Graphs.containsKey(channel)){
            Channel graph = Graphs.get(channel);

            graph.addReceivingFromPlayer(player);
        }
        else{
            throw new ChannelMismatch("No registered channel with that Identifier.");
        }
    }

    public void addSendToPlayer(@NotNull PlayerEntity player, @NotNull Identifier channel) throws ChannelMismatch {
        if(Graphs.containsKey(channel)){
            Channel graph = Graphs.get(channel);

            graph.addSendToPlayer(player);
        }
        else{
            throw new ChannelMismatch("No registered channel with that Identifier.");
        }
    }


    public void addReceivingFromPlayer(@NotNull PlayerEntity player, @NotNull Collection<Identifier> channels) throws ChannelMismatch{
        for(Identifier channel : channels){
            addReceivingFromPlayer(player, channel);
        }
    }

    public void addSendToPlayer(@NotNull PlayerEntity player, @NotNull Collection<Identifier> channels) throws  ChannelMismatch{
        for(Identifier channel : channels){
            addSendToPlayer(player, channel);
        }
    }


    public void removeReceivingFromPlayerEverywhere(@NotNull PlayerEntity player) {
        for(Identifier channel : Graphs.keySet()){
            Graphs.get(channel).removeReceivingFromPlayer(player);
        }
    }

    public void removeSendToPlayerEverywhere(@NotNull PlayerEntity player){
        for(Identifier channel : Graphs.keySet()){
            Graphs.get(channel).removeReceivingFromPlayer(player);
        }
    }


    public void removeReceivingFromPlayer(@NotNull PlayerEntity player, @NotNull Identifier channel) throws ChannelMismatch{
        if(Graphs.containsKey(channel)){
            Channel graph = Graphs.get(channel);

            graph.removeReceivingFromPlayer(player);
        }
        else{
            throw new ChannelMismatch("No registered channel with that Identifier.");
        }
    }

    public void removeSendToPlayer(@NotNull PlayerEntity player, @NotNull Identifier channel) throws ChannelMismatch{
        if(Graphs.containsKey(channel)){
            Channel graph = Graphs.get(channel);

            graph.removeSendToPlayer(player);
        }
        else{
            throw new ChannelMismatch("No registered channel with that Identifier.");
        }
    }


    public void removeReceivingFromPlayer(@NotNull PlayerEntity player, @NotNull Collection<Identifier> channels)  throws ChannelMismatch{
        for(Identifier channel : channels){
            removeReceivingFromPlayer(player, channel);
        }
    }

    public void removeSendTOPlayer(@NotNull PlayerEntity player, @NotNull Collection<Identifier> channels) throws ChannelMismatch{
        for(Identifier channel : channels){
            removeSendToPlayer(player, channel);
        }
    }


    public HashSet<Identifier> getReceivingFromChannelsForPlayer(@NotNull PlayerEntity player){
        HashSet<Identifier> output = new HashSet<>();
        for(Identifier channel : Graphs.keySet()){
            if(Graphs.get(channel).hasReceivingFromPlayer(player)){
                output.add(channel);
            }
        }

        return output;
    }

    public HashSet<Identifier> getSendToChannelsForPlayer(@NotNull PlayerEntity player){
        HashSet<Identifier> output = new HashSet<>();
        for(Identifier channel : Graphs.keySet()){
            if(Graphs.get(channel).hasSendToPlayer(player)){
                output.add(channel);
            }
        }

        return output;
    }


    // these two are used for non-players.
    public static void SubscribeToNodeMessage(@NotNull NetworkNode node, @NotNull Consumer<Message> method){
        // if there is no set in this slot, add one to avoid a nullptr
        if (!NodeMessageEvent.containsKey(node.getID())){
            NodeMessageEvent.put(node.getID(), new HashSet<>());
        }

        NodeMessageEvent.get(node.getID()).add(method);
    }

    public static void UnsubscribeToNodeMessage(@NotNull NetworkNode node, @NotNull Consumer<Message> method){
        if(NodeMessageEvent.containsKey(node.getID())){
            NodeMessageEvent.get(node.getID()).remove(method);
            // if a slot is empty, remove it from the registry to keep our trigger maximally efficient.
            if(NodeMessageEvent.get(node.getID()).isEmpty()){
                NodeMessageEvent.remove(node.getID());
            }
        }
    }


    public static boolean sendMessage(@NotNull NetworkNode source, @NotNull NetworkNode destination, @NotNull Message message) throws ChannelMismatch {
        if (source.getChannelId() != destination.getChannelId()){
            throw new ChannelMismatch("Nodes must have the same channel to send messages between them.");
        }

        if(!Graphs.containsKey(source.getChannelId())){
            throw new IllegalArgumentException("Channel not registered.");
        }

        Channel graph = Graphs.get(source.getChannelId());

        if (graph.hasNode(source)){
            throw new IllegalArgumentException("source node not found.");
        }

        if (graph.hasNode(destination)){
            throw new IllegalArgumentException("destination node not found.");
        }

        ArrayList<NetworkNode> path = graph.DirectMessage(source, destination);
        
        if(path != null){
            message.AddTrace(path);

            TriggerNodeMessage(destination, message);
            
            return true;
        }
        
        // when there is no valid path from source to destination.
        return false;
    }

    public void broadcastMessage(@NotNull NetworkNode source, Message message){
        if(!Graphs.containsKey(source.getChannelId())){
            throw new IllegalArgumentException("Channel not registered.");
        }

        Channel graph = Graphs.get(source.getChannelId());

        if (graph.hasNode(source)){
            throw new IllegalArgumentException("source node not found.");
        }

        HashMap<NetworkNode, ArrayList<NetworkNode>> paths = graph.BroadcastPaths(source);

        // we send a message to each node the source is connected to.
        for (NetworkNode destination : paths.keySet()){
            Message newMessage = new Message(message.getTrueSender(), message.getAlias(), message.getMessage());

            newMessage.AddTrace(paths.get(destination));

            TriggerNodeMessage(destination, newMessage);
        }
    }


    private static void TriggerNodeMessage(@NotNull NetworkNode node, @NotNull Message message){
        if(NodeMessageEvent.containsKey(node.getID())){
            for (Consumer<Message> method : NodeMessageEvent.get(node.getID())){
                method.accept(message);
            }
        }
    }
}