package com.proxtextchat.network;

import net.minecraft.util.Identifier;
import net.minecraft.world.chunk.WorldChunk;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;

import java.util.*;

public class NetworkGraph {
    private final DirectedMultigraph<NetworkNode, DefaultEdge> graph = new DirectedMultigraph<>(DefaultEdge.class);


    public NetworkGraph(HashSet<NetworkNode> nodes) throws ChannelMismatch {

        Identifier channel = null;

        // a temporary registry of nodes that lets us avoid looping though the
        // node set a second time.
        HashMap<WorldChunk, HashSet<NetworkNode>> tempNodeRegistry = new HashMap<>();

        // loop though all input nodes and add them to the graph
        for (NetworkNode node : nodes){

            // make sure all nodes have the same channel.
            if(channel != null){
                if(node.getChannel().equals(channel)){
                    throw new ChannelMismatch("Node at" + node.getLocation() + "doesn't match the expected channel");
                }
            }

            // if our channel var is null we fill it
            else{
                channel = node.getChannel();
            }

            // if we haven't thrown an exception, add the node to the graph.
            graph.addVertex();

            // Remember where the node is
            if(tempNodeRegistry.containsKey(node.getLocation())){
                tempNodeRegistry.put(node.getLocation(), new HashSet<>());
            }
            tempNodeRegistry.get(node.getLocation()).add(node);
        }

        // determine which nodes should be connected to each other
        for (NetworkNode node1 : nodes) {
            Set<WorldChunk> chunks = node1.getRange();

            // loop though all the chunks in Node1
            for (WorldChunk chunk : chunks) {

                // check if there are any nodes in this chunk
                if(tempNodeRegistry.containsKey(chunk)){

                    // if there is, loop though all nodes in that chunk
                    for (NetworkNode node2 : tempNodeRegistry.get(chunk)){
                        // dont connect to ourself
                        if (node2 != node1) {
                            // If node2 is in a chunk controlled by node1, add an edge
                            graph.addEdge(node1, node2);
                        }
                    }
                }
            }
        }
    }

    private final HashMap<NetworkNode, HashMap<NetworkNode, List<NetworkNode>>> ShortestPathRegistry = new HashMap<>();

    public List<NetworkNode> DirectMessage(NetworkNode origin, NetworkNode destination){
        PopulateShortestPathRegistry();

        if (ShortestPathRegistry.containsKey(origin)) {
            return ShortestPathRegistry.get(origin).get(destination);
        }
        return null;
    }

    public HashMap<NetworkNode, List<NetworkNode>> Broadcast(NetworkNode origin){
        PopulateShortestPathRegistry();

        return ShortestPathRegistry.get(origin);
    }

    private void PopulateShortestPathRegistry(){
        if (ShortestPathRegistry.isEmpty()){


            DijkstraShortestPath<NetworkNode, DefaultEdge> dijkstra =
                    new DijkstraShortestPath<>(graph);

            // for each node
            for (NetworkNode source : graph.vertexSet()) {
                HashMap<NetworkNode, List<NetworkNode>> pathsFromSource = new HashMap<>();

                // for each node that is not the origin
                for (NetworkNode destination : graph.vertexSet()) {
                    if (!source.equals(destination)) {
                        // get the path between origin and destination
                        List<NetworkNode> path = dijkstra.getPath(source, destination).getVertexList();
                        pathsFromSource.put(destination, path);
                    }
                }
                ShortestPathRegistry.put(source, pathsFromSource);
            }
        }
    }
}

