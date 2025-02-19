package com.proxtextchat.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;

import java.lang.reflect.Array;
import java.util.*;

/**
 * A network of nodes and tools for passing messages between them.
 */
public class Channel {
    private final DirectedMultigraph<NetworkNode, DefaultEdge> Graph = new DirectedMultigraph<>(DefaultEdge.class);

    private final HashMap<ChunkReferance, HashSet<NetworkNode>> NodeReceivingRegistry = new HashMap<>();

    private final HashSet<PlayerEntity> ReceiveFromPlayerRegistry = new HashSet<>();

    private final HashSet<PlayerEntity> SendToPlayerRegistry = new HashSet<>();

    private Identifier ID = null;

    private static final String NodeListKey = "NodeList";

    public static final Codec<Channel> CODEC;

    static{
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
                NetworkNode.CODEC.listOf().fieldOf(NodeListKey).validate(Channel::ValidateNodeList).forGetter(Channel::getNodeList)
        ).apply(instance, inner-> {
            try {
                return new Channel(inner);
            } catch (ChannelMismatch e) {
                throw new RuntimeException(e);
            }
        }));
    }

    private static DataResult<List<NetworkNode>> ValidateNodeList(List<NetworkNode> nodes){
        Identifier channel = null;

        for(NetworkNode node : nodes){
            if(channel != null){
                if(node.getChannelId() != channel){
                    return DataResult.error(()->"Channel mismatch in the node list. All nodes must have the same channel.");
                }
            }
            else{
                channel = node.getChannelId();
            }
        }

        return DataResult.success(nodes);
    }

    private ArrayList<NetworkNode> getNodeList(){
        return new ArrayList<>(Graph.vertexSet());
    }

    public Channel(Collection<NetworkNode> nodes) throws ChannelMismatch {
        this(new HashSet<>(nodes));
    }

    /**
     * Constructs a Channel by adding the provided nodes to the graph and establishing connections
     * between them based on their receiving chunks.
     * Ensures that all nodes have the same channel ID.
     * The nodes are added to the graph and connections are made between them if they share common chunks.
     *
     * @param nodes the set of nodes to be added to the channel.
     * @throws ChannelMismatch if any node does not match the expected channel.
     */
    public Channel(HashSet<NetworkNode> nodes) throws ChannelMismatch {

        // a registry of nodes that lets us avoid looping though the
        // node set a second time.

        // loop though all input nodes and add them to the graph
        for (NetworkNode node : nodes){

            // make sure all nodes have the same channel.
            if(ID != null){
                if(!node.getChannelId().equals(ID)){
                    throw new ChannelMismatch("Node at" + node.getReceivingChunks() + "doesn't match the expected channel");
                }
            }

            // if our channel var is null, we fill it
            else{
                ID = node.getChannelId();
            }

            // if we haven't thrown an exception, add the node to the graph.
            Graph.addVertex();
            for(ChunkReferance chunk : node.getReceivingChunks()){
                // Remember where the node is
                if(!NodeReceivingRegistry.containsKey(chunk)){
                    NodeReceivingRegistry.put(chunk, new HashSet<>());
                }

                NodeReceivingRegistry.get(chunk).add(node);

            }
        }

        // determine which nodes should be connected to each other
        for (NetworkNode node1 : nodes) {
            Set<ChunkReferance> chunks = node1.getRangeChunks();

            Set<ChunkReferance> intersection = new HashSet<>(chunks);

            intersection.retainAll(NodeReceivingRegistry.keySet());

            // loop though all the chunks in Node1
            for (ChunkReferance chunk : intersection) {

                // if there is, loop though all nodes in that chunk
                for (NetworkNode node2 : NodeReceivingRegistry.get(chunk)){
                    // dont connect to ourself
                    if (node2 != node1) {
                        // If node2 is in a chunk controlled by node1, add an edge
                        Graph.addEdge(node1, node2);
                    }
                }

            }
        }
    }

    /**
     * Adds a node to the graph and establishes connections for it based on its range and receiving chunks.
     *
     * @param node the node to be added to the graph.
     * @throws ChannelMismatch if the node's channel does not match the expected channel for the graph.
     */
    public void AddNode(@NotNull NetworkNode node) throws ChannelMismatch {

        // make sure the node is not already in the graph.
        if(Graph.containsVertex(node)){
            return;
        }

        // make sure the node is in the right channel.
        if(node.getChannelId() != ID){
            throw new ChannelMismatch("Node doss not match channel");
        }

        //first, we add the node to the graph
        Graph.addVertex(node);

        // then we establish outgoing connections from the node.
        for (ChunkReferance chunk : node.getRangeChunks()){
            if (NodeReceivingRegistry.containsKey(chunk)){
                for (NetworkNode node2 : NodeReceivingRegistry.get(chunk)){
                    Graph.addEdge(node, node2);
                }
            }
        }

        //now we register the location of the node
        for(ChunkReferance chunk : node.getReceivingChunks()){
            if (!NodeReceivingRegistry.containsKey(chunk)){
                NodeReceivingRegistry.put(chunk, new HashSet<>());
            }
            NodeReceivingRegistry.get(chunk).add(node);
        }



        // now we establish incoming connections
        for (NetworkNode node1 : Graph.vertexSet()){
            // if receiving and range have chunks in common...
            if(!Collections.disjoint(node.getReceivingChunks(), node1.getRangeChunks())){
                Graph.addEdge(node1, node);
            }
        }

        // finally, our ShortedPathRegistry is now out of date and will need to be recalculated.
        ShortestPathRegistry = new HashMap<>();

        PopulateShortestPathRegistry();
    }



    public void MurgeChannels(@NotNull Channel channel) throws ChannelMismatch{
        for(NetworkNode node : channel.getNodeList()){
            AddNode(node);
        }
    }
    /**
     * same as addNode, but creates the node instead of receiving a node object.
     * @param receivingChunks the range of chunks where the node can receive messages from
     * @param rangeChunks the range of chunks the node can send messages to.
     */
    public void NewNode(Set<ChunkReferance> receivingChunks, Set<ChunkReferance> rangeChunks){
        NetworkNode node = new NetworkNode(ID, receivingChunks, rangeChunks);

        try {
            AddNode(node);
        } catch (ChannelMismatch e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Removes a node from the graph and clears any associated data.
     *
     * @param node the node to be removed from the graph.
     */
    public void RemoveNode(@NotNull NetworkNode node){
        if(!Graph.containsVertex(node)){
            return;
        }

        Graph.removeVertex(node);

        ShortestPathRegistry = new HashMap<>();

        PopulateShortestPathRegistry();
    }

    /**
     * Checks if a node exists in the graph.
     *
     * @param node the node to check.
     * @return true if the node is in the graph, false otherwise.
     */
    public boolean hasNode(@NotNull NetworkNode node){
        return Graph.containsVertex(node);
    }

    private HashMap<NetworkNode /*origin*/, HashMap<NetworkNode/*destination*/, ArrayList<NetworkNode>/*path*/>>
            ShortestPathRegistry = new HashMap<>();

    /**
     * Retrieves the shortest path between the origin and destination nodes, if available,
     * from the shortest path registry.
     *
     * @param origin the starting node for the message.
     * @param destination the target node for the message.
     * @return an ArrayList of nodes representing the shortest path from origin to destination,
     *         or null if no path is found.
     */
    public ArrayList<NetworkNode> DirectMessage(@NotNull NetworkNode origin, @NotNull NetworkNode destination){
        PopulateShortestPathRegistry();

        if (!ShortestPathRegistry.containsKey(origin)) {
            return null;
        }

        return ShortestPathRegistry.get(origin).get(destination);
    }

    /**
     * Retrieves all broadcast paths from the origin node to all other reachable nodes,
     * using the shortest path registry.
     *
     * @param origin the starting node for the broadcast message.
     * @return a HashMap mapping each reachable node to its shortest path from the origin node,
     *         or null if no paths are found.
     */
    public HashMap<NetworkNode, ArrayList<NetworkNode>> BroadcastPaths(@NotNull NetworkNode origin){
        PopulateShortestPathRegistry();
        if (!ShortestPathRegistry.containsKey(origin)){
            return null;
        }

        return ShortestPathRegistry.get(origin);
    }


    //TODO: figure threading and implement here.
    private void PopulateShortestPathRegistry(){
        if (ShortestPathRegistry.isEmpty()){


            // for each node
            for (NetworkNode source : Graph.vertexSet()) {

                ShortestPathRegistry.put(source, computeShortestPaths(source));
            }
        }
    }

    private @NotNull HashMap<NetworkNode, ArrayList<NetworkNode>> computeShortestPaths(@NotNull NetworkNode source) {
        if (source.getChannelId() != ID){
            return new HashMap<>();
        }
        else if (Graph.containsVertex(source)){
            return new HashMap<>();
        }

        // Map to store the shortest path from the source to each node
        HashMap<NetworkNode, ArrayList<NetworkNode>> shortestPaths = new HashMap<>();

        // Map to store the minimum distance from the source to each node
        HashMap<NetworkNode, Double> distances = new HashMap<>();

        // Priority queue to process nodes in order of distance
        PriorityQueue<NetworkNode> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(distances::get));

        // Initialize distances to infinity and paths to empty
        for (NetworkNode node : Graph.vertexSet()) {
            distances.put(node, Double.POSITIVE_INFINITY);
            shortestPaths.put(node, new ArrayList<>());
        }

        // Set the distance to the source as 0
        distances.put(source, 0.0);
        priorityQueue.add(source);
        shortestPaths.get(source).add(source); // Source's path to itself is just itself

        // Dijkstra's algorithm, re implemented to save some compute time
        while (!priorityQueue.isEmpty()) {
            NetworkNode current = priorityQueue.poll();

            for (DefaultEdge edge : Graph.outgoingEdgesOf(current)) {
                NetworkNode neighbor = Graph.getEdgeTarget(edge);
                if (neighbor.equals(current)) {
                    neighbor = Graph.getEdgeSource(edge);
                }

                double weight = 1.0; // Adjust if your edges have weights
                double newDistance = distances.get(current) + weight;

                if (distances.containsKey(neighbor) && newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance);

                    // Update the path to the neighbor
                    ArrayList<NetworkNode> path = new ArrayList<>(shortestPaths.get(current));
                    path.add(neighbor);
                    shortestPaths.put(neighbor, path);

                    priorityQueue.add(neighbor);
                }
            }
        }

        return shortestPaths;
    }

    public @NotNull Map<ChunkReferance, HashSet<NetworkNode>> getNodeReceivingRegistry(){
        return Collections.unmodifiableMap(NodeReceivingRegistry);
    }

    public @NotNull Set<PlayerEntity> getReceiveFromPlayerRegistry(){
        return Collections.unmodifiableSet(ReceiveFromPlayerRegistry);
    }

    public @NotNull Set<PlayerEntity> getSendToPlayerRegistry() {
        return Collections.unmodifiableSet(SendToPlayerRegistry);
    }

    /**
     * Adds a player to the registry of players' Channel will receive messages.
     *
     * @param player the player to be added to the receiving registry.
     */
    public void addReceivingFromPlayer(@NotNull PlayerEntity player){
        ReceiveFromPlayerRegistry.add(player);
    }

    /**
     * Adds a player to the registry of players' Channel will send messages to.
     *
     * @param player the player to be added to the send to registry.
     */
    public void addSendToPlayer(@NotNull PlayerEntity player){
        SendToPlayerRegistry.add(player);
    }

    /**
     * Removes a player from the registry of players' Channel will receive messages.
     *
     * @param player the player to be removed from the receiving registry.
     */
    public void removeReceivingFromPlayer(@NotNull PlayerEntity player){
        ReceiveFromPlayerRegistry.remove(player);
    }

    /**
     * Removes a player from the registry of players' Channel will send messages to.
     *
     * @param player the player to be removed from the send to registry.
     */
    public void removeSendToPlayer(@NotNull PlayerEntity player){
        SendToPlayerRegistry.remove(player);
    }

    /**
     * Checks if a player is registered to receive messages from a channel.
     *
     * @param player the player to check.
     * @return true if the player is registered to receive messages, false otherwise.
     */
    public boolean hasReceivingFromPlayer(@NotNull PlayerEntity player){
        return ReceiveFromPlayerRegistry.contains(player);
    }

    /**
     * Checks if a player is registered to send messages to a channel.
     *
     * @param player the player to check.
     * @return true if the player is registered to send messages, false otherwise.
     */
    public boolean hasSendToPlayer(@NotNull PlayerEntity player){
        return SendToPlayerRegistry.contains(player);
    }


    public boolean isEmpty(){
        return Graph.vertexSet().isEmpty();
    }

    public Identifier getID(){
        return ID;
    }
}

