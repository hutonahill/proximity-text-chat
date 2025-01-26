package com.proxtextchat.network;

import com.proxtextchat.Message;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;


/**
 * manages all channels
 */
public class ChannelManager {

    /**
     * The singleton instance of the ChannelManager, providing access to the channel management system.
     * This instance is used to register, manage, and send messages between nodes within channels.
     */
    public static final ChannelManager Instance = new ChannelManager();

    private ChannelManager(){}

    private static final HashMap<Identifier, Channel> Graphs = new HashMap<>();

    /**
     * @param node Adds a node to the ChannelManager.
     *             Will automatically create a new channel if the node's channel is not registered with the ChannelManager.
     */
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

    /**
     * @param node Will remove the node from the channel if it was present.
     *             If the last node is removed from the channel, will remove the channel.
     */
    public void RemoveNode(@NotNull NetworkNode node){
        if(Graphs.containsKey((node.getChannelId()))){
            Channel graph = Graphs.get(node.getChannelId());
            
            graph.RemoveNode(node);

            if (graph.isEmpty()){
                Graphs.remove(node.getChannelId());
            }
        }
    }


    // stores subscriptions.
    // every node has a unique id, this allows storing a set of methods to be fired whenever we send a message.
    private static final HashMap<Integer, HashSet<Consumer<Message>>> NodeMessageEvent = new HashMap<>();

    /**
     * Retrieves a set of nodes that are receiving messages in the specified chunk from all channels.
     *
     * @param chunk the world chunk where the nodes are located.
     * @return a HashSet of nodes from all channels that receive at the specified chunk.
     * @throws RuntimeException if a ChannelMismatch occurs during processing.
     */
    public @NotNull HashSet<NetworkNode> NodesReceivingInChunk(@NotNull WorldChunk chunk){
        try {
            return NodesReceivingInChunk(chunk, Graphs.keySet());
        } catch (ChannelMismatch e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves a set of nodes that are receiving messages in the specified chunk for a given set of channels.
     *
     * @param chunk the world chunk where the nodes are located.
     * @param channels a set of channels to search for nodes receiving messages.
     * @return a HashSet of nodes from the specified channels that are receiving at the given chunk.
     * @throws ChannelMismatch when a provided channel is not registered with the ChannelManager.
     */
    public @NotNull HashSet<NetworkNode> NodesReceivingInChunk(@NotNull WorldChunk chunk, @NotNull Set<Identifier> channels) throws ChannelMismatch {
        HashSet<NetworkNode> output = new HashSet<>();

        // loop though the listed channels.
        for(Identifier channel : channels){
            // make sure the current channel is registered.
            if(!Graphs.containsKey(channel)){
                throw new ChannelMismatch("Could not find channel " + channel.toString());
            }

            // get the nodes that receive for this channel.
            Map<WorldChunk, HashSet<NetworkNode>> nodeReceivingRegistry = Graphs.get(channel).getNodeReceivingRegistry();

            // add those nodes that receive at chunk to the output set.
            if(nodeReceivingRegistry.containsKey(chunk)){
                output.addAll(nodeReceivingRegistry.get(chunk));
            }
        }

        return  output;
    }


    /**
     * Registers a player who will send messages to a specified channel.
     *
     * @param player the player to register.
     * @param channel the channel to register the player to.
     * @throws ChannelMismatch if the channel is not registered with the ChannelManager.
     */
    public void addReceivingFromPlayer(@NotNull PlayerEntity player, @NotNull Identifier channel) throws ChannelMismatch {
        if(Graphs.containsKey(channel)){
            Channel graph = Graphs.get(channel);

            graph.addReceivingFromPlayer(player);
        }
        else{
            throw new ChannelMismatch("No registered channel with that Identifier.");
        }
    }

    /**
     * Registers a player who will be sent messages from a specified channel.
     *
     * @param player the player to register.
     * @param channel the channel to register the player to.
     * @throws ChannelMismatch if the channel is not registered with the ChannelManager.
     */
    public void addSendToPlayer(@NotNull PlayerEntity player, @NotNull Identifier channel) throws ChannelMismatch {
        if(Graphs.containsKey(channel)){
            Channel graph = Graphs.get(channel);

            graph.addSendToPlayer(player);
        }
        else{
            throw new ChannelMismatch("No registered channel with that Identifier.");
        }
    }


    /**
     * Registers a player to receive messages from a collection of channels.
     *
     * @param player the player to register.
     * @param channels the collection of channels to register the player to.
     * @throws ChannelMismatch if any channel in the collection is not registered with the ChannelManager.
     */
    public void addReceivingFromPlayer(@NotNull PlayerEntity player, @NotNull Collection<Identifier> channels) throws ChannelMismatch{
        for(Identifier channel : channels){
            addReceivingFromPlayer(player, channel);
        }
    }

    /**
     * Registers a player to send messages to a collection of channels.
     *
     * @param player the player to register.
     * @param channels the collection of channels to register the player to.
     * @throws ChannelMismatch if any channel in the collection is not registered with the ChannelManager.
     */
    public void addSendToPlayer(@NotNull PlayerEntity player, @NotNull Collection<Identifier> channels) throws  ChannelMismatch{
        for(Identifier channel : channels){
            addSendToPlayer(player, channel);
        }
    }


    /**
     * Removes a player from receiving messages from all registered channels.
     *
     * @param player the player to remove.
     */
    public void removeReceivingFromPlayerEverywhere(@NotNull PlayerEntity player) {
        for(Identifier channel : Graphs.keySet()){
            Graphs.get(channel).removeReceivingFromPlayer(player);
        }
    }

    /**
     * Removes a player from sending messages to all registered channels.
     *
     * @param player the player to remove.
     */
    public void removeSendToPlayerEverywhere(@NotNull PlayerEntity player){
        for(Identifier channel : Graphs.keySet()){
            Graphs.get(channel).removeReceivingFromPlayer(player);
        }
    }


    /**
     * Removes a player from receiving messages from a specified channel.
     *
     * @param player the player to remove.
     * @param channel the channel to remove the player from.
     * @throws ChannelMismatch if the channel is not registered with the ChannelManager.
     */
    public void removeReceivingFromPlayer(@NotNull PlayerEntity player, @NotNull Identifier channel) throws ChannelMismatch{
        if(Graphs.containsKey(channel)){
            Channel graph = Graphs.get(channel);

            graph.removeReceivingFromPlayer(player);
        }
        else{
            throw new ChannelMismatch("No registered channel with that Identifier.");
        }
    }

    /**
     * Removes a player from sending messages to a specified channel.
     *
     * @param player the player to remove.
     * @param channel the channel to remove the player from.
     * @throws ChannelMismatch if the channel is not registered with the ChannelManager.
     */
    public void removeSendToPlayer(@NotNull PlayerEntity player, @NotNull Identifier channel) throws ChannelMismatch{
        if(Graphs.containsKey(channel)){
            Channel graph = Graphs.get(channel);

            graph.removeSendToPlayer(player);
        }
        else{
            throw new ChannelMismatch("No registered channel with that Identifier.");
        }
    }


    /**
     * Removes a player from receiving messages from a collection of channels.
     *
     * @param player the player to remove.
     * @param channels the collection of channels to remove the player from.
     * @throws ChannelMismatch if any channel in the collection is not registered with the ChannelManager.
     */
    public void removeReceivingFromPlayer(@NotNull PlayerEntity player, @NotNull Collection<Identifier> channels)  throws ChannelMismatch{
        for(Identifier channel : channels){
            removeReceivingFromPlayer(player, channel);
        }
    }

    /**
     * Removes a player from sending messages to a collection of channels.
     *
     * @param player the player to remove.
     * @param channels the collection of channels to remove the player from.
     * @throws ChannelMismatch if any channel in the collection is not registered with the ChannelManager.
     */
    public void removeSendTOPlayer(@NotNull PlayerEntity player, @NotNull Collection<Identifier> channels) throws ChannelMismatch{
        for(Identifier channel : channels){
            removeSendToPlayer(player, channel);
        }
    }


    /**
     * Retrieves the set of channels from which a player is registered to receive messages.
     *
     * @param player the player whose receiving channels are to be retrieved.
     * @return a set of channel identifiers representing the channels the player is receiving from.
     */
    public @NotNull HashSet<Identifier> getReceivingFromChannelsForPlayer(@NotNull PlayerEntity player){
        HashSet<Identifier> output = new HashSet<>();
        for(Identifier channel : Graphs.keySet()){
            if(Graphs.get(channel).hasReceivingFromPlayer(player)){
                output.add(channel);
            }
        }

        return output;
    }

    /**
     * Retrieves the set of channels to which a player is registered to send messages.
     *
     * @param player the player whose sending channels are to be retrieved.
     * @return a set of channel identifiers representing the channels the player is sending to.
     */
    public @NotNull HashSet<Identifier> getSendToChannelsForPlayer(@NotNull PlayerEntity player){
        HashSet<Identifier> output = new HashSet<>();
        for(Identifier channel : Graphs.keySet()){
            if(Graphs.get(channel).hasSendToPlayer(player)){
                output.add(channel);
            }
        }

        return output;
    }


    /**
     * Subscribes a method to receive messages for a specific network node.
     * This method adds the provided consumer to the registry,
     * allowing it to be triggered when messages are sent to the node.
     * Designed for non-players.
     *
     * @param node the network node to subscribe to.
     * @param method the method to be invoked when a message is received by the node.
     */
    public static void SubscribeToNodeMessage(@NotNull NetworkNode node, @NotNull Consumer<Message> method){
        // if there is no set in this slot, add one to avoid a nullptr
        if (!NodeMessageEvent.containsKey(node.getID())){
            NodeMessageEvent.put(node.getID(), new HashSet<>());
        }

        NodeMessageEvent.get(node.getID()).add(method);
    }

    /**
     * Unsubscribes a method from receiving messages for a specific network node.
     * This method removes the provided consumer from the registry,
     * so it will no longer be triggered when messages are sent to the node.
     * If there are no more subscribed methods for a node, it will be removed from the registry for efficiency.
     *
     * @param node the network node to unsubscribe from.
     * @param method the method to be removed from the subscription list.
     */
    public static void UnsubscribeToNodeMessage(@NotNull NetworkNode node, @NotNull Consumer<Message> method){
        if(NodeMessageEvent.containsKey(node.getID())){
            NodeMessageEvent.get(node.getID()).remove(method);
            // if a slot is empty, remove it from the registry to keep our trigger maximally efficient.
            if(NodeMessageEvent.get(node.getID()).isEmpty()){
                NodeMessageEvent.remove(node.getID());
            }
        }
    }


    /**
     * Sends a direct message from a source node to a destination node, provided both nodes are in the same channel.
     *
     * @param source the source node from which the message is sent.
     * @param destination the destination node to which the message is sent.
     * @param message the message to be sent.
     * @return {@code true} if the message was successfully delivered;
     * {@code false} if no valid path exists between the nodes.
     * @throws ChannelMismatch if the source and destination nodes are not in the same channel.
     * @throws IllegalArgumentException if the source node's channel is not registered with the ChannelManager or
     * the source/destination node is not found in the channel.
     */
    public static boolean directMessage(@NotNull NetworkNode source, @NotNull NetworkNode destination, @NotNull Message message) throws ChannelMismatch {
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

    /**
     * Broadcasts a message from a source node to all nodes in the same channel that it is connected to.
     *
     * @param source the source node from which the message is broadcast.
     * @param message the message to broadcast.
     * @throws IllegalArgumentException if the source node's channel is not registered with the ChannelManager or
     * the source node is not found in the channel.
     */
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

        // now deliver the messages to players

        Set<PlayerEntity> registeredPlayers = Graphs.get(node.getChannelId()).getSendToPlayerRegistry();

        for(PlayerEntity player : registeredPlayers){
            // filter out offline players
            World world = player.getWorld();
            if (world != null && player.getServer() != null) {

                // Get the player's current chunk coordinates
                int chunkX = player.getBlockPos().getX();
                int chunkZ = player.getBlockPos().getZ();

                // Retrieve the chunk from the world
                WorldChunk playerChunk = world.getChunk(chunkX, chunkZ);

                if(node.getRange().contains(playerChunk)){
                    player.sendMessage(message.getMessage());
                }
            }
        }
    }
}