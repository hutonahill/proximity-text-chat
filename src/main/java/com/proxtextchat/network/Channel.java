package com.proxtextchat.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;

import java.util.*;

public class Channel {
    private final DirectedMultigraph<NetworkNode, DefaultEdge> Graph = new DirectedMultigraph<>(DefaultEdge.class);

    private final HashMap<WorldChunk, HashSet<NetworkNode>> NodeLocationRegistry = new HashMap<>();

    private final HashSet<PlayerEntity> ReceiveFromPlayerRegistry = new HashSet<>();

    private final HashSet<PlayerEntity> SendToPlayerRegistry = new HashSet<>();

    private Identifier ID = null;

    public Channel(HashSet<NetworkNode> nodes) throws ChannelMismatch {

        // a registry of nodes that lets us avoid looping though the
        // node set a second time.

        // loop though all input nodes and add them to the graph
        for (NetworkNode node : nodes){

            // make sure all nodes have the same channel.
            if(ID != null){
                if(!node.getChannel().equals(ID)){
                    throw new ChannelMismatch("Node at" + node.getLocation() + "doesn't match the expected channel");
                }
            }

            // if our channel var is null we fill it
            else{
                ID = node.getChannel();
            }

            // if we haven't thrown an exception, add the node to the graph.
            Graph.addVertex();

            // Remember where the node is
            if(!NodeLocationRegistry.containsKey(node.getLocation())){
                NodeLocationRegistry.put(node.getLocation(), new HashSet<>());
            }
            NodeLocationRegistry.get(node.getLocation()).add(node);
        }

        // determine which nodes should be connected to each other
        for (NetworkNode node1 : nodes) {
            Set<WorldChunk> chunks = node1.getRange();

            Set<WorldChunk> intersection = new HashSet<>(chunks);

            intersection.retainAll(NodeLocationRegistry.keySet());

            // loop though all the chunks in Node1
            for (WorldChunk chunk : intersection) {

                // if there is, loop though all nodes in that chunk
                for (NetworkNode node2 : NodeLocationRegistry.get(chunk)){
                    // dont connect to ourself
                    if (node2 != node1) {
                        // If node2 is in a chunk controlled by node1, add an edge
                        Graph.addEdge(node1, node2);
                    }
                }

            }
        }
    }

    public void AddNode(@NotNull NetworkNode node) throws ChannelMismatch {

        // make sure the node is not already in the graph.
        if(Graph.containsVertex(node)){
            return;
        }

        // make sure the node is in the right channel.
        if(node.getChannel() != ID){
            throw new ChannelMismatch("Node doss not match channel");
        }

        //first we add the node to the graph
        Graph.addVertex(node);

        // then we establish outgoing connections from the node.
        for (WorldChunk chunk : node.getRange()){
            if (NodeLocationRegistry.containsKey(chunk)){
                for (NetworkNode node2 : NodeLocationRegistry.get(chunk)){
                    Graph.addEdge(node, node2);
                }
            }
        }

        //now we register the location of the node
        if (!NodeLocationRegistry.containsKey(node.getLocation())){
            NodeLocationRegistry.put(node.getLocation(), new HashSet<>());
        }
        NodeLocationRegistry.get(node.getLocation()).add(node);

        // now we establish incoming connections
        for (NetworkNode node1 : Graph.vertexSet()){
            if(node1.getRange().contains(node.getLocation())){
                Graph.addEdge(node1, node);
            }
        }

        // finally our ShortedPathRegistry is now out of date and will need to be recalculated.
        ShortestPathRegistry = new HashMap<>();

        PopulateShortestPathRegistry();
    }

    public void RemoveNode(@NotNull NetworkNode node){
        if(!Graph.containsVertex(node)){
            return;
        }

        Graph.removeVertex(node);

        ShortestPathRegistry = new HashMap<>();

        PopulateShortestPathRegistry();
    }

    public boolean hasNode(@NotNull NetworkNode node){
        return Graph.containsVertex(node);
    }

    private HashMap<NetworkNode /*origin*/, HashMap<NetworkNode/*destination*/, ArrayList<NetworkNode>/*path*/>>
            ShortestPathRegistry = new HashMap<>();

    public ArrayList<NetworkNode> DirectMessage(@NotNull NetworkNode origin, @NotNull NetworkNode destination){
        PopulateShortestPathRegistry();

        if (!ShortestPathRegistry.containsKey(origin)) {
            return null;
        }

        return ShortestPathRegistry.get(origin).get(destination);
    }

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
        if (source.getChannel() != ID){
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

    public @NotNull Set<PlayerEntity> getReceiveFromPlayerRegistry(){
        return Collections.unmodifiableSet(ReceiveFromPlayerRegistry);
    }

    public @NotNull Set<PlayerEntity> getSendToPlayerRegistry() {
        return Collections.unmodifiableSet(SendToPlayerRegistry);
    }

    public void addReceivingFromPlayer(@NotNull PlayerEntity player){
        ReceiveFromPlayerRegistry.add(player);
    }

    public void addSendToPlayer(@NotNull PlayerEntity player){
        SendToPlayerRegistry.add(player);
    }

    public void removeReceivingFromPlayer(@NotNull PlayerEntity player){
        ReceiveFromPlayerRegistry.remove(player);
    }

    public void removeSendToPlayer(@NotNull PlayerEntity player){
        SendToPlayerRegistry.remove(player);
    }

    public boolean hasReceivingFromPlayer(@NotNull PlayerEntity player){
        return ReceiveFromPlayerRegistry.contains(player);
    }

    public boolean hasSendToPlayer(@NotNull PlayerEntity player){
        return SendToPlayerRegistry.contains(player);
    }
}

