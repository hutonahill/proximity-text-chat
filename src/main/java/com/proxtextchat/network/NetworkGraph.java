package com.proxtextchat.network;

import net.minecraft.util.Identifier;
import net.minecraft.world.chunk.WorldChunk;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;

import java.util.*;

public class NetworkGraph {
    private final DirectedMultigraph<NetworkNode, DefaultEdge> Graph = new DirectedMultigraph<>(DefaultEdge.class);

    private final HashMap<WorldChunk, HashSet<NetworkNode>> NodeLocationRegistry = new HashMap<>();

    private Identifier Channel = null;

    public NetworkGraph(HashSet<NetworkNode> nodes) throws ChannelMismatch {



        // a registry of nodes that lets us avoid looping though the
        // node set a second time.

        // loop though all input nodes and add them to the graph
        for (NetworkNode node : nodes){

            // make sure all nodes have the same channel.
            if(Channel != null){
                if(node.getChannel().equals(Channel)){
                    throw new ChannelMismatch("Node at" + node.getLocation() + "doesn't match the expected channel");
                }
            }

            // if our channel var is null we fill it
            else{
                Channel = node.getChannel();
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

    public void AddNode(NetworkNode node){

        // make sure the node is not already in the graph.
        if(Graph.containsVertex(node)){
            return;
        }

        // make sure the node is in the right channel.
        if(node.getChannel() != Channel){
            return;
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
    }

    private HashMap<NetworkNode, HashMap<NetworkNode, List<NetworkNode>>> ShortestPathRegistry = new HashMap<>();

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
                    new DijkstraShortestPath<>(Graph);

            // for each node
            for (NetworkNode source : Graph.vertexSet()) {
                HashMap<NetworkNode, List<NetworkNode>> pathsFromSource = new HashMap<>();

                // for each node that is not the origin
                for (NetworkNode destination : Graph.vertexSet()) {
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

